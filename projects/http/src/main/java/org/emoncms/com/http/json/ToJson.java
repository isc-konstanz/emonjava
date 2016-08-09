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

import java.util.List;
import java.util.Map;

import org.emoncms.data.Options;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class ToJson {

	private final Gson gson;
	private final JsonObject jsonObject;

	public ToJson() {
		
		gson = new Gson();
		jsonObject = new JsonObject();
	}

	public JsonObject getJsonObject() {
		
		return jsonObject;
	}

	public void addJsonObject(String propertyName, JsonObject jsonObject) {
		
		this.jsonObject.add(propertyName, jsonObject);
	}

	@Override
	public String toString() {
		
		return jsonObject.toString();
	}

	public void addBoolean(String propertyName, boolean value) {
		
		jsonObject.addProperty(propertyName, value);
	}

	public void addInteger(String propertyName, int value) {
		
		jsonObject.addProperty(propertyName, value);
	}

	public void addDouble(String propertyName, double value) {
		
		jsonObject.addProperty(propertyName, value);
	}

	public void addString(String propertyName, String value) {
		
		jsonObject.addProperty(propertyName, value);
	}

	public void addStringList(String propertyName, List<String> stringList) {
		
		jsonObject.add(propertyName, gson.toJsonTree(stringList).getAsJsonArray());
	}

	public void addOptions(Options options) {
		for (Map.Entry<String, String> option : options.entrySet()) {
			jsonObject.addProperty(option.getKey(), option.getValue());
		}
	}
}
