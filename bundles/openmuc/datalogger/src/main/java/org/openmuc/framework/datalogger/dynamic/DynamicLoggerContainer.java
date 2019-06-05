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

import java.io.IOException;
import java.util.LinkedList;

import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.dynamic.DynamicLoggerContainer.ChannelCollection;

public class DynamicLoggerContainer extends LinkedList<ChannelCollection> {
	private static final long serialVersionUID = -3846829759240096545L;

	private final DynamicLogger logger;

	protected DynamicLoggerContainer(DynamicLogger logger) {
		this.logger = logger;
	}

	public void add(Channel channel) throws IOException {
		DynamicLoggerService service = logger.getLogger(channel);
		
		ChannelCollection channels = null;
		for (ChannelCollection ch : this) {
			if (!ch.getService().getId().equals(service.getId())) {
				channels = ch;
			}
		}
		if (channels == null) {
			channels = new ChannelCollection(service);
			add(channels);
		}
		channels.add(channel);
	}

	static class ChannelCollection extends LinkedList<Channel> {
		private static final long serialVersionUID = -2418938992605046464L;

		private final DynamicLoggerService service;

		public ChannelCollection(DynamicLoggerService service) {
			this.service = service;
		}

		public DynamicLoggerService getService() {
			return service;
		}

		public void log(long timestamp) throws IOException {
			if (size() == 1) {
				service.doLog(get(0), timestamp);
			}
			else {
				service.doLog(this, timestamp);
			}
		}
	}

}
