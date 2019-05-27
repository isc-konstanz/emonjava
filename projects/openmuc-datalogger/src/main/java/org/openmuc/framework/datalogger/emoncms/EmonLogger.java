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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.Feed;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsSyntaxException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.HttpEmoncmsFactory;
import org.emoncms.com.http.HttpFeed;
import org.emoncms.com.http.HttpInput;
import org.emoncms.com.mqtt.MqttEmoncmsFactory;
import org.emoncms.com.mqtt.MqttInput;
import org.emoncms.data.Authentication;
import org.emoncms.data.Data;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.datalogger.emoncms.data.ChannelAverageHandler;
import org.openmuc.framework.datalogger.emoncms.data.ChannelDynamicHandler;
import org.openmuc.framework.datalogger.emoncms.data.ChannelLogHandler;
import org.openmuc.framework.datalogger.emoncms.data.ChannelLogSettings;
import org.openmuc.framework.datalogger.emoncms.data.DeviceDataList;
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
public class EmonLogger implements DataLoggerService {
	private final static Logger logger = LoggerFactory.getLogger(EmonLogger.class);

    @Reference
    private DataAccessService dataAccessService;
	private Emoncms httpConnection = null;
	private Emoncms mqttConnection = null;
	private EmoncmsConfig configs = null;
	private boolean isMqttConnectionType = false;
	
	private final HashMap<String, ChannelLogHandler> httpChannelHandlers = new HashMap<String, ChannelLogHandler>();
	private final HashMap<String, ChannelLogHandler> mqttChannelHandlers = new HashMap<String, ChannelLogHandler>();

	protected void activateHttp() {
		logger.info("Activating Emoncms Logger Http Connection");
		try {
			String address = configs.getHttpAddress();
			
			// Start Http Connection
			try {
				int maxThreads = configs.getHttpMaxThreads();
				if (configs.hasAuthentication()) {
					String authorization = configs.getAuthorization();
					String authentication = configs.getAuthentication();
					
					httpConnection = HttpEmoncmsFactory.newAuthenticatedConnection(address, authorization, authentication, maxThreads);
				}
				else {
					httpConnection = HttpEmoncmsFactory.newConnection(address, maxThreads);
				}
				httpConnection.start();
			} catch (EmoncmsUnavailableException e) {
				logger.warn("Unable to connect to \"{}\"", address);
			}
		} catch (Exception e) {
			logger.error("Error while reading emoncms configuration: {}", e.getMessage());
		}		
	}
	
	protected void activateMqtt() {
		logger.info("Activating Emoncms Logger Mqtt Connection");
		try {
			String address = configs.getMqttAddress();
			
			// Start Mqtt Connection
			try {
				if (configs.hasUserName()) {
					String userName = configs.getUserName();
					String password = configs.getPassword();
					
					mqttConnection = MqttEmoncmsFactory.newAuthenticatedConnection(address, userName, password.toCharArray());
				}
				else {
					mqttConnection = MqttEmoncmsFactory.newConnection(address);
				}
				mqttConnection.start();
				if (!mqttConnection.isConnected()) {
					mqttConnection.stop();
					mqttConnection = null;
				}
				
			} catch (EmoncmsUnavailableException e) {
				logger.warn("Unable to connect to \"{}\"", address);
			}
		} catch (Exception e) {
			logger.error("Error while reading emoncms configuration: {}", e.getMessage());
		}
		
	}
	
	@Activate
	protected void activate(ComponentContext context) {
		logger.info("Activating Emoncms Logger");
		try {
			configs = new EmoncmsConfig();
			activateMqtt();
			if (mqttConnection != null) {
				isMqttConnectionType = true;
			}
			
		} catch (IOException e) {
			logger.error("Error while reading emoncms configuration: {}", e.getMessage());
		}
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		logger.info("Deactivating Emoncms Logger");
		if (httpConnection != null) {
			httpConnection.stop();
			httpConnection = null;
		}
		if (mqttConnection != null) {
			mqttConnection.stop();
			mqttConnection = null;
		}
	}

	@Override
	public String getId() {
		return "emoncms";
	}

	@Override
	public void setChannelsToLog(List<LogChannel> channels) {
		setChannelsToLogForMqtt(channels);

		setChannelsToLogForHttp(channels);
	}

	public void setChannelsToLogForHttp(List<LogChannel> channels) {
		// Will be called if OpenMUC receives new logging configurations
		for (ChannelLogHandler channel : httpChannelHandlers.values()) {
			if (channel instanceof ChannelAverageHandler) {
				dataAccessService.getChannel(channel.getId()).removeListener((ChannelAverageHandler) channel);
			}
		}
		httpChannelHandlers.clear();
		
		for (LogChannel channel : channels) {
			String id = channel.getId();
			
			ChannelLogSettings settings = new ChannelLogSettings(channel.getLoggingSettings());
			if (settings.isValid() && (!isMqttConnectionType || settings.hasFeedId())) {
				if (httpConnection == null) {
					activateHttp();
				}
				if (httpConnection == null) {
					logger.error("Requested to configure log Channels for deactivated Emoncms logger");
					return;
				}
				try {
					Input input = getHttpInput(settings.getNode(), id);
					if (input == null) {
						return;
					}
					
					ChannelLogHandler handler;
					if (settings.isAveraging()) {
						handler = new ChannelAverageHandler(id, input, settings);
                        if (dataAccessService.getAllIds().contains(id)) {
                            dataAccessService.getChannel(id).addListener((ChannelAverageHandler) handler);
                            ((ChannelAverageHandler) handler).setListening(true);
                        }
					}
					else if (settings.isDynamic()) {
						handler = new ChannelDynamicHandler(id, input, settings);
					}
					else {
						handler = new ChannelLogHandler(id, input, settings);
					}
					if (settings.hasFeedId()) {
						Feed feed = HttpFeed.connect(httpConnection, settings.getFeedId());
						Integer interval = settings.getInterval();
						if (interval == null && dataAccessService.getAllIds().contains(id)) {
							interval = dataAccessService.getChannel(id).getLoggingInterval()/1000;
                        }
						if (interval != null && interval > 0) {
    						handler.setInterval(interval);
						}
						handler.setFeed(feed);
					}
					httpChannelHandlers.put(id, handler);
							
				} catch (EmoncmsUnavailableException | EmoncmsSyntaxException e) {
					logger.warn("Unable to configure logging for Channel \"{}\": {}", id, e.getMessage());
				}
				
				if (logger.isTraceEnabled() && channel.getLoggingInterval() != null) {
					logger.trace("Channel \"{}\" configured to log every {}s", id, channel.getLoggingInterval()/1000);
				}
			}
			else if (settings.hasAuthorization()) {
				logger.warn("Unable to configure logging due to invalid syntax for Channel \"{}\": {}", channel.getId(), channel.getLoggingSettings());
			}
		}
	}

	public void setChannelsToLogForMqtt(List<LogChannel> channels) {
		// Will be called if OpenMUC receives new logging configurations
		for (ChannelLogHandler channel : mqttChannelHandlers.values()) {
			if (channel instanceof ChannelAverageHandler) {
				dataAccessService.getChannel(channel.getId()).removeListener((ChannelAverageHandler) channel);
			}
		}
		mqttChannelHandlers.clear();
		
		for (LogChannel channel : channels) {
			String id = channel.getId();
			
			ChannelLogSettings settings = new ChannelLogSettings(channel.getLoggingSettings());
			if (settings.isValid()) {
				if (mqttConnection == null) {
					logger.error("Requested to configure log Channels for deactivated Emoncms logger");
					return;
				}
				try {
					Input input = getMqttInput(settings.getNode(), id);
					if (input == null) {
						return;
					}
					
					ChannelLogHandler handler;
					if (settings.isAveraging()) {
						handler = new ChannelAverageHandler(id, input, settings);
                        if (dataAccessService.getAllIds().contains(id)) {
                            dataAccessService.getChannel(id).addListener((ChannelAverageHandler) handler);
                            ((ChannelAverageHandler) handler).setListening(true);
                        }
					}
					else if (settings.isDynamic()) {
						handler = new ChannelDynamicHandler(id, input, settings);
					}
					else {
						handler = new ChannelLogHandler(id, input, settings);
					}
					mqttChannelHandlers.put(id, handler);
							
				} catch (EmoncmsUnavailableException | EmoncmsSyntaxException e) {
					logger.warn("Unable to configure logging for Channel \"{}\": {}", id, e.getMessage());
				}
				
				if (logger.isTraceEnabled() && channel.getLoggingInterval() != null) {
					logger.trace("Channel \"{}\" configured to log every {}s", id, channel.getLoggingInterval()/1000);
				}
			}
			else if (settings.hasAuthorization()) {
				logger.warn("Unable to configure logging due to invalid syntax for Channel \"{}\": {}", channel.getId(), channel.getLoggingSettings());
			}
		}
	}

	@Override
	public void log(List<LogRecordContainer> containers, long timestamp) {
		Emoncms connection = getConnection();
		String type = isMqttConnectionType==true?EmoncmsConfig.MQTT_CON_TYPE:EmoncmsConfig.HTTP_CON_TYPE;
		logger.info("Emoncms Logger Log with " + type);
		if (connection == null) {
			logger.error("Requested to log values for deactivated Emoncms logger");
		}
		else if (containers == null || containers.isEmpty()) {
			logger.debug("Requested Emoncms logger to log an empty container list");
		}
		else if (containers.size() == 1) {
			LogRecordContainer container = containers.get(0);
			
			ChannelLogHandler channel = getChannel(container.getChannelId());
			if (isValid(container, isMqttConnectionType)) {
				try {
					Record record = container.getRecord();
					Long time = record.getTimestamp();
					if (time == null) {
						time = timestamp;
					}
					
					channel.post(time, record.getValue().asDouble());
					
				} catch (EmoncmsException | TypeConversionException e) {
					logger.warn("Failed to log record for channel \"{}\": {}", container.getChannelId(), e.getMessage());
				}
			}
		}
		else {
			List<DeviceDataList> devices = new ArrayList<DeviceDataList>();
			for (LogRecordContainer container : containers) {
				ChannelLogHandler channel = getChannel(container.getChannelId());
				if (isValid(container, isMqttConnectionType)) {
					try {
						Record record = container.getRecord();
						Long time = record.getTimestamp();
						if (time == null) {
							time = timestamp;
						}
						
						if (channel.update(time, record.getValue().asDouble())) {
							Namevalue namevalue = channel.getNamevalue();
							Authentication authenticator = channel.getAuthenticator();
							
							DeviceDataList device = null;
							for (DeviceDataList d : devices) {
								if (d.hasSameAuthentication(authenticator)) {
									device = d;
									break;
								}
							}
							if (device == null) {
								// No input collection for that device exists yet, so it needs to be created
								device = new DeviceDataList(authenticator);
								devices.add(device);
							}
							device.add(time, channel.getInput().getNode(), namevalue);
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
					if (device.size() == 1) {
						Data data = device.get(0);
						
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
							connection.post(device);
						}
						else {
							connection.post(device, device.getAuthenticator());
						}
					}
				} catch (EmoncmsException e) {
					logger.warn("Error logging values: {}", e.getMessage());
				}
			}
		}
	}

	private Input getHttpInput(String node, String id) throws EmoncmsUnavailableException {
		Input input = HttpInput.connect(httpConnection, node, id);
		return input;
	}
	
	private Input getMqttInput(String node, String id) throws EmoncmsUnavailableException {
		Input input = MqttInput.connect(mqttConnection, node, id);
		return input;
	}
	
	private Emoncms getConnection() {
		if (isMqttConnectionType) {
			return mqttConnection;
		}
		else {
			return httpConnection;
		}
	}
	
	private boolean isValid(LogRecordContainer container, boolean isMqtt) {
		HashMap<String, ChannelLogHandler> channelHandlers = isMqtt==true?mqttChannelHandlers:httpChannelHandlers;
		if (channelHandlers.containsKey(container.getChannelId())) {
		
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

	private ChannelLogHandler getChannel(String id) {
		ChannelLogHandler channel;
		if (isMqttConnectionType) {
			channel = mqttChannelHandlers.get(id);
		}
		else {
			channel = httpChannelHandlers.get(id);
		}
		if (channel.isAveraging()) {
			ChannelAverageHandler listener = (ChannelAverageHandler) channel;
			if (!listener.isListening() && dataAccessService.getAllIds().contains(id)) {
				dataAccessService.getChannel(id).addListener(listener);
				listener.setListening(true);
			}
		}
		return channel;
	}

	@Override
	public List<Record> getRecords(String id, long startTime, long endTime) throws IOException {
		ChannelLogHandler channel = httpChannelHandlers.get(id);
		
		Feed feed = channel.getFeed();
		if (feed == null) {
			throw new IOException("Unable to retrieve values for channel without configured feed: " + id);
		}
		try {
			List<Record> records = new LinkedList<Record>();
			List<Timevalue> data = feed.getData(startTime, endTime, channel.getSettings().getMaxInterval());
			for (Timevalue timevalue : data) {
				records.add(new Record(new DoubleValue(timevalue.getValue()), timevalue.getTime()));
			}
			return records;
			
		} catch (EmoncmsException e) {
			throw new IOException("Unable to retrieve values for channel " + id + ": " + e.getMessage());
		}
	}
}
