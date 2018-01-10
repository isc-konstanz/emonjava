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
import org.openmuc.framework.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelInputDynamic extends ChannelInput {
	private final static Logger logger = LoggerFactory.getLogger(ChannelInputDynamic.class);

	protected volatile Long lastTime = null;
	protected volatile Double lastValue = null;

	public ChannelInputDynamic(String id, Input input, ChannelLogSettings settings) throws EmoncmsSyntaxException {
		super(id, input, settings);
	}

	public boolean isUpdated(Record record) {
		Double value = record.getValue().asDouble();
		if (lastTime != null) {
			boolean updated = true;
			
			Double tolerance = settings.getTolerance();
			if (tolerance != null && tolerance > Math.abs(value - lastValue)) {
				updated = false;
			}
			else if (value == lastValue) {
				updated = false;
			}
			if (!updated && record.getTimestamp() - lastTime < settings.getMaxInterval()) {
				logger.trace("Channel \"{}\" not updated with value: {}", id, value);
				return false;
			}
		}
		
		lastTime = record.getTimestamp();
		lastValue = value;
		return true;
	}
}
