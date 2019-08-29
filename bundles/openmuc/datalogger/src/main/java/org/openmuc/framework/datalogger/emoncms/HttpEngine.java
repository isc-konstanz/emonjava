/* 
 * Copyright 2016-19 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsType;
import org.emoncms.Feed;
import org.emoncms.data.Authentication;
import org.emoncms.data.Authorization;
import org.emoncms.data.Data;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.http.HttpBuilder;
import org.emoncms.http.HttpConnection;
import org.emoncms.http.HttpFeed;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;
import org.openmuc.framework.datalogger.data.Settings;
import org.openmuc.framework.datalogger.emoncms.data.DataContainer;
import org.openmuc.framework.datalogger.engine.DataLoggerEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpEngine implements DataLoggerEngine {
	private final static Logger logger = LoggerFactory.getLogger(HttpEngine.class);

	private final static String ADDRESS = "address";
	private final static String ADDRESS_DEFAULT = "http://localhost/emoncms/";

	private final static String AUTHORIZATION = "authorization";
	private final static String AUTHENTICATION = "authentication";

	private final static String MAX_THREADS = "maxThreads";

	private final static String NODE_ID = "nodeid";
	private final static String FEED_ID = "feedid";

	private final static String API_KEY = "apikey";
	private final static String API_AUTH = "authorization";

	private HttpConnection connection;

	@Override
	public String getId() {
		return EmoncmsType.HTTP.name();
	}

	@Override
	public boolean isActive() {
		return connection != null && !connection.isClosed();
	}

	@Override
	public void onActivate(Configuration config) throws IOException {
		logger.info("Activating Emoncms HTTP Logger");
		
		String address = config.getString(ADDRESS, ADDRESS_DEFAULT);
		HttpBuilder builder = HttpBuilder.create(address);
		if (config.contains(MAX_THREADS)) {
			builder.setMaxThreads(config.getInteger(MAX_THREADS));
		}
		if (config.contains(AUTHORIZATION) && config.contains(AUTHENTICATION)) {
			builder.setCredentials(config.getString(AUTHORIZATION), config.getString(AUTHENTICATION));
		}
		connection = (HttpConnection) builder.build();
		connection.open();
	}

	@Override
	public void onDeactivate() {
		connection.close();
	}

	@Override
	public void doLog(Channel channel, long timestamp) throws IOException {
		if (!isValid(channel)) {
			return;
		}
		String node = channel.getSetting(NODE_ID).asString();
		Long time = channel.getTime();
		if (time == null) {
			time = timestamp;
		}
		connection.post(node, channel.getId(), new Timevalue(time, channel.getValue().asDouble()));
	}

	@Override
	public void doLog(List<Channel> channels, long timestamp) throws IOException {
		List<DataContainer> containers = new ArrayList<DataContainer>();
		for (Channel channel : channels) {
			try {
				if (!isValid(channel)) {
					return;
				}
		        Settings settings = channel.getSettings();
				String node = settings.getString(NODE_ID);
				Long time = channel.getTime();
				if (time == null) {
					time = timestamp;
				}
		        
		        Authentication authentication;
		    	Authorization authorization = Authorization.DEFAULT;
				if (settings.contains(API_AUTH)) {
					authorization = Authorization.valueOf(settings.get(API_AUTH).asString());
				}
		    	switch(authorization) {
		    	case NONE:
		    		throw new EmoncmsSyntaxException("Emoncms authorization unconfigured");
		    	case DEFAULT:
		    		authentication = new Authentication();
		    	default:
		    		authentication = new Authentication(authorization, settings.getString(API_KEY));
		    	}
				DataContainer container = null;
				for (DataContainer c : containers) {
					if (c.equals(authentication)) {
						container = c;
						break;
					}
				}
				if (container == null) {
					// No input collection for that device exists yet, so it needs to be created
					container = new DataContainer(authentication);
					containers.add(container);
				}
				container.add(time, node, new Namevalue(channel.getId(), channel.getValue().asDouble()));
				
			} catch (EmoncmsSyntaxException e) {
				logger.warn("Error preparing record to be logged for Channel \"{}\": {}", 
						channel.getId(), e.getMessage());
			}
		}
		for (DataContainer container : containers) {
			logger.debug("Logging {} values with authentication \"{}\"", container.size(), container.getAuthenticator());
			
			Authentication authenticator = container.getAuthenticator();
			try {
				if (container.size() == 1) {
					Data data = container.get(0);
					
					// Data time is already in seconds, but needs to be in milliseconds for post()
					long time = data.getTime()*1000;
					if (authenticator.isDefault()) {
						connection.post(data.getNode(), time, data.getNamevalues());
					}
					else {
						connection.post(data.getNode(), time, data.getNamevalues(), authenticator);
					}
				}
				else {
					if (authenticator.isDefault()) {
						connection.post(container);
					}
					else {
						connection.post(container, authenticator);
					}
				}
			} catch (EmoncmsException e) {
				logger.warn("Failed to log values: {}", e.getMessage());
			}
		}
	}

	private boolean isValid(Channel channel) throws EmoncmsSyntaxException {
		if (!channel.isValid()) {
			logger.trace("Skipped logging an invalid or empty value for channel \"{}\": {}",
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
        if (!channel.hasSetting(NODE_ID)) {
			throw new EmoncmsSyntaxException("Node needs to be configured");
        }
        Settings settings = channel.getSettings();
        
    	Authorization authorization = Authorization.DEFAULT;
		if (settings.contains(API_AUTH)) {
			authorization = Authorization.valueOf(settings.getString(API_AUTH));
		}
    	switch(authorization) {
    	case DEVICE:
    	case WRITE:
    	case READ:
    		if (!settings.contains(API_KEY)) {
    			throw new EmoncmsSyntaxException("Api Key needs to be configured for "+authorization+" access");
    		}
    	case DEFAULT:
    		break;
    	default:
    		return false;
    	}
		logger.trace("Preparing record to log for channel {}", channel);
		return true;
	}

	@Override
	public List<Record> getRecords(Channel channel, long startTime, long endTime) throws IOException {
		if (!channel.hasSetting(FEED_ID)) {
			throw new EmoncmsException("Unable to retrieve values for channel without configured feed: " + channel.getId());
		}
		Feed feed = HttpFeed.connect(connection, channel.getSetting(FEED_ID).asInt());
		List<Record> records = new LinkedList<Record>();
		List<Timevalue> data = feed.getData(startTime, endTime, channel.getInterval());
		for (Timevalue timevalue : data) {
			records.add(new Record(new DoubleValue(timevalue.getValue()), timevalue.getTime()));
		}
		return records;
	}

}
