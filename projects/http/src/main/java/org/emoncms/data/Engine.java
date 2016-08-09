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

import de.isc.emonjava.data.Engine;


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
