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
