/* 
 * Copyright 2016-18 ISC Konstanz
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
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsSyntaxException;
import org.emoncms.data.Authentication;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;

public class ChannelInput {

	protected final String id;
	protected final Input input;
	protected final Authentication authenticator;
	protected final ChannelLogSettings settings;

	protected Long time = null;
	protected Double value = null;

	public ChannelInput(String id, Input input, ChannelLogSettings settings) throws EmoncmsSyntaxException {
		this.id = id;
		this.input = input;
		this.settings = settings;
		this.authenticator = settings.getAuthentication();
	}

	public String getId() {
		return id;
	}

	public Input getInput() {
		return input;
	}

	public Authentication getAuthenticator() {
		return authenticator;
	}

	public ChannelLogSettings getSettings() {
		return settings;
	}

	public boolean isAveraging() {
		return settings.isAveraging();
	}

	public boolean update(long time, double value) {
		if (this.time != null && this.time >= time) {
			return false;
		}
		this.time = time;
		this.value = value;
		
		return true;
	}

	public double getValue() {
		return value;
	}

	public Namevalue getNamevalue() {
		return new Namevalue(id, getValue());
	}

	public Timevalue getTimevalue() {
		return new Timevalue(time, getValue());
	}

	public void post(long time, double value) throws EmoncmsException {
		if (update(time, value)) {
			Timevalue postValue = getTimevalue();
			
			if (authenticator.isDefault()) {
				input.post(postValue);
			}
			else {
				input.post(postValue, authenticator);
			}
		}
	}
}
