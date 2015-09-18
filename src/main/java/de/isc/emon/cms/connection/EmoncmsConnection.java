package de.isc.emon.cms.connection;

import de.isc.emon.cms.EmoncmsException;


public interface EmoncmsConnection {
	
	public String getId();
	
	public String getAddress();

	public void postRequest(String request) throws EmoncmsException;
	
	public EmoncmsResponse getResponse(String request) throws EmoncmsException;
	
}
