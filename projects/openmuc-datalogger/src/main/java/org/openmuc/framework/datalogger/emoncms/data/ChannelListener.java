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
import org.emoncms.com.EmoncmsException;
import org.emoncms.data.Authentication;
import org.emoncms.data.Timevalue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.RecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelListener extends ChannelInput implements RecordListener {
	private final static Logger logger = LoggerFactory.getLogger(ChannelListener.class);

	public ChannelListener(String id, Input input, Authentication authenticator) {
		super(id, input, authenticator);
	}


	@Override
	public void newRecord(Record record) {
		if (record != null) {
			if (record.getFlag() == Flag.VALID && record.getValue() != null) {
				
				if (logger.isTraceEnabled()) {
					logger.trace("Listener received new record to log for channel \"{}\": {}", 
							id, record.toString());
				}
				try {
					Long time = record.getTimestamp();
					if (time == null) {
						time = System.currentTimeMillis();
					}
					Timevalue timevalue = new Timevalue(time, record.getValue().asDouble());
					post(timevalue);
					
				} catch (EmoncmsException e) {
					logger.warn("Failed to log record for channel \"{}\": {}", id, e.getMessage());
				}
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Skipped logging an invalid or empty value for channel \"{}\": {}",
						id, record.getFlag().toString());
			}
		}
		else if (logger.isTraceEnabled()) {
			logger.trace("Failed to log an empty record for channel \"{}\"", id);
		}
	}
}
