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

	protected final static String CONNECTION_URL = "connectionUrl";
	protected final static String CONNECTION_URL_DEFAULT = "jdbc:mysql://127.0.0.1:3306/openmuc?useSSL=false";
	protected final static String CONNECTION_DRIVER_CLASS = "connectionDriverClass";
	protected final static String CONNECTION_DRIVER_CLASS_DEFAULT = "com.mysql.jdbc.Driver";

	protected final static String USER = "user";
	protected final static String PASSWORD = "password";

	protected final static String NODE = "nodeid";
	protected final static String FEED_ID = "feedid";
	protected final static String FEED_PREFIX = "feed_";

	private SqlClient client;
	private Map<String, Feed> channelIdFeedsMap;

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
            
//	        SqlFeed feed = new SqlFeed(client, channel.getSetting(FEED_ID).asInt(), channel.getValueType().name());
            Value feedId = channel.getSetting(FEED_ID);
            if (feedId != null) {
    	        SqlFeed feed = new SqlFeed(client, feedId.asInt());
    	        feedMap.put(feedId.asInt(), feed);
            }
            else {
    	        SqlFeed feed = new SqlFeed(client, channel.getId());
    	        if (channelIdFeedsMap == null) {
    	        	channelIdFeedsMap = new HashMap<String, Feed>();
    	        }
    	        channelIdFeedsMap.put(channel.getId(), feed);
            }
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
            Value feedId = channel.getSetting(FEED_ID);
            Feed feed;
            if (feedId != null) {
            	feed = client.getFeed(channel.getSetting(FEED_ID).asInt());
            }
            else {
            	feed = channelIdFeedsMap.get(channel.getId());
            }
            timestamp = Math.round(timestamp/1000.0);
			Timevalue timevalue = new Timevalue(timestamp, channel.getValue().asDouble());
			feed.insertData(timevalue);
		}
	}

	@Override
	public void doLog(List<org.openmuc.framework.datalogger.data.Channel> channels, long timestamp) throws IOException {
        timestamp = Math.round(timestamp/1000.0);
		for (Channel channel : channels) {
			try {
				if (!isValid(channel)) {
					return;
				}
				if (!client.isClosed()) {
		            Value feedId = channel.getSetting(FEED_ID);
		            Feed feed;
		            if (feedId != null) {
		            	feed = client.getFeed(channel.getSetting(FEED_ID).asInt());
		            }
		            else {
		            	feed = channelIdFeedsMap.get(channel.getId());
		            }
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
            Value feedId = channel.getSetting(FEED_ID);
            Feed feed;
            if (feedId != null) {
            	feed = client.getFeed(channel.getSetting(FEED_ID).asInt());
            }
            else {
            	feed = channelIdFeedsMap.get(channel.getId());
            }
            startTime = Math.round(startTime/1000.0);
            endTime = Math.round(endTime/1000.0);
			List<Timevalue> data = feed.getData(startTime, endTime, channel.getInterval());
			for (Timevalue timevalue : data) {
				Double d = timevalue.getValue();
				Long time = timevalue.getTime() * 1000;
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
}
