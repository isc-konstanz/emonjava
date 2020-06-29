/* 
 * Copyright 2016-20 ISC Konstanz
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
import org.emoncms.data.Authorization;
import org.emoncms.data.DataList;

public class DataContainer extends DataList {
	private static final long serialVersionUID = 1720223912529518324L;

	private final Authentication authenticator;

	public DataContainer(Authentication authenticator) {
		this.authenticator = authenticator;
	}

	public DataContainer() {
		this(new Authentication());
	}

	public Authentication getAuthenticator() {
		return authenticator;
	}

	public boolean equals(Authentication authenticator) {
		if (this.authenticator.getAuthorization() == Authorization.DEFAULT && 
				authenticator.getAuthorization() == Authorization.DEFAULT) {
			return true;
		}
		else if (this.authenticator.equals(authenticator)) {
			return true;
		}
		return false;
	}
}
