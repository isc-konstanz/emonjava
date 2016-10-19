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

import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class JsonInputList extends ArrayList<JsonInputConfig> {
	private static final long serialVersionUID = -8635109389933023875L;

	
	public JsonInputList() {
		super();
	}

	public JsonInputList(JsonObject json) {
		super();

		for (Map.Entry<String, JsonElement> nodeEntry : json.entrySet()) {
			for (Map.Entry<String, JsonElement> nameEntry : nodeEntry.getValue().getAsJsonObject().entrySet()) {
				
				int id = nameEntry.getValue().getAsJsonObject().get(Const.ID).getAsInt();
				String processList = nameEntry.getValue().getAsJsonObject().get(Const.PROCESSLIST).getAsString();
				
				JsonInputConfig input = new JsonInputConfig(id, nodeEntry.getKey(), nameEntry.getKey(), processList);
				super.add(input);
			}
		}
	}
	
	public JsonInputList(String node, JsonObject json) {
		super();

		JsonObject nodeObject = json.getAsJsonObject(node);
		for (Map.Entry<String, JsonElement> nameEntry : nodeObject.entrySet()) {
			
			int id = nameEntry.getValue().getAsJsonObject().get(Const.ID).getAsInt();
			String processList = nameEntry.getValue().getAsJsonObject().get(Const.PROCESSLIST).getAsString();
			
			JsonInputConfig input = new JsonInputConfig(id, node, nameEntry.getKey(), processList);
			super.add(input);
		}
	}
}
