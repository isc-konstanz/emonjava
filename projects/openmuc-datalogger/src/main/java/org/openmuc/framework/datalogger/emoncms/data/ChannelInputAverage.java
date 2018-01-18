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
package org.openmuc.framework.datalogger.emoncms.data;

import org.emoncms.Input;
import org.emoncms.com.EmoncmsSyntaxException;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelInputAverage extends ChannelInputDynamic implements RecordListener {
	private final static Logger logger = LoggerFactory.getLogger(ChannelInputAverage.class);

	protected volatile Double valueSum = 0.0;
	protected volatile int valueCount = 0;

	protected boolean listening = false;

	public ChannelInputAverage(String id, Input input, ChannelLogSettings settings) throws EmoncmsSyntaxException {
		super(id, input, settings);
	}

	public boolean isListening() {
		return listening;
	}

	public void setListening(boolean listening) {
		this.listening = listening;
	}

	@Override
	public double getValue() {
		if (valueCount > 1) {
			synchronized (valueSum) {
				double average = valueSum/valueCount;
				logger.trace("Average of {} values for channel \"{}\": {}", valueCount, id, average);
				
				valueSum = 0.0;
				valueCount = 0;
				return average;
			}
		}
		return value;
	}

	@Override
	public void newRecord(Record record) {
		if (record != null) {
			if (record.getFlag() == Flag.VALID && record.getValue() != null) {
				
				if (logger.isTraceEnabled()) {
					logger.trace("Listener received new record for channel \"{}\": {}", 
							id, record.toString());
				}
				Long time = record.getTimestamp();
				if (this.time == null || this.time < time) {
					synchronized (valueSum) {
						valueSum += record.getValue().asDouble();
						valueCount++;
					}
				}
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Listener received invalid or empty value for channel \"{}\": {}",
						id, record.getFlag().toString());
			}
		}
		else if (logger.isTraceEnabled()) {
			logger.trace("Failed to log an empty record for channel \"{}\"", id);
		}
	}
}
