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
package org.emoncms.com.http.request;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.emoncms.Feed;
import org.emoncms.com.EmoncmsSyntaxException;
import org.emoncms.com.http.json.Const;
import org.emoncms.com.http.json.FromJson;
import org.emoncms.com.http.json.JsonFeed;
import org.emoncms.com.http.json.JsonInput;
import org.emoncms.com.http.json.JsonInputConfig;
import org.emoncms.com.http.json.JsonInputList;
import org.emoncms.com.http.json.JsonTimevalue;
import org.emoncms.data.Timevalue;

import com.google.gson.JsonSyntaxException;


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
		if (response.toLowerCase().startsWith("ok")) {
			// Posted input values will be responded with "ok"
			return true;
		}
		else if (response.equalsIgnoreCase("null")) {
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
				try {
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
				catch (JsonSyntaxException e) {
					throw new EmoncmsSyntaxException(response);
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
