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
		channelInputsById.clear();
		
		if (cms != null) synchronized (cms) {
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
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Channel \"{}\" configured to log every {}s", channel.getId(), channel.getLoggingInterval()/1000);
				}
			}
		}
	}

	@Override
	public synchronized void log(List<LogRecordContainer> containers, long timestamp) {

		if (cms != null) synchronized (cms) {
			if (containers.size() > 1) {
				LogRecordContainer container = containers.get(0);

				if (logger.isTraceEnabled()) {
					logger.trace("Attempting to log record for channel \"{}\": {}", container.getChannelId(), container.getRecord());
				}
				try {
					if (channelInputsById.containsKey(container.getChannelId())) {
						channelInputsById.get(container.getChannelId()).post(container.getRecord());
					}
					
				} catch (EmoncmsException | TypeConversionException e) {
					logger.debug("Failed to log record for channel \"{}\": {}", container.getChannelId(), e.getMessage());
				}
			}
			else {
				// Check, if several channels can be posted for the same device at once
				List<DeviceValuesCollection> devices = new ArrayList<DeviceValuesCollection>();
				for (LogRecordContainer container : containers) {
					if (channelInputsById.containsKey(container.getChannelId())) {
						if (logger.isTraceEnabled()) {
							logger.trace("Preparing record to log for channel \"{}\": {}", container.getChannelId(), container.getRecord());
						}
						ChannelInput channel = channelInputsById.get(container.getChannelId());
						if (container.getRecord() != null && container.getRecord().getValue() != null) {
							try {
								Record record = container.getRecord();
								Namevalue value = new Namevalue(container.getChannelId(), record.getValue().asDouble());
								
								long time = timestamp;
								if (container.getRecord().getTimestamp() != null) {
									time = container.getRecord().getTimestamp();
								}
								
								DeviceValuesCollection device = null;
								for (DeviceValuesCollection d : devices) {
									if (d.getNode().equals(channel.getInput().getNode()) && 
											d.getAuthenticator().equals(channel.getAuthenticator()) &&
											d.getTimestamp() == time) {
										
										d.add(value);
										device = d;
										break;
									}
								}
								if (device == null) {
									// No input collection for that device exists yet, so it needs to be created
									device = new DeviceValuesCollection(channel.getInput().getNode(), channel.getAuthenticator(), time);
									device.add(value);
									devices.add(device);
								}
							} catch (TypeConversionException e) {
								logger.debug("Failed to prepare record to log to Channel \"{}\": {}", container.getChannelId(), e.getMessage());
							}
						}
						else {
							logger.debug("Preparing to log an empty record for channel \"{}\" was skipped", container.getChannelId());
						}
					}
					else {
						logger.debug("Logging for channel \"{}\" not prepared yet", container.getChannelId());
					}
				}
				
				for (DeviceValuesCollection device : devices) {

					if (logger.isTraceEnabled()) {
						logger.trace("Attempting to log values for {} channels at device node \"{}\"", device.size(), device.getNode());
					}
					try {
						cms.post(device.getNode(), device.getTimestamp(), device, device.getAuthenticator());
						
					} catch (EmoncmsException e) {
						logger.debug("Failed to log values for device node \"{}\": {}", device.getNode(), e.getMessage());
					}
				}
			}
		}
	}

	@Override
	public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
		
		// TODO: fetch feed id and retrieve data from web server
		throw new UnsupportedOperationException();
	}
}
