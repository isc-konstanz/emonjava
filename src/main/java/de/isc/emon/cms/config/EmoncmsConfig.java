package de.isc.emon.cms.config;

import java.util.Map;


public class EmoncmsConfig {

	private final boolean shell;
	private final String api;
	private final String location;
	
	private final Map<String, EmoncmsProcessConfig> processByKey;

	
	public EmoncmsConfig(boolean shell, String api, String location, Map<String, EmoncmsProcessConfig> processes) {
		this.shell = shell;
		this.api = api;
		this.location = location;
		
		this.processByKey = processes;
	}
	
	public String getAPIKey() {
		return api;
	}
	
	public String getLocation() {
		return location;
	}
	
	public boolean useShell() {
		return shell;
	}
	
	public void addProcess(EmoncmsProcessConfig process) {
		processByKey.put(process.getKey(), process);
	}
	
	public boolean containsProcess(String processKey) {
		return processByKey.containsKey(processKey);
	}
	
	public EmoncmsProcessConfig getProcessId(String processKey) {
		return processByKey.get(processKey);
	}
}
