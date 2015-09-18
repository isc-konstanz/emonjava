package de.isc.emon.cms.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import de.isc.emon.cms.EmoncmsException;


public class DataType {
	private final int id;
	
	
	public DataType(String key) throws EmoncmsException {
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
              	throw new EmoncmsException("Unknown exception while reading datatype property: " + e.getMessage());
			}
		}
		if (properties.containsKey("datatype." + key)) {
			id = Integer.valueOf(properties.getProperty("datatype." + key));
		}
		else throw new EmoncmsException("Datatype \"" + key + "\" not found in emoncms.properties");
	}
	
	public DataType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
