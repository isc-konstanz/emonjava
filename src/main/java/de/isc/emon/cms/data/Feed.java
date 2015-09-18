package de.isc.emon.cms.data;

import org.json.simple.JSONObject;


public class Feed {
	
	private final int id;
	private final String name;
	private final String tag;
	private final int datatype;
	private final int engine;
	private final int size;
	
	private final Value value;
	
	
	public Feed(JSONObject json) {
		this.id = Integer.valueOf((String) json.get("id"));
		this.name = (String) json.get("name");
		this.tag = (String) json.get("tag");
		this.datatype = Integer.valueOf((String) json.get("datatype"));
		this.engine = Integer.valueOf((String) json.get("engine"));
		this.size = Integer.valueOf((String) json.get("size"));
		
		String valueStr = (String) json.get("value");
		if (!valueStr.isEmpty()) {
			value = new Value(Double.valueOf(valueStr), 
					Long.valueOf((String) json.get("time")));
		}
		else {
			value = null;
		}
	}

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
