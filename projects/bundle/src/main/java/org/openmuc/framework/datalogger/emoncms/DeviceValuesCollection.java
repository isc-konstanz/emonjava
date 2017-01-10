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

import java.util.ArrayList;

import org.emoncms.data.Namevalue;

public class DeviceValuesCollection extends ArrayList<Namevalue> {
	private static final long serialVersionUID = 1720223912529518324L;

	private final String node;
	private final String authenticator;

	private final long timestamp;

	public DeviceValuesCollection(String node, String key, long timestamp) {
		this.node = node;
		this.authenticator = key;
		this.timestamp = timestamp;
	}

	public String getNode() {
		return node;
	}

	public String getAuthenticator() {
		return authenticator;
	}

	public long getTimestamp() {
		return timestamp;
	}
}