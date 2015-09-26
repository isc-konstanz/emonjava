package de.isc.emon.cms.connection.php;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.EmoncmsException;
import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.EmoncmsResponse;
import de.isc.emon.cms.connection.RequestParameter;


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
	public void writeRequest(String request, List<RequestParameter> parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EmoncmsResponse sendRequest(String request, List<RequestParameter> parameters) throws EmoncmsException {
		// TODO Auto-generated method stub
		return null;
	}
}
