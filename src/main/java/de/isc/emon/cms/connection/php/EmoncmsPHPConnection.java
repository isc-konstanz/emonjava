package de.isc.emon.cms.connection.php;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.EmoncmsException;
import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.EmoncmsResponse;


public class EmoncmsPHPConnection implements EmoncmsConnection {
	private static final Logger logger = LoggerFactory.getLogger(EmoncmsPHPConnection.class);

	private final String DIR;
	private final String KEY;
    
	
	public EmoncmsPHPConnection(String address, String apiKey) {
    	this.DIR = address;
    	this.KEY = apiKey;
    }

	@Override
	public String getId() {
		return "PHP shell";
	}

	@Override
	public String getAddress() {
		return DIR;
	}

	@Override
	public void postRequest(String request) throws EmoncmsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EmoncmsResponse getResponse(String request) throws EmoncmsException {
		// TODO Auto-generated method stub
		return null;
	}
}
