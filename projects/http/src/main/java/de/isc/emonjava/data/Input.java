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


public class Input {
	
	private final int id;
	private final int nodeId;
	private final String name;
	private final String processList;
	

	public Input(int id, int nodeId, String name, String processList) {
		this.id = id;
		this.nodeId = nodeId;
		this.name = name;
		this.processList = processList;
	}
	
//	public Input(JSONObject json) {
//		this.id = Integer.valueOf((String) json.get("id"));
//		this.nodeId = Integer.valueOf((String) json.get("nodeid"));
//		this.name = (String) json.get("name");
//		this.processList = (String) json.get("processList");
//	}

	public int getId() {
		return id;
	}

	public int getNodeId() {
		return nodeId;
	}

	public String getName() {
		return name;
	}

	public String getProcessList() {
		return processList;
	}

	@Override
	public String toString() {
		return "id: " + id + "; nodeId: " + nodeId + "; name: " + name + "; processList: " + processList;
	}
}
