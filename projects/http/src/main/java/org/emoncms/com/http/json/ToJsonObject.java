/*
 * Copyright 2016-17 ISC Konstanz
 *
 * This file is part of emonjava.
 * For more information visit https://bitbucket.org/isc-konstanz/emonjava
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
 *
 */
package org.emoncms.com.http.json;

import java.util.List;
import java.util.Map;

import org.emoncms.data.Options;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class ToJsonObject {

	private final Gson gson;
	private final JsonObject jsonObject;

	public ToJsonObject() {
		
		gson = new Gson();
		jsonObject = new JsonObject();
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

	public JsonObject getJsonObject() {
		
		return jsonObject;
	}

	@Override
	public String toString() {
		
		return jsonObject.toString();
	}
}
