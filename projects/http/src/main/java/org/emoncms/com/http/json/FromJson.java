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
import java.util.Iterator;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class FromJson {

	private final Gson gson;
	private final JsonElement jse;

	public FromJson(String jsonString) {

		gson = new Gson();
		jse = gson.fromJson(jsonString, JsonElement.class);
	}
	
	public boolean isValidObject() {
		
		return !jse.isJsonNull() && jse.isJsonObject();
	}
	
	public boolean isValidArray() {
		
		return !jse.isJsonNull() && jse.isJsonArray();
	}

	public Gson getGson() {

		return gson;
	}

	public JsonObject getJsonObject() {

		return jse.getAsJsonObject();
	}

	public JsonArray getJsonArray() {

		return jse.getAsJsonArray();
	}
	
	public Object getObject(Class<?> objectClass) throws ClassCastException {

		return gson.fromJson(jse, objectClass);			
	}
	
	public ArrayList<Object> getArrayList(Class<?> objectClass) throws ClassCastException {

		ArrayList<Object> objectList = new ArrayList<Object>();
		JsonArray jsa = jse.getAsJsonArray();

		Iterator<JsonElement> iteratorJsonArray = jsa.iterator();
		while (iteratorJsonArray.hasNext()) {
			Object object = gson.fromJson(iteratorJsonArray.next(), objectClass);
			objectList.add(object);
		}
		return objectList;
	}

	public Double[] getDoubleArray() {

		Double doubleArray[] = null;

		if (!jse.isJsonNull() && jse.isJsonArray()) {
			doubleArray = gson.fromJson(jse, Double[].class);
		}
		return doubleArray;
	}

	public LinkedList<Double[]> getDoubleArrayList() {

		LinkedList<Double[]> resultList = new LinkedList<Double[]>();

		if (!jse.isJsonNull() && jse.isJsonArray()) {
			JsonArray jsa = jse.getAsJsonArray();

			Iterator<JsonElement> iteratorJsonArray = jsa.iterator();
			while (iteratorJsonArray.hasNext()) {
				resultList.add(gson.fromJson(iteratorJsonArray.next(), Double[].class));
			}
		}
		if (resultList.size() == 0) {
			resultList = null;
		}
		return resultList;
	}
}
