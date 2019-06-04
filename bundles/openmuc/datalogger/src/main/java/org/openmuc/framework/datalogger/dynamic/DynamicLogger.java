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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsException;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.dataaccess.Channel;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DynamicLogger implements DataLoggerService {
	private final static Logger logger = LoggerFactory.getLogger(DynamicLogger.class);

	protected final Map<String, LogHandler> handlers = new HashMap<String, LogHandler>();

	@Reference
	private DataAccessService dataAccess;

	@Override
	public void setChannelsToLog(List<org.openmuc.framework.datalogger.spi.LogChannel> channels) {
		// Will be called if OpenMUC receives new logging configurations
		for (LogHandler handler : handlers.values()) {
			if (handler instanceof AverageHandler) {
				Channel channel = dataAccess.getChannel(handler.getId());
				if (channel != null) {
					channel.removeListener((AverageHandler) channel);
				}
			}
		}
		handlers.clear();
		
		for (org.openmuc.framework.datalogger.spi.LogChannel channel : channels) {
			String id = channel.getId();
			
			LogSettings settings = new LogSettings(channel);
			LogHandler handler;
			if (settings.isAveraging()) {
				handler = new AverageHandler(channel, settings);
				if (dataAccess.getAllIds().contains(id)) {
					dataAccess.getChannel(id).addListener((AverageHandler) handler);
					((AverageHandler) handler).setListening(true);
				}
			}
			else if (settings.isDynamic()) {
				handler = new DynamicHandler(channel, settings);
			}
			else {
				handler = new LogHandler(channel, settings);
			}
			this.handlers.put(id, handler);
			
			if (logger.isTraceEnabled() && channel.getLoggingInterval() != null) {
				logger.trace("Channel \"{}\" configured to log every {}s", id, channel.getLoggingInterval()/1000);
			}
		}
	}

	@Override
	public void log(List<LogRecordContainer> containers, long timestamp) {
		if (containers == null || containers.isEmpty()) {
			logger.debug("Requested Emoncms logger to log an empty container list");
			return;
		}
		List<LogChannel> channels = new ArrayList<LogChannel>();
		for (LogRecordContainer container : containers) {
			if (!handlers.containsKey(container.getChannelId())) {
				logger.warn("Failed to log record for unconfigured channel \"{}\"", container.getChannelId());
				continue;
			}
			try {
				LogHandler handler = getHandler(container.getChannelId());
				if (handler.update(container.getRecord())) {
					channels.add(handler);
				}
			} catch (TypeConversionException e) {
				logger.warn("Failed to prepare record to log to Channel \"{}\": {}", container.getChannelId(), e.getMessage());
			}
		}
		try {
			logChannels(channels, timestamp);
			
		} catch(EmoncmsException e) {
			logger.warn("Failed to log values: {}", e.getMessage());
			
		} catch(Exception e) {
			logger.warn("Error while logging values :{}", e);
		}
	}

	protected abstract void logChannels(List<LogChannel> channels, long timestamp) throws EmoncmsException;

	private LogHandler getHandler(String id) {
		LogHandler handler = handlers.get(id);	
		if (handler.isAveraging()) {
			AverageHandler listener = (AverageHandler) handler;
			if (!listener.isListening() && dataAccess.getAllIds().contains(id)) {
				listener.setListening(true);
				dataAccess.getChannel(id).addListener(listener);
			}
		}
		return handler;
	}

}
