/* 
 * Copyright 2016-19 ISC Konstanz
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
