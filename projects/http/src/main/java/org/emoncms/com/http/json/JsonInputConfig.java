/* 
 * Copyright 2016-17 ISC Konstanz
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
package org.emoncms.com.http.json;

import com.google.gson.JsonObject;


public class JsonInputConfig {
	int id;
	String nodeid;
	String name;
	String processList;

	
	public JsonInputConfig() {
		super();
	}

	public JsonInputConfig(String nodeid, String name, JsonObject json) {
		this.nodeid = nodeid;
		this.name = name;

		JsonObject nodeObject = json.getAsJsonObject(nodeid);
		JsonObject inputObject = nodeObject.getAsJsonObject(name);
		
		id = inputObject.get(Const.ID).getAsInt();
		processList = inputObject.get(Const.PROCESSLIST).getAsString();
	}

	public JsonInputConfig(int id, String nodeid, String name, String processList) {
		this.id = id;
		this.nodeid = nodeid;
		this.name = name;
		this.processList = processList;
	}

	public int getId() {
		return id;
	}

	public String getNodeid() {
		return nodeid;
	}


	public String getName() {
		return name;
	}

	public String getProcessList() {
		return processList;
	}
}
