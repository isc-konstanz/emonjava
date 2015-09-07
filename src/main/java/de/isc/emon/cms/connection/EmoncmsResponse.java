package de.isc.emon.cms.connection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class EmoncmsResponse {
	
	private final Map<String, String> response;

	public EmoncmsResponse() {
		response = new HashMap<String, String>();
	}
	
	public EmoncmsResponse(String key, String value) {
		this();
		
		response.put(key, value);
	}

	public void put(String key, String value) {
		response.put(key, value);
	}
	
	public String get(String key) {
		return response.get(key);
	}
	
	public Collection<String> getKeys() {
		return response.keySet();
	}
	
	@Override
	public String toString() {
		return response.toString();
	}
}
