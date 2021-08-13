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


public enum Field {

	INPUTID("inputid"), 
	NODE("nodeid"), 
	NAME("name"),
	TAG("tag"), 
	DESCRIPTION("description"),
	TIME("time"), 
	VALUE("value"), 
	PUBLIC("public"), 
	SIZE("size"), 
	DATATYPE("datatype"), 
	ENGINE("engine"), 
	PROCESSES("processList"),
	DISABLED("disabled"), 
	ADDRESS("address"), 
	PASSWORD("password"), 
	DEVICEKEY("devicekey"), 
	TYPE("type"), 
	DRIVERS("drivers"), 
	DEVICES("devices"), 
	CHANNELS("channels"), 
	CONFIG("config");

	private final String value;

	private Field(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Field getEnum(String field) {
		switch (field) {
		case "nodeid":
			return NODE;
		case "name":
			return NAME;
		case "tag":
			return TAG;
		case "description":
			return DESCRIPTION;
		case "time":
			return TIME;
		case "value":
			return VALUE;
		case "public":
			return PUBLIC;
		case "size":
			return SIZE;
		case "datatype":
			return DATATYPE;
		case "engine":
			return ENGINE;
		case "processList":
			return PROCESSES;
		case "disabled":
			return DISABLED;
		case "address":
			return ADDRESS;
		case "password":
			return PASSWORD;
		case "devicekey":
			return DEVICEKEY;
		case "type":
			return TYPE;
		case "drivers":
			return DRIVERS;
		case "devices":
			return DEVICES;
		case "channels":
			return CHANNELS;
		case "config":
			return CONFIG;
		default:
			throw new IllegalArgumentException("Unknown field: " + field);
		}
	}
}
