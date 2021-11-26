/* 
 * Copyright 2016-2021 ISC Konstanz
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
package org.openmuc.framework.datalogger.emoncms.http.data;

import org.emoncms.data.Authentication;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;

public class Device extends DataContainer {
	private static final long serialVersionUID = -7256569692637391935L;

	private final String node;

	public Device(String node, Authentication authenticator) {
		super(authenticator);
		this.node = node;
	}

	public Device(String node) {
		this(node, new Authentication());
	}

	public String getNode() {
		return node;
	}

	public boolean add(Long time, Namevalue namevalue) {
		return super.add(time, node, namevalue);
	}

	@Override
	@Deprecated
	public boolean add(Long time, String node, Namevalue namevalue) {
		return false;
	}

	@Override
	@Deprecated
	public boolean add(String node, String name, Timevalue timevalue) {
		return false;
	}

}
