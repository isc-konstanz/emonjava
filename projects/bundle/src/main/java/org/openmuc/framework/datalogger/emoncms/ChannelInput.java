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

import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.data.Timevalue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;

public class ChannelInput {

	private final Input input;
	private final String authenticator;

	public ChannelInput(Input input, String key) {
		this.input = input;
		this.authenticator = key;
	}

	public Input getInput() {
		return input;
	}

	public String getAuthenticator() {
		return authenticator;
	}
	
	public boolean post(Record record) throws EmoncmsException, TypeConversionException {

		if (record != null && record.getValue() != null) {
			Timevalue timevalue = new Timevalue(record.getTimestamp(), record.getValue().asDouble());
			if (authenticator != null) {
				input.post(authenticator, timevalue);
			}
			else {
				input.post(timevalue);
			}
			return true;
		}
		else return false;
	}
}