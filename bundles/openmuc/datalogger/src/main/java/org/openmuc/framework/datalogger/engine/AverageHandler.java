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
package org.openmuc.framework.datalogger.engine;

import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.RecordListener;
import org.openmuc.framework.datalogger.data.Settings;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AverageHandler extends DynamicHandler implements RecordListener {
	private final static Logger logger = LoggerFactory.getLogger(AverageHandler.class);

	protected volatile Long lastTime = null;
	protected volatile Double valueSum = 0.0;
	protected volatile int valueCount = 0;

	protected Boolean listening = false;

	protected AverageHandler(LogChannel channel, Settings settings) {
		super(channel, settings);
	}

	public boolean isListening() {
		return listening;
	}

	public void setListening(boolean listening) {
		this.listening = listening;
	}

	@Override
	public boolean update(Record update) {
		if (isUpdate(update)) {
			synchronized (listening) {
				if (valueCount > 1) {
					double average = valueSum/valueCount;
					logger.trace("Average of {} values for channel \"{}\": {}", valueCount, id, average);
					
					valueSum = 0.0;
					valueCount = 0;
					record = new Record(new DoubleValue(average), update.getTimestamp());
					return true;
				}
			}
			record = update;
			return true;
		}
		return false;
	}

	@Override
	public void newRecord(Record record) {
		if (record == null) {
			logger.trace("Failed to log an empty record for channel \"{}\"", id);
			return;
		}
		if (record.getFlag() != Flag.VALID) {
			logger.debug("Listener received invalid or empty value for channel \"{}\": {}",
					id, record.getFlag().toString());
		}
		logger.trace("Listener received new record for channel \"{}\": {}", 
				id, record.toString());
		
		Long time = record.getTimestamp();
		if (this.lastTime == null || this.lastTime < time) {
			synchronized (listening) {
				valueSum += record.getValue().asDouble();
				valueCount++;
			}
			lastTime = time;
		}
	}
}
