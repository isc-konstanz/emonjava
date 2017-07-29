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

import org.emoncms.com.EmoncmsException;
import org.emoncms.data.Timevalue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelListener implements RecordListener {
	private final static Logger logger = LoggerFactory.getLogger(ChannelListener.class);

	private final ChannelInput channel;

	public ChannelListener(ChannelInput channel) {
		this.channel = channel;
	}

	@Override
	public void newRecord(Record record) {
		try {
			Long time = record.getTimestamp();
			Timevalue timevalue = new Timevalue(time, record.getValue().asDouble());
			
			channel.post(timevalue);
			
		} catch (EmoncmsException e) {
			logger.warn("Failed to log record for channel \"{}\": {}", channel.getInput().getName(), e.getMessage());
		}
	}
}
