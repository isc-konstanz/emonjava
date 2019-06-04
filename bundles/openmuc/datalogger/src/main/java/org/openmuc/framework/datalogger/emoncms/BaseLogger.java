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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.datalogger.dynamic.DynamicLogger;
import org.openmuc.framework.datalogger.dynamic.LogChannel;
import org.openmuc.framework.datalogger.emoncms.HttpLogger.HttpConfig;
import org.openmuc.framework.datalogger.emoncms.MqttLogger.MqttConfig;
import org.openmuc.framework.datalogger.emoncms.data.Channel;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DataLoggerService.class)
public class BaseLogger extends DynamicLogger {
	private final static Logger logger = LoggerFactory.getLogger(BaseLogger.class);

	private final Map<EmoncmsType, EmoncmsLogger> connections = new LinkedHashMap<EmoncmsType, EmoncmsLogger>();

	@Reference
	private DataAccessService dataAccess;

	private GeneralConfig configs;

	@Override
	public String getId() {
		return "emoncms";
	}

	@Activate
	protected void activate(ComponentContext context) {
		try {
			configs = GeneralConfig.load();
			
		} catch (IOException e) {
			logger.error("Error while reading emoncms configuration: {}", e.getMessage());
		}
		activate(EmoncmsType.MQTT);
		activate(EmoncmsType.HTTP);
	}

	protected void activate(EmoncmsType type) {
		try {
			if (!configs.contains(type)) {
				logger.debug("Skipping {} Logger activation", type.name());
				return;
			}
			try {
				EmoncmsLogger connection;
				switch(type) {
				case MQTT:
					connection = MqttLogger.open(configs.get(type, MqttConfig.class));
					break;
				case HTTP:
					connection = HttpLogger.open(configs.get(type, HttpConfig.class));
					break;
				default:
					return;
				}
				connections.put(connection.getType(), connection);
				
			} catch (EmoncmsUnavailableException e) {
				logger.warn("Unable to establish {} connection. "
						+ "Please remove or disable the configuration section if this is intentional.", type.name());
			}
		} catch (Exception e) {
			logger.error("Error while reading {} configuration: {}", type.name(), e.getMessage());
		}
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		logger.info("Deactivating Emoncms Logger");
		for (EmoncmsLogger connection : connections.values()) {
			try {
				if (!connection.isClosed()) {
					connection.close();
				}
			} catch (IOException e) {
				logger.warn("Error while closing emoncms connection: {}", e.getMessage());
			}
		}
	}

	@Override
	protected void logChannels(List<LogChannel> channels, long timestamp) throws EmoncmsException {
		Map<EmoncmsType, List<Channel>> containers = new HashMap<EmoncmsType, List<Channel>>();
		for (int i=0; i<channels.size(); i++) {
			Channel channel = new Channel(channels.get(i));
			
			EmoncmsType type = channel.getType();
			EmoncmsLogger connection = getLogger(type);
			if (type != connection.getType()) {
				type = connection.getType();
			}
			
			if (channels.size() == 1) {
				connection.log(channel, timestamp);
				return;
			}
			if (!containers.containsKey(type)) {
				containers.put(type, new LinkedList<Channel>());
			}
			containers.get(type).add(channel);
		}
		for (Entry<EmoncmsType, List<Channel>> container : containers.entrySet()) {
			connections.get(container.getKey()).log(container.getValue(), timestamp);
		}
	}

	@Override
	public List<Record> getRecords(String id, long startTime, long endTime) throws IOException {
		if (!handlers.containsKey(id)) {
			logger.warn("Failed to retrieve records for unconfigured channel \"{}\"", id);
			return null;
		}
		Channel channel = new Channel(handlers.get(id));
		
		return getLogger(channel.getType()).getRecords(channel, startTime, endTime);
	}

	public EmoncmsLogger getLogger(EmoncmsType type) {
		if (type == EmoncmsType.DEFAULT) {
			type = configs.getDefault();
		}
		if (connections.containsKey(type)) {
			return connections.get(type);
		}
		else {
			return connections.values().iterator().next();
		}
	}

}
