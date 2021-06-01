/* 
 * Copyright 2016-21 ISC Konstanz
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
package org.emoncms.data;

public class Authentication {

	private final Authorization authorization;
	private final String key;

	public Authentication(Authorization authorization, String key) {
		this.authorization = authorization;
		this.key = key;
	}

	public Authentication(String key) {
		this(Authorization.WRITE, key);
	}

	public Authentication() {
		this(Authorization.DEFAULT, null);
	}

	public Authorization getAuthorization() {
		return authorization;
	}

	public boolean isDefault() {
		if (authorization == Authorization.DEFAULT) {
			return true;
		}
		return false;
	}

	public String getKey() {
		return key;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Authentication) {
			Authentication authentication = (Authentication) object;
			if (authentication.getAuthorization() == authorization &&
					authentication.getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		switch (authorization) {
		case NONE:
			return "None";
		case DEFAULT:
			return "Default";
		default:
			return authorization.getValue() + "=" + key;
		}
	}
}
