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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.Feed;
import org.emoncms.data.Authentication;
import org.emoncms.data.Data;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.http.HttpBuilder;
import org.emoncms.http.HttpConnection;
import org.emoncms.http.HttpFeed;
import org.ini4j.Profile.Section;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.emoncms.GeneralConfig.Configuration;
import org.openmuc.framework.datalogger.emoncms.data.Channel;
import org.openmuc.framework.datalogger.emoncms.data.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpLogger extends HttpConnection implements EmoncmsLogger {
	private final static Logger logger = LoggerFactory.getLogger(HttpLogger.class);

	protected HttpLogger(HttpBuilder builder) throws EmoncmsUnavailableException {
		super(builder);
		open();
	}

	@Override
	public void log(Channel channel, long timestamp) throws EmoncmsException {
		if (!isValid(channel)) {
			return;
		}
		Long time = channel.getTime();
		if (time == null) {
			time = timestamp;
		}
		post(channel.getNode(), channel.getId(), new Timevalue(time, channel.getValue().asDouble()));
	}

	@Override
	public void log(List<Channel> channels, long timestamp) throws EmoncmsException {
		List<DataContainer> containers = new ArrayList<DataContainer>();
		for (Channel channel : channels) {
			try {
				if (!isValid(channel)) {
					return;
				}
				Long time = channel.getTime();
				if (time == null) {
					time = timestamp;
				}
				Authentication authenticator = channel.getAuthenticator();
				DataContainer container = null;
				for (DataContainer c : containers) {
					if (c.equals(authenticator)) {
						container = c;
						break;
					}
				}
				if (container == null) {
					// No input collection for that device exists yet, so it needs to be created
					container = new DataContainer(authenticator);
					containers.add(container);
				}
				container.add(time, channel.getNode(), new Namevalue(channel.getId(), channel.getValue().asDouble()));
				
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
						post(data.getNode(), time, data.getNamevalues());
					}
					else {
						post(data.getNode(), time, data.getNamevalues(), authenticator);
					}
				}
				else {
					if (authenticator.isDefault()) {
						post(container);
					}
					else {
						post(container, authenticator);
					}
				}
			} catch (EmoncmsException e) {
				logger.warn("Failed to log values: {}", e.getMessage());
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
        if (!channel.hasNode()) {
			throw new EmoncmsSyntaxException("Node needs to be configured");
        }
    	switch(channel.getAuthorization()) {
    	case DEVICE:
    	case WRITE:
    	case READ:
    		if (!channel.hasApiKey()) {
    			throw new EmoncmsSyntaxException("Api Key needs to be configured for "+channel.getAuthorization()+" access");
    		}
    	case DEFAULT:
    		break;
    	default:
    		return false;
    	}
		logger.trace("Preparing record to log for channel \"{}\": {}", channel.getId(), channel.getRecord());
		return true;
	}

	@Override
	public List<Record> getRecords(Channel channel, long startTime, long endTime) throws EmoncmsException {
		if (!channel.hasFeedId()) {
			throw new EmoncmsException("Unable to retrieve values for channel without configured feed: " + channel.getId());
		}
		Feed feed = HttpFeed.connect(this, channel.getFeedId());
		List<Record> records = new LinkedList<Record>();
		List<Timevalue> data = feed.getData(startTime, endTime, channel.getSettings().getMaxInterval());
		for (Timevalue timevalue : data) {
			records.add(new Record(new DoubleValue(timevalue.getValue()), timevalue.getTime()));
		}
		return records;
	}

	public static HttpLogger open(HttpConfig configs) throws EmoncmsUnavailableException {
		logger.info("Activating Emoncms HTTP Logger");
		
		HttpBuilder builder = HttpBuilder.create(configs.getAddress())
				.setMaxThreads(configs.getMaxThreads());
		
		if (configs.hasCredentials()) {
			builder.setCredentials(configs.getAuthorization(), configs.getAuthentication());
		}
		return new HttpLogger(builder);
	}

	static class HttpConfig extends Configuration {

		private final static String ADDRESS_KEY = "address";
		private final static String ADDRESS_DEFAULT = "http://localhost/emoncms/";

		private final static String AUTHORIZATION_KEY = "authorization";
		private final static String AUTHENTICATION_KEY = "authentication";

		private final static String MAX_THREADS_KEY = "maxThreads";
		private final static int MAX_THREADS_DEFAULT = 1;

		protected HttpConfig(Section configs) throws EmoncmsException {
			super(configs);
		}

		public String getAddress() {
			return configs.get(ADDRESS_KEY, ADDRESS_DEFAULT);
		}

		public int getMaxThreads() {
			return configs.get(MAX_THREADS_KEY, Integer.class, MAX_THREADS_DEFAULT);
		}

		public boolean hasCredentials() {
			return configs.containsKey(AUTHORIZATION_KEY) && configs.containsKey(AUTHENTICATION_KEY);
		}

		public String getAuthorization() {
			return configs.get(AUTHORIZATION_KEY);
		}

		public String getAuthentication() {
			return configs.get(AUTHENTICATION_KEY);
		}
	}

}
