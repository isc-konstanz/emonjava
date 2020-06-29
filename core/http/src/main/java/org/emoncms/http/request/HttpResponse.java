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
package org.emoncms.http.request;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.Feed;
import org.emoncms.data.Timevalue;
import org.emoncms.http.json.Const;
import org.emoncms.http.json.JsonFeed;
import org.emoncms.http.json.JsonInput;
import org.emoncms.http.json.JsonInputConfig;
import org.emoncms.http.json.JsonInputList;
import org.emoncms.http.json.JsonResponse;
import org.emoncms.http.json.JsonTimevalue;

import com.google.gson.JsonSyntaxException;

public class HttpResponse {

	private final String response;
	private final JsonResponse json;

	public HttpResponse(String response) throws EmoncmsException {
		this.response = response;
		if (response.toLowerCase().startsWith("ok")) {
			// Posted input values will be responded with "ok"
			this.json = null;
			return;
		}
		else if (response.equalsIgnoreCase("null")) {
			// Responded string for deleted inputs or feeds.
			// TODO: Verify necessity for future versions.
			this.json = null;
			return;
		}
		else if (response.startsWith("\"") && response.endsWith("\"")) {
			// Fetched field values will be responded with strings
			this.json = null;
			return;
		}
		else if (Character.isDigit(response.charAt(0))) {
			// Returned numeric values (only first value will be checked) are valid
			this.json = null;
			return;
		}
		else if (response.startsWith("Error:")) {
			throw new EmoncmsSyntaxException(response.substring(7));
		}
		else if (response.toLowerCase().equals("false")) {
			throw new EmoncmsException("Emoncms request responsed \"false\"");
		}
		try {
			json = new JsonResponse(response);
		}
		catch (JsonSyntaxException e) {
			throw new EmoncmsSyntaxException("Invalid JSON: "+response);
		}
		if (json.isValidArray()) {
			return;
		}
		else if (json.isValidObject()) {
			if (!json.getJsonObject().has(Const.SUCCESS)) {
				return;
			}
			else {
				if (json.getJsonObject().get(Const.SUCCESS).getAsBoolean()) {
					return;
				}
				else {
					String message = json.getJsonObject().get(Const.MESSAGE).getAsString();
					throw new EmoncmsSyntaxException(message);
				}
			}
		}
	}

	public String getString(String key) {
		if (json != null && json.isValidObject()) {
			return json.getJsonObject().get(key).getAsString();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<JsonInput> getInputList() throws ClassCastException {
		if (json != null && json.isValidArray()) {
			return (List<JsonInput>)(List<? extends Object>) json.getArrayList(JsonInput.class);
		}
		return null;
	}

	public JsonInputList getInputConfigList(String node) {
		if (json != null && json.isValidObject()) {
			if (node != null) {
				return new JsonInputList(node, json.getJsonObject());
			}
			else {
				return new JsonInputList(json.getJsonObject());
			}
		}
		return null;
	}

	public JsonInputConfig getInputConfig(String node, String name) {
		if (json != null && json.isValidObject()) {
			return new JsonInputConfig(node, name, json.getJsonObject());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<JsonFeed> getFeedList() throws ClassCastException {
		if (json != null && json.isValidArray()) {
			return (List<JsonFeed>)(List<? extends Object>) json.getArrayList(JsonFeed.class);
		}
		return null;
	}

	public JsonFeed getFeed() throws ClassCastException {
		if (json != null && json.isValidObject()) {
			return (JsonFeed) json.getObject(JsonFeed.class);
		}
		return null;
	}

	public Map<Feed, Double> getValues(LinkedList<Feed> feeds) {
		Map<Feed, Double> results = new HashMap<Feed, Double>();
		if (json != null) {
			Double[] values = json.getDoubleArray();
			if (values != null) {
				for (int i = 0; i<values.length; i++) {
					results.put(feeds.get(i), values[i]);
				}
			}
		}
		return results;
	}

	public JsonTimevalue getTimevalue() throws ClassCastException {
		if (json != null && json.isValidObject()) {
			return (JsonTimevalue) json.getObject(JsonTimevalue.class);
		}
		return null;
	}

	public LinkedList<Timevalue> getTimevalues() {
		LinkedList<Timevalue> timevalues = new LinkedList<Timevalue>();
		if (json != null) {
			LinkedList<Double[]> valuesArrList = json.getDoubleArrayList();
			if (valuesArrList != null) {
				for (Double[] valueArr : valuesArrList) {
					Timevalue timevalue = new Timevalue(valueArr[0].longValue(), valueArr[1]);
					timevalues.add(timevalue);
				}
			}
		}
		return timevalues;
	}

	@Override
	public String toString() {
		return response;
	}

}
