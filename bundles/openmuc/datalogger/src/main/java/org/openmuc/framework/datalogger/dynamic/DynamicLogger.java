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
package org.openmuc.framework.datalogger.dynamic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;
import org.openmuc.framework.datalogger.data.Settings;
import org.openmuc.framework.datalogger.dynamic.DynamicLoggerCollection.ChannelCollection;
import org.openmuc.framework.datalogger.emoncms.HttpLogger;
import org.openmuc.framework.datalogger.emoncms.MqttLogger;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DynamicLogger implements DataLoggerService {
	private final static Logger logger = LoggerFactory.getLogger(DynamicLogger.class);

    private static final String CONFIG = System.getProperty(DynamicLogger.class.
    		getPackage().getName().toLowerCase() + ".config", "conf" + File.separator + "emoncms.conf");

    private static final String DEFAULT = System.getProperty(DynamicLogger.class.
    		getPackage().getName().toLowerCase() + ".default", "HTTP");

	protected final Map<String, DynamicLoggerService> services = new LinkedHashMap<String, DynamicLoggerService>();

	private final Map<String, ChannelHandler> handlers = new HashMap<String, ChannelHandler>();

	private DataAccessService access = null;

	@Override
	public String getId() {
		return "emoncms";
	}

	@Activate
	protected void activate(ComponentContext context) {
		try {
			Ini config = new Ini(new File(CONFIG));
			activate(config, EmoncmsType.MQTT);
			activate(config, EmoncmsType.HTTP);
			
		} catch (IOException e) {
			logger.error("Error while reading emoncms configuration: {}", e.getMessage());
		}
	}

	protected void activate(Ini config, EmoncmsType type) {
		try {
			Section section;
			if (config.containsKey(type.name())) {
				section = config.get(type.name());
			}
			else if (config.keySet().size() == 1) {
	    		section = config.get("Emoncms");
			}
			else {
				logger.debug("Skipping {} Logger activation", type.name());
				return;
			}
			try {
				DynamicLoggerService service;
				switch(type) {
				case MQTT:
					service = new MqttLogger();
					break;
				case HTTP:
					service = new HttpLogger();
					break;
				default:
					return;
				}
				service.onActivate(new Configuration(section));
				services.put(service.getId(), service);
				
			} catch (EmoncmsUnavailableException e) {
				logger.warn("Unable to establish {} connection. "
						+ "Please remove or disable the configuration section if this is intentional.", type.name());
			}
		} catch (Exception e) {
			logger.error("Error while activating logger \"{}: {}", type.name(), e.getMessage());
		}
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		logger.info("Deactivating Emoncms Logger");
		for (DynamicLoggerService service : services.values()) {
			try {
				if (service.isActive()) {
					service.onDeactivate();
				}
			} catch (Exception e) {
				logger.warn("Error while deactivating logger: {}", e);
			}
		}
	}

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    protected void bindDataAccessService(DataAccessService service) {
    	this.access = service;
    	for (ChannelHandler handler : handlers.values()) {
    		if (handler.isAveraging()) {
    			AverageHandler listener = (AverageHandler) handler;
    			if (!listener.isListening() && service.getAllIds().contains(handler.getId())) {
    				listener.setListening(true);
    				service.getChannel(handler.getId()).addListener(listener);
    			}
    		}
		}
    }

    protected void unbindDataAccessService(DataAccessService service) {
    	for (ChannelHandler handler : handlers.values()) {
    		if (handler.isAveraging()) {
    			AverageHandler listener = (AverageHandler) handler;
    			if (!listener.isListening() && service.getAllIds().contains(handler.getId())) {
    				listener.setListening(false);
    				service.getChannel(handler.getId()).removeListener(listener);
    			}
    		}
		}
    	this.access = null;
    }

	@Override
	public void setChannelsToLog(List<org.openmuc.framework.datalogger.spi.LogChannel> channels) {
		try {
			// Will be called if OpenMUC receives new logging configurations
			for (ChannelHandler handler : handlers.values()) {
				if (handler instanceof AverageHandler && 
						access != null && access.getAllIds().contains(handler.getId())) {
					access.getChannel(handler.getId()).removeListener((AverageHandler) handler);
				}
			}
			handlers.clear();
			
			for (LogChannel channel : channels) {
				String id = channel.getId();
				
				Settings settings = new Settings(channel);
				ChannelHandler handler;
				if (settings.isAveraging()) {
					handler = new AverageHandler(channel, settings);
					if (access != null && access.getAllIds().contains(id)) {
						access.getChannel(id).addListener((AverageHandler) handler);
						((AverageHandler) handler).setListening(true);
					}
				}
				else if (settings.isDynamic()) {
					handler = new DynamicHandler(channel, settings);
				}
				else {
					handler = new ChannelHandler(channel, settings);
				}
				this.handlers.put(id, handler);
				
				logger.debug("{} \"{}\" configured to log every {}s", 
						handler.getClass().getSimpleName(), id, channel.getLoggingInterval()/1000);
			}
			for (DynamicLoggerService service : services.values()) {
				if (service.isActive()) {
					service.onConfigure(new ArrayList<Channel>(handlers.values()));
				}
			}
		} catch (Exception e) {
			logger.warn("Error while configuring channels:", e);
		}
	}

	@Override
	public void log(List<LogRecordContainer> containers, long timestamp) {
		if (containers == null || containers.isEmpty()) {
			logger.debug("Requested Emoncms logger to log an empty container list");
			return;
		}
		DynamicLoggerCollection loggers = new DynamicLoggerCollection(this);
		for (LogRecordContainer container : containers) {
			if (!handlers.containsKey(container.getChannelId())) {
				logger.warn("Failed to log record for unconfigured channel \"{}\"", container.getChannelId());
				continue;
			}
			try {
				ChannelHandler channel = handlers.get(container.getChannelId());
				if (channel.update(container.getRecord())) {
					loggers.add(channel);
				}
			} catch (IOException | TypeConversionException e) {
				logger.warn("Failed to prepare record to log to channel \"{}\": {}", container.getChannelId(), e.getMessage());
			}
		}
		for (ChannelCollection channels : loggers) {
			try {
				channels.log(timestamp);
				
			} catch(IOException e) {
				logger.warn("Failed to log channels: {}", e.getMessage());
				
			} catch(Exception e) {
				logger.warn("Error while logging channels:", e);
			}
		}
	}

	@Override
	public List<Record> getRecords(String id, long startTime, long endTime) throws IOException {
		if (!handlers.containsKey(id)) {
			logger.warn("Failed to retrieve records for unconfigured channel \"{}\"", id);
			return null;
		}
		Channel channel = handlers.get(id);
		
		return getLogger(channel).getRecords(channel, startTime, endTime);
	}

	protected DynamicLoggerService getLogger(Channel channel) throws IOException {
		String id = channel.getLogger();
		if (id == null) {
			id = DEFAULT;
		}
		if (services.containsKey(id)) {
			return services.get(id);
		}
		else if (services.size() > 0) {
			return services.values().iterator().next();
		}
		throw new IOException("DataLogger unavailable: "+id);
	}

}
