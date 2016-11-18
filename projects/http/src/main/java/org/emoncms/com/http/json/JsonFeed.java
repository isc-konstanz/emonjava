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

import com.google.gson.annotations.SerializedName;

public class JsonFeed {
	int id;
	String name;
	String tag;
	@SerializedName("public")
	String visible;
	int datatype;
	int engine;
	int size;
	String processList;
	String time;
	String value;


	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}

	public boolean isPublic() {
		if (visible.equals("1")) return true;
		else return false;
	}

	public int getDatatype() {
		return datatype;
	}

	public int getEngine() {
		return engine;
	}

	public int getSize() {
		return size;
	}

	public String getProcessList() {
		return processList;
	}

	public Long getTime() {
		if (time == null || time.isEmpty()) return null;
		return Long.valueOf(time);
	}

	public Double getValue() {
		if (value == null || value.isEmpty()) return null;
		return Double.valueOf(value);
	}
}