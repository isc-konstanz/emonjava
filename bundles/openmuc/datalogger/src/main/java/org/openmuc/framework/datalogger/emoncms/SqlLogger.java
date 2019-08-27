package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsType;
import org.emoncms.Feed;
import org.emoncms.data.Timevalue;
import org.emoncms.hibernate.HibernateBuilder;
import org.emoncms.hibernate.HibernateClient;
import org.emoncms.hibernate.HibernateFeed;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;
import org.openmuc.framework.datalogger.dynamic.DynamicLoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlLogger implements DynamicLoggerService {

	private final static Logger logger = LoggerFactory.getLogger(SqlLogger.class);

	protected final static String CONNECTION_ADDRESS = "address";
	protected final static String CONNECTION_ADDRESS_DEFAULT = "127.0.0.1";
	protected final static String CONNECTION_PORT = "port";
	protected final static int    CONNECTION_PORT_DEFAULT = 3306;
	protected final static String CONNECTION_DB_NAME = "databaseName";
	protected final static String CONNECTION_DB_NAME_DEFAULT = "openmuc";
	protected final static String CONNECTION_DB_TYPE = "databaseType";
	protected final static String CONNECTION_DB_TYPE_DEFAULT = "jdbc:mysql";
	protected final static String CONNECTION_DRIVER_CLASS = "connectionDriverClass";
	protected final static String CONNECTION_DRIVER_CLASS_DEFAULT = "com.mysql.jdbc.Driver";

	protected final static String DB_DIALECT = "databaseDialect";
	protected final static String DB_DIALECT_DEFAULT = "org.hibernate.dialect.MariaDBDialect";

	protected final static String USER = "user";
	protected final static String PASSWORD = "password";

	protected final static String PREFIX = "prefix";
	protected final static String GENERIC = "generic";

	protected final static String NODE = "nodeid";
	protected final static String FEED_ID = "feedid";
	protected final static String FEED_PREFIX = "feed_";

	private HibernateClient client;
	private String prefix = "";
	private boolean isGeneric = false;

	@Override
	public String getId() {
		return EmoncmsType.SQL.name();
	}

	@Override
	public boolean isActive() {
		return client != null; // && !client.isClosed();
	}

	@Override
	public void onActivate(Configuration config) throws IOException {
		logger.info("Activating Emoncms SQL Logger");
		
		String connectionUrl = config.getString(CONNECTION_ADDRESS, CONNECTION_ADDRESS_DEFAULT);
		HibernateBuilder builder = HibernateBuilder.create(connectionUrl);
		if (config.contains(CONNECTION_DRIVER_CLASS)) {
			builder.setConnectionDriverClass(config.getString(CONNECTION_DRIVER_CLASS, CONNECTION_DRIVER_CLASS_DEFAULT));
		}
		if (config.contains(CONNECTION_PORT)) {
			builder.setPort(config.getInteger(CONNECTION_PORT, CONNECTION_PORT_DEFAULT));
		}
		if (config.contains(CONNECTION_DB_NAME)) {
			builder.setDatabaseName(config.getString(CONNECTION_DB_NAME, CONNECTION_DB_NAME_DEFAULT));
		}
		if (config.contains(CONNECTION_DB_TYPE)) {
			builder.setDatabaseType(config.getString(CONNECTION_DB_TYPE, CONNECTION_DB_TYPE_DEFAULT));
		}
		if (config.contains(DB_DIALECT)) {
			builder.setDatabaseDialect(config.getString(DB_DIALECT, DB_DIALECT_DEFAULT));
		}
		if (config.contains(USER) && config.contains(PASSWORD)) {
			builder.setCredentials(config.getString(USER), config.getString(PASSWORD));
		}
		if (config.contains(PREFIX)) {
			prefix = config.getString(PREFIX);
		}
		if (config.contains(GENERIC)) {
			isGeneric = config.getBoolean(GENERIC);
		}
		
		
		client = (HibernateClient) builder.build();
//		client.open();
	}

	@Override
	public void onDeactivate() {
		client.close();
	}

	@Override 
	public void onConfigure(List<Channel> channels) throws IOException {
		logger.info("Configuring Emoncms SQL Logger");
		Map<String, HibernateFeed> feedMap = new HashMap<String, HibernateFeed>(channels.size());
		for (Channel channel : channels) {

            if (logger.isTraceEnabled()) {
                logger.trace("channel.getId() " + channel.getId());
            }
            
            String entityName = getEntityName(channel);
            HibernateFeed feed = new HibernateFeed(client, entityName);
            feed.setValueType(channel.getValueType().toString());
	        feedMap.put(entityName, feed);
		}
		client.setFeedMap(feedMap);
		client.open();
	}

	@Override
	public void doLog(Channel channel, long timestamp) throws IOException {
		if (!isValid(channel)) {
			return;
		}
		if (!client.isClosed()) {
            String entityName = getEntityName(channel);
            Feed feed = client.getFeed(entityName);

			Timevalue timevalue = new Timevalue(timestamp, channel.getValue().asDouble());
			feed.insertData(timevalue);
		}
	}

	@Override
	public void doLog(List<org.openmuc.framework.datalogger.data.Channel> channels, long timestamp) throws IOException {
		for (Channel channel : channels) {
			try {
				if (!isValid(channel)) {
					return;
				}
				if (!client.isClosed()) {
		            String entityName = getEntityName(channel);
		            Feed feed = client.getFeed(entityName);
					Timevalue timevalue = new Timevalue(timestamp, channel.getValue().asDouble());
					feed.insertData(timevalue);					
				}
			} 
			catch (EmoncmsSyntaxException e) {
				logger.warn("Error preparing record to be logged for Channel \"{}\": {}", 
						channel.getId(), e.getMessage());
			}
		}
	}

	private boolean isValid(Channel channel) throws EmoncmsSyntaxException {
		if (!channel.isValid()) {
			logger.debug("Skipped logging an invalid or empty value for channel \"{}\": {}",
					channel.getId(), channel.getFlag());
			
			return false;
		}
		switch(channel.getValueType()) {
		case DOUBLE:
		case FLOAT:
		case LONG:
		case INTEGER:
		case SHORT:
		case BYTE:
		case BOOLEAN:
			break;
		default:
			throw new EmoncmsSyntaxException("Invalid value type: "+channel.getValueType());
		}
        if (!channel.hasSetting(NODE)) {
			throw new EmoncmsSyntaxException("Node needs to be configured");
        }
		logger.trace("Preparing record to log for channel {}", channel);
		return true;
	}

	@Override
	public List<Record> getRecords(Channel channel, long startTime, long endTime)
			throws IOException {
		if (!channel.hasSetting(FEED_ID)) {
			throw new EmoncmsException("Unable to retrieve values for channel without configured feed: " + channel.getId());
		}
		List<Record> records = new LinkedList<Record>();
		if (!client.isClosed()) {
            String entityName = getEntityName(channel);
            Feed feed = client.getFeed(entityName);

			List<Timevalue> data = feed.getData(startTime, endTime, channel.getInterval());
			for (Timevalue timevalue : data) {
				Double d = timevalue.getValue();
				Long time = timevalue.getTime();
				switch (channel.getValueType()) {
					case BOOLEAN:
						boolean v = (d.intValue()!= 0);
						records.add(new Record(new BooleanValue(v), time));
						break;
					case BYTE:
						records.add(new Record(new ByteValue(d.byteValue()), time));
						break;
					case DOUBLE:
						records.add(new Record(new DoubleValue(d), time));
						break;
					case FLOAT:
						records.add(new Record(new FloatValue(d.floatValue()), time));
						break;
					case INTEGER:
						records.add(new Record(new IntValue(d.intValue()), time));
						break;
					case LONG:
						records.add(new Record(new LongValue(d.longValue()), time));
						break;
					case SHORT:
						records.add(new Record(new ShortValue(d.shortValue()), time));
						break;
					case STRING:
						records.add(new Record(new StringValue(String.valueOf(d)), time));
						break;
					default:
						records.add(new Record(new StringValue(String.valueOf(d)), time));
						break;
				}
			}
		}
		return records;
	}
	
	protected String getEntityName(Channel channel) throws EmoncmsSyntaxException {
        Value feedId = channel.getSetting(FEED_ID);
        String entityName = prefix;
        if (isGeneric) {
	        if (feedId != null) {
	        	if (prefix.equals("")) entityName += FEED_PREFIX;
	        	entityName += feedId.asString();
	        }
	        else {
	        	throw new EmoncmsSyntaxException("Feed id not available!");
	        }
        }
        else {
        	if (prefix.equals("") && isNumeric(channel.getId())) {
        		entityName = FEED_PREFIX;
        	}
        	entityName += channel.getId();
        }
		return entityName;
	}

	public static boolean isNumeric(String str) { 
		try {  
			Double.parseDouble(str);  
			return true;
		} 
		catch(NumberFormatException e){  
			return false;  
		}  
	}
	
	public HibernateClient getClient() {
		return client;
	}
}
