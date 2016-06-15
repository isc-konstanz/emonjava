package de.isc.emon.cms.data;

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
