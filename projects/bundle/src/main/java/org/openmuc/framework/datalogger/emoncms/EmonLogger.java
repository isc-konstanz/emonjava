/*
 * Copyright 2016-17 ISC Konstanz
 *
 * This file is part of emonjava.
 * For more information visit https://bitbucket.org/isc-konstanz/emonjava
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
 *
 */
package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.HttpEmoncmsFactory;
import org.emoncms.com.http.HttpInput;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class EmonLogger implements DataLoggerService {

	private final static Logger logger = LoggerFactory.getLogger(EmonLogger.class);

	private final HashMap<String, ChannelInput> channelInputsById = new HashMap<String, ChannelInput>();

	private Emoncms cms = null;

	protected void activate(ComponentContext context) {
		
		logger.info("Activating Emoncms Logger");

		String url = System.getProperty("org.openmuc.framework.datalogger.emoncms.url");
		try {
			if (url != null) {
				String maxThreadsProperty = System.getProperty("org.openmuc.framework.datalogger.emoncms.maxThreads");
				if (maxThreadsProperty != null) {
					int maxThreads = Integer.parseInt(maxThreadsProperty);
					cms = HttpEmoncmsFactory.newHttpEmoncmsConnection(url, maxThreads);
					logger.debug("Connecting to emoncms web server at \"{}\" with a maximum amount of {} threads synchronously", url, maxThreads);
				}
				else {
					cms = HttpEmoncmsFactory.newHttpEmoncmsConnection(url);
					logger.debug("Connecting to emoncms web server at \"{}\"", url);
				}
			}
			else {
				cms = HttpEmoncmsFactory.newHttpEmoncmsConnection();
				logger.debug("Connecting to emoncms web server running on localhost");
			}
			cms.start();
			
		} catch (EmoncmsUnavailableException e) {
			logger.warn("Unable to connect to \"{}\"", url);
		}
	}

	protected void deactivate(ComponentContext context) {
		
		logger.info("Deactivating Emoncms Logger");
		if (cms != null) {
			cms.stop();
			cms = null;
		}
	}

	@Override
	public String getId() {
		return "emoncms";
	}

	@Override
	public void setChannelsToLog(List<LogChannel> channels) {
		
		// Will be called if OpenMUC starts the logger
		if (cms == null) {
			logger.error("Requested to configure log Channels for deactivated Emoncms logger");
			return;
		}
		channelInputsById.clear();
		
		for (LogChannel channel : channels) {
			SettingsHelper settings = new SettingsHelper(channel.getLoggingSettings());
			
			if (settings.isValid()) {
				try {
					Input input = HttpInput.connect(cms, settings.getInputId(), settings.getNode(), channel.getId());
					ChannelInput container = new ChannelInput(input, settings.getApiKey());
					
					channelInputsById.put(channel.getId(), container);
					
				} catch (EmoncmsUnavailableException e) {
					logger.warn("Unable to configure logging for Channel \"{}\": {}", channel.getId(), e.getMessage());
				}

				if (logger.isTraceEnabled()) {
					logger.trace("Channel \"{}\" configured to log every {}s", channel.getId(), channel.getLoggingInterval()/1000);
				}
			}
			else logger.warn("Unable to configure invalid syntax for logging Channel \"{}\"", channel.getId());
		}
	}

	@Override
	public synchronized void log(List<LogRecordContainer> containers, long timestamp) {

		if (cms == null) {
			logger.error("Requested to log values for deactivated Emoncms logger");
		}
		else if (containers == null || containers.isEmpty()) {
			logger.debug("Requested Emoncms logger to log an empty container list");
		}
		else if (containers.size() == 1) {
			
			LogRecordContainer container = containers.get(0);
			if (isValid(container)) {
				ChannelInput channel = channelInputsById.get(container.getChannelId());
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
					ChannelInput channel = channelInputsById.get(container.getChannelId());
					try {
						Record record = container.getRecord();
						Long time = record.getTimestamp();
						if (time == null) {
							time = timestamp;
						}
						Namevalue value = new Namevalue(container.getChannelId(), record.getValue().asDouble());
						
						DeviceDataList device = null;
						for (DeviceDataList d : devices) {
							if (d.getAuthenticator().equals(channel.getAuthenticator())) {
								
								d.add(time, channel.getInput().getNode(), value);
								device = d;
								break;
							}
						}
						if (device == null) {
							// No input collection for that device exists yet, so it needs to be created
							device = new DeviceDataList(channel.getAuthenticator());
							device.add(time, channel.getInput().getNode(), value);
							
							devices.add(device);
						}
						
					} catch (TypeConversionException e) {
						logger.warn("Failed to prepare record to log to Channel \"{}\": {}", container.getChannelId(), e.getMessage());
					}
				}
			}
			
			for (DeviceDataList device : devices) {

				if (logger.isTraceEnabled()) {
					logger.trace("Attempting to log {} values for key \"{}\"", device.size(), device.getAuthenticator());
				}
				try {
					cms.post(device, device.getAuthenticator());
					
				} catch (EmoncmsException e) {
					logger.warn("Failed to log values for key \"{}\": {}", device.getAuthenticator(), e.getMessage());
				}
			}
		}
	}

	private boolean isValid(LogRecordContainer container) {
		
		if (channelInputsById.containsKey(container.getChannelId())) {
		
			if (container.getRecord() != null) {
				if (container.getRecord().getFlag() == Flag.VALID && container.getRecord().getValue() != null) {
					
					if (logger.isTraceEnabled()) {
						logger.trace("Preparing record to log for channel \"{}\": {}", container.getChannelId(), container.getRecord());
					}
					return true;
				}
				else logger.debug("Skipped logging an invalid or empty value for channel \"{}\": {}",
						container.getChannelId(), container.getRecord().getFlag().toString());
			}
			else logger.debug("Failed to log an empty record for channel \"{}\"", container.getChannelId());
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
