package de.isc.emon.cms.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import de.isc.emon.cms.EmoncmsException;


public class Process {
	private final int id;
	private final String argument;
	
	
	public Process(String key, String argument) throws EmoncmsException {
		Properties properties = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream("conf/emoncms.properties");
			properties.load(in);

		} catch (IOException e) {
        	throw new EmoncmsException("Error reading emoncms.properties: " + e.getMessage());
		}
		finally {
			try {
				in.close();
			} catch (IOException e) {
              	throw new EmoncmsException("Unknown exception while reading process property: " + e.getMessage());
			}
		}
		if (properties.containsKey("process." + key)) {
			id = Integer.valueOf(properties.getProperty("process." + key));
			this.argument = argument;
		}
		else throw new EmoncmsException("Process \"" + key + "\" not found in emoncms.properties");
	}
	
	public Process(int id, String argument) {
		this.id = id;
		this.argument = argument;
	}
	
	public Process(int id) {
		this(id, null);
	}

	public int getId() {
		return id;
	}

	public String getArgument() {
		return argument;
	}

	@Override
	public String toString() {
		return id + ":" + argument;
	}
}
