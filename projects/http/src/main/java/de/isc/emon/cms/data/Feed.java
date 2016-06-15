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
package de.isc.emon.cms.data;

public class Feed {
	
	private final int id;
	private final String name;
	private final String tag;
	private final int datatype;
	private final int engine;
	private final int size;
	
	private final Value value;
	
	
//	public Feed(JSONObject json) {
//		this.id = Integer.valueOf((String) json.get("id"));
//		this.name = (String) json.get("name");
//		this.tag = (String) json.get("tag");
//		this.datatype = Integer.valueOf((String) json.get("datatype"));
//		this.engine = Integer.valueOf((String) json.get("engine"));
//		this.size = Integer.valueOf((String) json.get("size"));
//		
//		String valueStr = (String) json.get("value");
//		if (!valueStr.isEmpty()) {
//			value = new Value(Double.valueOf(valueStr), 
//					(long) json.get("time"));
////					Long.valueOf((String) json.get("time")));
//		}
//		else {
//			value = null;
//		}
//	}

	public Feed(int id, String name, String tag, int datatype, int engine, int size, Value value) {
		this.id = id;
		this.name = name;
		this.tag = tag;
		this.datatype = datatype;
		this.engine = engine;
		this.size = size;
		
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

	public int getDatatype() {
		return datatype;
	}

	public int getEngine() {
		return engine;
	}

	public int getSize() {
		return size;
	}

	public Value getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "id: " + id + "; name: " + name + "; tag: " + tag + "; datatype: " + datatype + "; engine: " + engine + "; size: " + size
				+ "; " + value.toString();
	}
}
