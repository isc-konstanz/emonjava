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

import org.emoncms.data.Data;
import org.emoncms.data.Namevalue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class ToJsonArray {

	private final JsonArray jsonArray;

	public ToJsonArray() {
		
		jsonArray = new JsonArray();
	}

	public JsonArray getJsonArray() {
		
		return jsonArray;
	}

	@Override
	public String toString() {
		
		return jsonArray.toString();
	}

	public void addData(long referenceTime, Data data) {
		
		JsonArray dataArray = new JsonArray();
		
		Long time = data.getTime();
		if (time == null) {
			time = System.currentTimeMillis();
		}
		// Posted UNIX time values need to be sent in seconds
		dataArray.add((int) Math.round(((double) referenceTime - time)/1000));
		dataArray.add(data.getNode());
		
		for (Namevalue namevalue : data.getNamevalues()) {
			JsonObject valueObj = new JsonObject();
			valueObj.addProperty(namevalue.getName(), namevalue.getValue());
			dataArray.add(valueObj);
		}
		jsonArray.add(dataArray);
	}
}
