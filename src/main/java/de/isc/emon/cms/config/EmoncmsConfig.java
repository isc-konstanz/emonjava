package de.isc.emon.cms.config;

import java.util.HashMap;
import java.util.Map;

import de.isc.emon.cms.EmoncmsException;


public class EmoncmsConfig {

	private final boolean shell;
	private final String apiKey;
	private final String address;
	
	private final Map<String, Integer> processIdsByKey = new HashMap<String, Integer>();
	private final Map<String, Integer> datatypeIdsByKey = new HashMap<String, Integer>();
	private final Map<String, Integer> engineIdsByKey = new HashMap<String, Integer>();
	
	
	public EmoncmsConfig(boolean shell, String apiKey, String address) {
		this.shell = shell;
		this.apiKey = apiKey;
		this.address = address;
	}
	
	public String getAPIKey() {
		return apiKey;
	}
	
	public String getAddress() {
		return address;
	}
	
	public boolean useShell() {
		return shell;
	}

	public void addProcess(String key, int id) {
		processIdsByKey.put(key, id);
	}
	
	public boolean containsProcess(String key) {
		return processIdsByKey.containsKey(key);
	}
	
	public int getProcessId(String key) throws EmoncmsException {
		if (processIdsByKey.containsKey(key)) return processIdsByKey.get(key);
		throw new EmoncmsException("Requested unknown process: " + key);
	}
	
	public void addDataType(String key, int id) {
		datatypeIdsByKey.put(key, id);
	}
	
	public boolean containsDataType(String key) {
		return datatypeIdsByKey.containsKey(key);
	}
	
	public int getDataTypeId(String key) throws EmoncmsException {
		if (datatypeIdsByKey.containsKey(key)) return datatypeIdsByKey.get(key);
		throw new EmoncmsException("Requested unknown data type: " + key);
	}
	
	public void addEngine(String key, int id) {
		engineIdsByKey.put(key, id);
	}
	
	public boolean containsEngine(String key) {
		return engineIdsByKey.containsKey(key);
	}
	
	public int getEngineId(String key) throws EmoncmsException {
		if (engineIdsByKey.containsKey(key)) return engineIdsByKey.get(key);
		throw new EmoncmsException("Requested unknown engine: " + key);
	}
}
