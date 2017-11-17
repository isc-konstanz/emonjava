/* 
 * Copyright 2016-17 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsSyntaxException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.HttpEmoncmsFactory;
import org.emoncms.com.http.HttpInput;
import org.emoncms.data.Authentication;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.openmuc.framework.config.ChannelConfig;
import org.openmuc.framework.config.ConfigChangeListener;
import org.openmuc.framework.config.ConfigService;
import org.openmuc.framework.config.RootConfig;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class EmonLogger implements DataLoggerService, ConfigChangeListener {
	private final static Logger logger = LoggerFactory.getLogger(EmonLogger.class);

	private final static String ADDRESS = "org.openmuc.framework.datalogger.emoncms.address";
	private final static String AUTHORIZATION = "org.openmuc.framework.datalogger.emoncms.authorization";
	private final static String AUTHENTICATION = "org.openmuc.framework.datalogger.emoncms.authentication";
	private final static String MAX_THREADS = "org.openmuc.framework.datalogger.emoncms.maxThreads";

	private DataAccessService dataAccessService;
	private ConfigService configService;

	private Emoncms connection = null;

	private final HashMap<String, ChannelInput> channelInputs = new HashMap<String, ChannelInput>();

    @Activate
	protected void activate(ComponentContext context) {
		
		logger.info("Activating Emoncms Logger");

		String address = System.getProperty(ADDRESS, null);
		String authorization = System.getProperty(AUTHORIZATION, null);
		String authentication = System.getProperty(AUTHENTICATION, null);
		String maxThreadsProperty = System.getProperty(MAX_THREADS, null);
		if (maxThreadsProperty != null) {
			int maxThreads = Integer.parseInt(maxThreadsProperty);
			connection = HttpEmoncmsFactory.newAuthenticatedConnection(address, authorization, authentication, maxThreads);
		}
		else {
			connection = HttpEmoncmsFactory.newAuthenticatedConnection(address, authorization, authentication);
		}
		try {
			connection.start();
			
			RootConfig configs = configService.getConfig(this);
			configureLogging(configs);
			
		} catch (EmoncmsUnavailableException e) {
			logger.warn("Unable to connect to \"{}\"", address);
		}
	}

    @Deactivate
	protected void deactivate(ComponentContext context) {
		
		logger.info("Deactivating Emoncms Logger");
		if (connection != null) {
			connection.stop();
			connection = null;
		}
	}

    @Reference
    protected void bindDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    protected void unbindDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = null;
    }

    @Reference
    protected void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    protected void unsetConfigService(ConfigService configService) {
    	this.configService = null;
    }

	@Override
	public String getId() {
		return "emoncms";
	}

	@Override
	public void setChannelsToLog(List<LogChannel> channels) {
		
		// Will be called if OpenMUC receives new logging configurations.
		// Logging preparations will be done on configurationChanged(), 
		// to allow the immediate logging of listened values
	}

	@Override
	public void configurationChanged() {

		RootConfig configs = configService.getConfig();
		configureLogging(configs);
	}

	private void configureLogging(RootConfig configs) {

		if (connection == null) {
			logger.error("Requested to configure log Channels for deactivated Emoncms logger");
			return;
		}
		channelInputs.clear();
		
		for (String id : dataAccessService.getAllIds()) {
			ChannelConfig channel = configs.getChannel(id);
			
			SettingsHelper settings = new SettingsHelper(channel.getLoggingSettings());
			if (settings.isValid()) {
				try {
					// TODO: verify inputid to be unnecessary here
					Input input = HttpInput.connect(connection, settings.getNode(), id);					
					
					if (channel.isListening() != null && channel.isListening()) {
						ChannelListener channelListener = new ChannelListener(id, input, settings.getAuthentication());
						dataAccessService.getChannel(id).addListener(channelListener);
	
						channelInputs.put(id, channelListener);
					}
					else {
						ChannelInput channelInput = new ChannelInput(id, input, settings.getAuthentication());
						channelInputs.put(id, channelInput);
					}
					
				} catch (EmoncmsUnavailableException | EmoncmsSyntaxException e) {
					logger.warn("Unable to configure logging for Channel \"{}\": {}", channel.getId(), e.getMessage());
				}

				if (logger.isTraceEnabled() && channel.getLoggingInterval() != null) {
					logger.trace("Channel \"{}\" configured to log every {}s", channel.getId(), channel.getLoggingInterval()/1000);
				}
			}
			else if (settings.hasAuthorization()) {
				logger.warn("Unable to configure logging due to invalid syntax for Channel \"{}\": {}", channel.getId(), channel.getLoggingSettings());
			}
		}
	}

	@Override
	public synchronized void log(List<LogRecordContainer> containers, long timestamp) {

		if (connection == null) {
			logger.error("Requested to log values for deactivated Emoncms logger");
		}
		else if (containers == null || containers.isEmpty()) {
			logger.debug("Requested Emoncms logger to log an empty container list");
		}
		else if (containers.size() == 1) {
			
			LogRecordContainer container = containers.get(0);
			if (isValid(container)) {
				ChannelInput channel = channelInputs.get(container.getChannelId());
				try {
					Record record = container.getRecord();
					Long time = record.getTimestamp();
					if (time == null) {
						time = timestamp;
					}
					Timevalue timevalue = new Timevalue(time, record.getValue().asDouble());
					
					channel.post(timevalue);
					
				} catch (EmoncmsException | TypeConversionException e) {
					logger.warn("Failed to log record for channel \"{}\": {}", container.getChannelId(), e.getMessage());
				}
			}
		}
		else {
			List<DeviceDataList> devices = new ArrayList<DeviceDataList>();
			for (LogRecordContainer container : containers) {
				if (isValid(container)) {
					ChannelInput channel = channelInputs.get(container.getChannelId());
					try {
						Record record = container.getRecord();
						Long time = record.getTimestamp();
						if (time == null) {
							time = timestamp;
						}
						Namevalue value = new Namevalue(container.getChannelId(), record.getValue().asDouble());
						
						Authentication authenticator = channel.getAuthenticator();
						DeviceDataList device = null;
						for (DeviceDataList d : devices) {
							if (d.hasSameAuthentication(authenticator)) {
								d.add(time, channel.getInput().getNode(), value);
								device = d;
								break;
							}
						}
						if (device == null) {
							// No input collection for that device exists yet, so it needs to be created
							device = new DeviceDataList(authenticator);
							device.add(time, channel.getInput().getNode(), value);
							
							devices.add(device);
						}
						
					} catch (TypeConversionException e) {
						logger.warn("Failed to prepare record to log to Channel \"{}\": {}", container.getChannelId(), e.getMessage());
					}
				}
			}
			
			for (DeviceDataList device : devices) {
				
				logger.debug("Logging {} values with authentication \"{}\"", device.size(), device.getAuthenticator());
				try {
					Authentication authenticator = device.getAuthenticator();
					if (authenticator.isDefault()) {
						connection.post(device);
					}
					else {
						connection.post(device, device.getAuthenticator());
					}
				} catch (EmoncmsException e) {
					logger.warn("Error logging values: {}", e.getMessage());
				}
			}
		}
	}

	private boolean isValid(LogRecordContainer container) {
		
		if (channelInputs.containsKey(container.getChannelId())) {
		
			if (container.getRecord() != null) {
				if (container.getRecord().getFlag() == Flag.VALID && container.getRecord().getValue() != null) {
					
					if (logger.isTraceEnabled()) {
						logger.trace("Preparing record to log for channel \"{}\": {}", container.getChannelId(), container.getRecord());
					}
					return true;
				}
				else if (logger.isDebugEnabled()) {
					logger.debug("Skipped logging an invalid or empty value for channel \"{}\": {}",
							container.getChannelId(), container.getRecord().getFlag().toString());
				}
			}
			else if (logger.isTraceEnabled()) {
				logger.trace("Failed to log an empty record for channel \"{}\"", container.getChannelId());
			}
		}
		else logger.warn("Failed to log record for unconfigured channel \"{}\"", container.getChannelId());
		
		return false;
	}

	@Override
	public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
		
		// TODO: fetch feed id and retrieve data from web server
		throw new UnsupportedOperationException();
	}
}
