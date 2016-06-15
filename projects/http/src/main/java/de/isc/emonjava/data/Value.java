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
package de.isc.emonjava.data;


public class Value {

	private final Long time;
	private final double value;

	
//	public Value(JSONObject json) {
//		this.time = Long.valueOf((String) json.get("time"))*1000;
//		this.value = Double.valueOf((String) json.get("value"));
//	}
	
	public Value(double value, Long timestamp) {
		this.value = value;
		this.time = timestamp;
	}
	
	public Value(double value) {
		this(value, null);
	}

	public double getValue() {
		return value;
	}

	public Long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "value: " + value + "; time: " + time;
	}
}
