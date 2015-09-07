package de.isc.emon.cms.connection.php;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.EmoncmsException;
import de.isc.emon.cms.connection.EmoncmsConnection;


public class EmoncmsPHPConnection implements EmoncmsConnection {
	private static final Logger logger = LoggerFactory.getLogger(EmoncmsPHPConnection.class);

	private final String URL;
	private final String KEY;
    
	
	public EmoncmsPHPConnection(String address, String apiKey) {
    	this.URL = address;
    	this.KEY = apiKey;
    }

	@Override
	public String getId() {
		return "PHP shell";
	}

	@Override
	public void postRequest(String request) throws EmoncmsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getResponse(String request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getJSONResponse(String request) throws IOException,
			ParseException {
		// TODO Auto-generated method stub
		return null;
	}
}
