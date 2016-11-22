package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoncms.Emoncms;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.HttpEmoncmsFactory;
import org.emoncms.com.http.HttpInput;
import org.emoncms.com.http.request.HttpRequestCallbacks;
import org.emoncms.data.Timevalue;
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

	private final static String INPUTID = "inputid";
	private final static String NODE = "nodeid";
	private final static String APIKEY = "apikey";

	private final HashMap<String, ChannelInputContainer> channelInputsById = new HashMap<String, ChannelInputContainer>();

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
		
		// TODO add log interval groups and post List of Namevalues
		for (LogChannel channel : channels) {
			Map<String, String> settings = new HashMap<String, String>();
			
			String settingStr = channel.getLoggingSettings();
			if (settingStr != null && !settingStr.isEmpty()) {
				String[] parameters = settingStr.split(",");
				for (String parameter : parameters) {
					String[] keyValue = parameter.split(":");
					settings.put(keyValue[0], keyValue[1]);
				}
			}
			
			Input input = null;
			if (cms != null && settings.containsKey(INPUTID) && settings.containsKey(NODE)) {
				int id = Integer.valueOf(settings.get(INPUTID));
				input = new HttpInput((HttpRequestCallbacks) cms, id, settings.get(NODE), channel.getId());
			}
			
			if (input != null && settings.containsKey(APIKEY)) {
				ChannelInputContainer container = new ChannelInputContainer(input, settings.get(APIKEY));
				
				channelInputsById.put(channel.getId(), container);
			}
			else {
				logger.warn("Unable to configure logging for Channel \"{}\"", channel.getId());
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Channel \"{}\"configured to log every {}s", channel.getId(), channel.getLoggingInterval()/1000);
			}
		}
	}

	@Override
	public synchronized void log(List<LogRecordContainer> containers, long timestamp) {
		
		for (LogRecordContainer container : containers) {
			if (channelInputsById.containsKey(container.getChannelId())) {
				try {
					ChannelInputContainer channel = channelInputsById.get(container.getChannelId());
					Record record = container.getRecord();
					
					if (logger.isTraceEnabled()) {
						logger.trace("Logging to Channel \"{}\": {}", container.getChannelId(), container.getRecord());
					}
					if (record != null && record.getValue() != null) {
						Timevalue timevalue = new Timevalue(record.getTimestamp(), record.getValue().asDouble());
						channel.getInput().post(channel.getAuthenticator(), timevalue);
					}
				} catch (EmoncmsException | TypeConversionException e) {
					logger.debug("Failed to log record for Channel \"{}\": {}", container.getChannelId(), e.getMessage());
				}
			}
			else {
				logger.debug("Unable to log record for Channel \"{}\"", container.getChannelId());
			}
		}
	}

	@Override
	public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
		
		// TODO: fetch feed id and retrieve data from web server
		throw new UnsupportedOperationException();
	}
}
