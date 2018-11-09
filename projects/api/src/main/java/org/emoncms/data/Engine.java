/* 
 * Copyright 2016-18 ISC Konstanz
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


public enum Engine {

	MYSQL(0), 
	PHPTIMESERIES(2), 
	PHPFINA(5), 
	PHPFIWA(6), 
	VIRTUALFEED(7), 
	MYSQLMEMORY(8), 
	REDISBUFFER(9);

	private final int id;

	private Engine(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	public static Engine getEnum(int id) {
		switch (id) {
		case 0:
			return MYSQL;
		case 2:
			return PHPTIMESERIES;
		case 5:
			return PHPFINA;
		case 6:
			return PHPFIWA;
		case 7:
			return VIRTUALFEED;
		case 8:
			return MYSQLMEMORY;
		case 9:
			return REDISBUFFER;
		default:
			throw new IllegalArgumentException("Unknown engine id: " + id);
		}
	}
}
