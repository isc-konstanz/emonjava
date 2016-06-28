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
package de.isc.emonjava.com.http;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.isc.emonjava.Feed;
import de.isc.emonjava.com.EmoncmsSyntaxException;
import de.isc.emonjava.com.http.json.Const;
import de.isc.emonjava.com.http.json.FromJson;
import de.isc.emonjava.com.http.json.JsonFeed;
import de.isc.emonjava.com.http.json.JsonInput;
import de.isc.emonjava.com.http.json.JsonInputConfig;
import de.isc.emonjava.com.http.json.JsonInputList;
import de.isc.emonjava.com.http.json.JsonTimevalue;
import de.isc.emonjava.data.Timevalue;

public class HttpEmoncmsResponse {

	private final String response;
	private FromJson json;


	public HttpEmoncmsResponse(String response) {

		this.response = response;
	}
	
	public String getResponse() {
		
		return response;
	}
	
	public boolean isSuccess() throws EmoncmsSyntaxException {
		if (response.toLowerCase().equals("ok")) {
			// Posted input values will be responded with "ok"
			return true;
		}
		else if (response.toLowerCase().equals("null")) {
			// Responded string for deleted inputs or feeds.
			// TODO: Verify necessity for future versions.
			return true;
		}
		else if (response.startsWith("\"") && response.endsWith("\"")) {
			// Fetched field values will be responded with strings
			return true;
		}
		else if (Character.isDigit(response.charAt(0))) {
			// Returned numeric values (only first value will be checked) are valid
			return true;
		}
		else if (!response.toLowerCase().equals("false")) {
			if (!response.startsWith("Error:")) {
				if (json == null) {
					json = new FromJson(response);
				}
				if (json.isValidArray()) {
					return true;
				}
				else if (json.isValidObject()) {
					if (!json.getJsonObject().has(Const.SUCCESS)) {
						return true;
					}
					else {
						if (json.getJsonObject().get(Const.SUCCESS).getAsBoolean()) {
							return true;
						}
						else {
							String message = json.getJsonObject().get(Const.MESSAGE).getAsString();
							throw new EmoncmsSyntaxException(message);
						}
					}
				}
			}
			else throw new EmoncmsSyntaxException(response.substring(7));
		}
		return false;
	}
	
	public String getString(String key) {

		if (json == null) {
			json = new FromJson(response);
		}
		if (json.isValidObject()) {
			return json.getJsonObject().get(key).getAsString();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<JsonInput> getInputList() throws ClassCastException {

		if (json == null) {
			json = new FromJson(response);
		}
		if (json.isValidArray()) {
			return (List<JsonInput>)(List<? extends Object>) json.getArrayList(JsonInput.class);
		}
		return null;
	}
	
	public JsonInputList getInputConfigList(String node) {

		if (json == null) {
			json = new FromJson(response);
		}
		if (json.isValidObject()) {
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

		if (json == null) {
			json = new FromJson(response);
		}
		if (json.isValidObject()) {
			return new JsonInputConfig(node, name, json.getJsonObject());
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<JsonFeed> getFeedList() throws ClassCastException {

		if (json == null) {
			json = new FromJson(response);
		}
		if (json.isValidArray()) {
			return (List<JsonFeed>)(List<? extends Object>) json.getArrayList(JsonFeed.class);
		}
		return null;
	}
	
	public JsonFeed getFeed() throws ClassCastException {

		if (json == null) {
			json = new FromJson(response);
		}
		if (json.isValidObject()) {
			return (JsonFeed) json.getObject(JsonFeed.class);
		}
		return null;
	}
	
	public Map<Feed, Double> getValues(LinkedList<Feed> feeds) {

		if (json == null) {
			json = new FromJson(response);
		}
		Double values[] = json.getDoubleArray();

		Map<Feed, Double> results = new HashMap<Feed, Double>(feeds.size());
		for (int i = 0; i<values.length; i++) {
			
			results.put(feeds.get(i), values[i]);
		}
		return results;
	}
	
	public JsonTimevalue getTimevalue() throws ClassCastException {

		if (json == null) {
			json = new FromJson(response);
		}
		if (json.isValidObject()) {
			return (JsonTimevalue) json.getObject(JsonTimevalue.class);
		}
		return null;
	}
	
	public LinkedList<Timevalue> getTimevalues() {

		if (json == null) {
			json = new FromJson(response);
		}
		LinkedList<Double[]> valuesArrList = json.getDoubleArrayList();
		LinkedList<Timevalue> timevalues = new LinkedList<Timevalue>();
		if (valuesArrList != null) {
			for (Double[] valueArr : valuesArrList) {
				Timevalue timevalue = new Timevalue(valueArr[0].longValue(), valueArr[1]);
				timevalues.add(timevalue);
			}
		}
		return timevalues;
	}
}
