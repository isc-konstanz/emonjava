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


public enum ProcessArg {

	VALUE(0), 
	INPUTID(1), 
	FEEDID(2), 
	NONE(3), 
	TEXT(4), 
	SCHEDULEID(5);

	private final int arg;

	private ProcessArg(int arg) {
		this.arg = arg;
	}

	public int getValue() {
		return arg;
	}

	public static ProcessArg getEnum(int arg) {
		switch (arg) {
		case 0:
			return VALUE;
		case 1:
			return INPUTID;
		case 2:
			return FEEDID;
		case 3:
			return NONE;
		case 4:
			return TEXT;
		case 5:
			return SCHEDULEID;
		default:
			throw new IllegalArgumentException("Unknown process argument: " + arg);
		}
	}
}
