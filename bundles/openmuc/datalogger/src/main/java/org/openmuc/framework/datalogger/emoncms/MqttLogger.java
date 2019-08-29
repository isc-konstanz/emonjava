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
import java.util.List;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsType;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.mqtt.MqttBuilder;
import org.emoncms.mqtt.MqttClient;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;
import org.openmuc.framework.datalogger.dynamic.DynamicLoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttLogger implements DynamicLoggerService {
	private final static Logger logger = LoggerFactory.getLogger(MqttLogger.class);

	private final static String ADDRESS = "address";
	private final static String ADDRESS_DEFAULT = "tcp://localhost";
	private final static String PORT = "port";

	private final static String USER = "user";
	private final static String PASSWORD = "password";

	private final static String NODE = "nodeid";

	private MqttClient client;

	@Override
	public String getId() {
		return EmoncmsType.MQTT.name();
	}

	@Override
	public boolean isActive() {
		return client != null && !client.isClosed();
	}

	@Override
	public void onActivate(Configuration config) throws IOException {
		logger.info("Activating Emoncms MQTT Logger");
		
		String address = config.getString(ADDRESS, ADDRESS_DEFAULT);
		MqttBuilder builder = MqttBuilder.create(address);
		if (config.contains(PORT)) {
			builder.setPort(config.getInteger(PORT));
		}
		if (config.contains(USER) && config.contains(PASSWORD)) {
			builder.setCredentials(config.getString(USER), config.getString(PASSWORD));
		}
		client = (MqttClient) builder.build();
		client.open();
	}

	@Override
	public void onDeactivate() {
		client.close();
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
	public void doLog(List<Channel> channels, long timestamp) throws IOException {
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
        if (!channel.hasSetting(NODE)) {
			throw new EmoncmsSyntaxException("Node needs to be configured");
        }
		logger.trace("Preparing record to log for channel {}", channel);
		return true;
	}

	@Override
	public List<Record> getRecords(Channel channel, long startTime, long endTime) throws IOException {
		throw new UnsupportedOperationException();
	}

}
