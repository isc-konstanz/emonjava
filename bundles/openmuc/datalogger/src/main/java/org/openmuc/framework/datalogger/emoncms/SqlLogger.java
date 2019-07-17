package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsType;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.sql.SqlTimeSeries;
import org.emoncms.sql.SqlBuilder;
import org.emoncms.sql.SqlClient;
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
		List<SqlTimeSeries> timeSeriesArray = new ArrayList<SqlTimeSeries>(channels.size());
		for (Channel channel : channels) {

            if (logger.isTraceEnabled()) {
                logger.trace("channel.getId() " + channel.getId());
            }
            
	        SqlTimeSeries ts = new SqlTimeSeries(channel.getId(), channel.getValueType().name());
	        timeSeriesArray.add(ts);
		}
		client.setTimeSeriesArray(timeSeriesArray);
		client.open();
		
	}

	@Override
	public void doLog(Channel channel, long timestamp) throws IOException {
		if (!isValid(channel)) {
			return;
		}
		String node = channel.getSetting(NODE).asString();
		Long time = channel.getTime();
		if (time == null) {
			time = timestamp;
		}
		client.post(node, channel.getId(), new Timevalue(time, channel.getValue().asDouble()));
	}

	@Override
	public void doLog(List<org.openmuc.framework.datalogger.data.Channel> channels, long timestamp) throws IOException {
		DataList data = new DataList();
		for (Channel channel : channels) {
			try {
				if (!isValid(channel)) {
					return;
				}
				String node = channel.getSetting(NODE).asString();
				Long time = channel.getTime();
				if (time == null) {
					time = timestamp;
				}
				data.add(time, node, new Namevalue(channel.getId(), channel.getValue().asDouble()));
				
			} catch (EmoncmsSyntaxException e) {
				logger.warn("Error preparing record to be logged to Channel \"{}\": {}", 
						channel.getId(), e.getMessage());
			}
		}
		try {
			client.post(data);
			
		} catch (EmoncmsException e) {
			logger.warn("Failed to log values: {}", e.getMessage());
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
		// TODO Auto-generated method stub
		return null;
	}
}
