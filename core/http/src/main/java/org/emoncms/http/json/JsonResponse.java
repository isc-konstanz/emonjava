/* 
 * Copyright 2016-20 ISC Konstanz
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
package org.emoncms.http.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class JsonResponse {
	private final Gson gson;
	private final JsonElement jse;

	public JsonResponse(String jsonString) {
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

	public Object getObject(Class<?> objectClass) throws ClassCastException, JsonSyntaxException {
		return gson.fromJson(jse, objectClass);			
	}

	public ArrayList<Object> getArrayList(Class<?> objectClass) throws ClassCastException, JsonSyntaxException {
		ArrayList<Object> objectList = new ArrayList<Object>();
		JsonArray jsa = jse.getAsJsonArray();

		for (JsonElement objectJson : jsa) {
			Object object = gson.fromJson(objectJson, objectClass);
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
