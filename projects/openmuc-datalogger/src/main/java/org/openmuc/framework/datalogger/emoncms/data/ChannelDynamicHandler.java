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
package org.openmuc.framework.datalogger.emoncms.data;

import org.emoncms.Input;
import org.emoncms.com.EmoncmsSyntaxException;

public class ChannelDynamicHandler extends ChannelLogHandler {

	public ChannelDynamicHandler(String id, Input input, ChannelLogSettings settings) throws EmoncmsSyntaxException {
		super(id, input, settings);
	}

	@Override
	public boolean update(long time, double value) {
		if (this.time != null && this.time >= time) {
			return false;
		}
		else if (this.value != null && settings.getMaxInterval() != null) {
			double tolerance = settings.getTolerance();
			double delta = Math.abs(value - this.value);
			if (tolerance >= delta && (time - this.time) <= settings.getMaxInterval()) {
				return false;
			}
		}
		this.time = time;
		this.value = value;
		
		return true;
	}
}
