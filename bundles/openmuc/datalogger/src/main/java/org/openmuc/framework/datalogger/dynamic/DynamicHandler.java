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

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.data.Settings;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicHandler extends ChannelHandler {
	private final static Logger logger = LoggerFactory.getLogger(DynamicHandler.class);

	protected DynamicHandler(LogChannel channel, Settings settings) {
		super(channel, settings);
	}

	@Override
	public boolean isUpdate(Record update) {
		if (record == null) {
			return true;
		}
		if (record.getFlag() != update.getFlag()) {
			return true;
		}
		else if (Flag.VALID != update.getFlag()) {
			logger.trace("Skipped logging value for unchanged flag: {}", update.getFlag());
			return false;
		}
		if (record.getTimestamp() >= update.getTimestamp()) {
			logger.trace("Skipped logging value with invalid timestamp: {}", update.getTimestamp());
			return false;
		}
		else {
			switch(type) {
			case INTEGER:
			case SHORT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				double tolerance = settings.getTolerance();
				double delta = Math.abs(update.getValue().asDouble() - record.getValue().asDouble());
				if (tolerance >= delta && (update.getTimestamp() - record.getTimestamp()) < settings.getIntervalMax()) {
					if (logger.isTraceEnabled()) {
						logger.trace("Skipped logging value inside tolerance: {} -> {} <= {}",
								record.getValue().asDouble(), update.getValue(), tolerance);
					}
					return false;
				}
			default:
				break;
			}
		}
		return true;
	}
}
