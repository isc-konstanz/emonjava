/* 
 * Copyright 2016-20 ISC Konstanz
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


public enum Datatype {

	UNDEFINED(0), 
	REALTIME(1), 
	DAILY(2), 
	HISTOGRAM(3);

	private final int id;

	private Datatype(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	public static Datatype getEnum(int id) {
		switch (id) {
		case 0:
			return UNDEFINED;
		case 1:
			return REALTIME;
		case 2:
			return DAILY;
		case 3:
			return HISTOGRAM;
		default:
			throw new IllegalArgumentException("Unknown datatype id: " + id);
		}
	}
}
