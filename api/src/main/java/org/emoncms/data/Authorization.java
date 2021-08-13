/* 
 * Copyright 2016-2021 ISC Konstanz
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

public enum Authorization {

	DEFAULT,
	DEVICE,
	WRITE,
	READ,
	NONE;

	public String getValue() {
		switch (this) {
		case DEVICE:
			return "devicekey";
		case WRITE:
		case READ:
			return "apikey";
		default:
			throw new UnsupportedOperationException("No authorization value for type: " + this.name());
		}
	}
}
