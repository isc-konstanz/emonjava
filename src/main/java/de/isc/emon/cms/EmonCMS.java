package de.isc.emon.cms;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.config.EmoncmsConfig;
import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.http.EmoncmsHTTPConnection;


public class EmonCMS {
	private final static Logger logger = LoggerFactory.getLogger(EmonCMS.class);
	
	private final EmoncmsConfig config;
	private final EmoncmsConnection connection;

    private final Map<String, Integer> inputNodesByKey = Collections.synchronizedMap(new HashMap<String, Integer>());
    private final Map<String, Integer> inputIdsByKey = Collections.synchronizedMap(new HashMap<String, Integer>());
    
    private final Map<String, Integer> feedIdsByKey = Collections.synchronizedMap(new HashMap<String, Integer>());    
	
    
    public EmonCMS(EmoncmsConfig config) {
    	this.config = config;
    	if (config.useShell()) {
    		connection = null;
    	}
    	else {
    		connection = new EmoncmsHTTPConnection(config.getLocation(), config.getAPIKey());
    	}
    }
    
    public boolean createInputProcess(int node, String inputName, String inputProcess, String feedName) 
    		throws EmoncmsException {
    	try {
			String response = connection.postInputData(inputName, node, 0);
			if (!response.equals("ok")) {
				throw new EmoncmsException("Failed to create new input: " + inputName);
			}
			
		} catch (IOException e) {
			throw new EmoncmsException("Error while writing request to emoncms connection");
		}
		return false;
    }
    
    public boolean containsInput() {
    	return false;
    }
}
