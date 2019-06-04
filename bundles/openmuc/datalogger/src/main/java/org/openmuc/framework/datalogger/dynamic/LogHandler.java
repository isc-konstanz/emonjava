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

import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;

public class LogHandler extends org.openmuc.framework.datalogger.dynamic.LogChannel {

	protected LogHandler(LogChannel channel, LogSettings settings) {
		super(channel, settings);
	}

	protected boolean isUpdate(Record update) {
		if (record == null) {
			return true;
		}
		if (record.getFlag() != update.getFlag()) {
			return true;
		}
		else if (Flag.VALID != update.getFlag()) {
			return false;
		}
		if (record.getTimestamp() <= update.getTimestamp()) {
			return false;
		}
		return true;
	}

	public boolean update(Record update) {
		if (isUpdate(update)) {
			record = update;
			return true;
		}
		return false;
	}

}
