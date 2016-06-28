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
package de.isc.emonjava.data;


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
