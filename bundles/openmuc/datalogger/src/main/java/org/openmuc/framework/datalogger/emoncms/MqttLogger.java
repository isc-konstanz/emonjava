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

import java.util.List;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.mqtt.MqttBuilder;
import org.emoncms.mqtt.MqttClient;
import org.ini4j.Profile.Section;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.emoncms.GeneralConfig.Configuration;
import org.openmuc.framework.datalogger.emoncms.data.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttLogger extends MqttClient implements EmoncmsLogger {
	private final static Logger logger = LoggerFactory.getLogger(MqttLogger.class);

	protected MqttLogger(MqttBuilder builder) throws EmoncmsUnavailableException {
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
		DataList data = new DataList();
		for (Channel channel : channels) {
			try {
				if (!isValid(channel)) {
					return;
				}
				Long time = channel.getTime();
				if (time == null) {
					time = timestamp;
				}
				data.add(time, channel.getNode(), new Namevalue(channel.getId(), channel.getValue().asDouble()));
				
			} catch (EmoncmsSyntaxException e) {
				logger.warn("Error preparing record to be logged to Channel \"{}\": {}", 
						channel.getId(), e.getMessage());
			}
		}
		try {
			post(data);
			
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
        if (!channel.hasNode()) {
			throw new EmoncmsSyntaxException("Node needs to be configured");
        }
		logger.trace("Preparing record to log for channel \"{}\": {}", channel.getId(), channel.getRecord());
		return true;
	}

	@Override
	public List<Record> getRecords(Channel channel, long startTime, long endTime) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public static MqttLogger open(MqttConfig configs) throws EmoncmsUnavailableException {
		logger.info("Activating Emoncms MQTT Logger");
		
		MqttBuilder builder = MqttBuilder.create(configs.getAddress())
				.setPort(configs.getPort());
		
		if (configs.hasCredentials()) {
			builder.setCredentials(configs.getUser(), configs.getPassword());
		}
		return new MqttLogger(builder);
	}

	static class MqttConfig extends Configuration {

		private final static String ADDRESS_KEY = "address";
		private final static String ADDRESS_DEFAULT = "tcp://localhost";

		private final static String PORT_KEY = "port";
		private final static int PORT_DEFAULT = 1883;

		private final static String USER_KEY = "user";
		private final static String PASSWORD_KEY = "password";

		protected MqttConfig(Section configs) throws EmoncmsException {
			super(configs);
		}

		public String getAddress() {
			return configs.get(ADDRESS_KEY, ADDRESS_DEFAULT);
		}

		public int getPort() {
			return configs.get(PORT_KEY, Integer.class, PORT_DEFAULT);
		}

		public boolean hasCredentials() {
			return configs.containsKey(USER_KEY) && configs.containsKey(PASSWORD_KEY);
		}

		public String getUser() {
			return configs.get(USER_KEY);
		}

		public String getPassword() {
			return configs.get(PASSWORD_KEY);
		}
	}

}
