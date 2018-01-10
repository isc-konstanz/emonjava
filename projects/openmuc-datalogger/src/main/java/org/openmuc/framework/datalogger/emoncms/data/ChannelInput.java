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
import org.emoncms.com.EmoncmsSyntaxException;
import org.emoncms.data.Authentication;
import org.emoncms.data.Timevalue;

public class ChannelInput {

	protected final String id;
	protected final Input input;
	protected final ChannelLogSettings settings;
	protected final Authentication authenticator;

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

	public ChannelLogSettings getSettings() {
		return settings;
	}

	public boolean isDynamic() {
		return settings.isDynamic();
	}

	public boolean isAveraging() {
		return settings.isAveraging();
	}

	public Authentication getAuthenticator() {
		return authenticator;
	}

	public void post(Timevalue timevalue) throws EmoncmsException {
		if (authenticator.isDefault()) {
			input.post(timevalue);
		}
		else {
			input.post(timevalue, authenticator);
		}
	}
}
