/*
 * Copyright 2016 ISC Konstanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
