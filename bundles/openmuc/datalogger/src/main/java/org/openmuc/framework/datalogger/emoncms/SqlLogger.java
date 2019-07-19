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
import org.emoncms.sql.SqlBuilder;
import org.emoncms.sql.SqlClient;
import org.emoncms.sql.SqlFeed;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;
import org.openmuc.framework.datalogger.dynamic.DynamicLoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlLogger implements DynamicLoggerService {

	private final static Logger logger = LoggerFactory.getLogger(SqlLogger.class);

	private final static String CONNECTION_URL = "connectionUrl";
	private final static String CONNECTION_URL_DEFAULT = "jdbc:mysql://127.0.0.1:3306/openmuc?useSSL=false";
	private final static String CONNECTION_DRIVER_CLASS = "connectionDriverClass";
	private final static String CONNECTION_DRIVER_CLASS_DEFAULT = "com.mysql.jdbc.Driver";

	private final static String USER = "user";
	private final static String PASSWORD = "password";

	private final static String NODE = "nodeid";
	private final static String FEED_ID = "feedid";

	private SqlClient client;

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
		
		String connectionUrl = config.getString(CONNECTION_URL, CONNECTION_URL_DEFAULT);
		SqlBuilder builder = SqlBuilder.create(connectionUrl);
		if (config.contains(CONNECTION_DRIVER_CLASS)) {
			builder.setConnectionDriverClass(config.getString(CONNECTION_DRIVER_CLASS, CONNECTION_DRIVER_CLASS_DEFAULT));
		}
		if (config.contains(USER) && config.contains(PASSWORD)) {
			builder.setCredentials(config.getString(USER), config.getString(PASSWORD));
		}
		client = (SqlClient) builder.build();
//		client.open();
	}

	@Override
	public void onDeactivate() {
		client.close();
	}

	@Override 
	public void onConfigure(List<Channel> channels) throws IOException {
		logger.info("Configuring Emoncms SQL Logger");
		Map<Integer, SqlFeed> feedMap = new HashMap<Integer, SqlFeed>(channels.size());
		for (Channel channel : channels) {

            if (logger.isTraceEnabled()) {
                logger.trace("channel.getId() " + channel.getId());
            }
            
	        SqlFeed feed = new SqlFeed(channel.getSetting(FEED_ID).asInt(), channel.getValueType().name());
	        feed.setEntityName(channel.getId());
	        feedMap.put(channel.getSetting(FEED_ID).asInt(), feed);
		}
		client.setFeedMap(feedMap);
		client.open();
		
		for (SqlFeed feed : feedMap.values()) {
			feed.setSessionFactory(client.getSessionFactory());
		}
	}

	@Override
	public void doLog(Channel channel, long timestamp) throws IOException {
		//TODO
	}

	@Override
	public void doLog(List<org.openmuc.framework.datalogger.data.Channel> channels, long timestamp) throws IOException {
		//TODO
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
			Feed feed = client.getFeed(channel.getSetting(FEED_ID).asInt());
			List<Timevalue> data = feed.getData(startTime, endTime, channel.getInterval());
			for (Timevalue timevalue : data) {
				records.add(new Record(new DoubleValue(timevalue.getValue()), timevalue.getTime()));
			}
		}
		return records;
	}
}
