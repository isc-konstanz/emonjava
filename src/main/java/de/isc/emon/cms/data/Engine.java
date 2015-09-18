package de.isc.emon.cms.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import de.isc.emon.cms.EmoncmsException;


public class Engine {
	private final int id;
	
	
	public Engine(String key) throws EmoncmsException {
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
              	throw new EmoncmsException("Unknown exception while reading engine property: " + e.getMessage());
			}
		}
		if (properties.containsKey("engine." + key)) {
			id = Integer.valueOf(properties.getProperty("engine." + key));
		}
		else throw new EmoncmsException("Engine \"" + key + "\" not found in emoncms.properties");
	}
	
	public Engine(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
