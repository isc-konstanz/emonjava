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
	private final int id;
	private final String name;
	private final String tag;
	@SerializedName("public")
	private final String visible;
	private final int datatype;
	private final int engine;
	private final int size;
	private final String time;
	private final String value;
	
	
	public JsonFeed(int id, String name, String tag, String visible, int datatype, int engine, int size, String time, String value) {
		this.id = id;
		this.name = name;
		this.tag = tag;
		this.visible = visible;
		this.datatype = datatype;
		this.engine = engine;
		this.size = size;
		this.time = time;
		this.value = value;
	}
	
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

	public Long getTime() {
		if (time == null || time.isEmpty()) return null;
		return Long.valueOf(time);
	}

	public Double getValue() {
		if (value == null || value.isEmpty()) return null;
		return Double.valueOf(value);
	}
}