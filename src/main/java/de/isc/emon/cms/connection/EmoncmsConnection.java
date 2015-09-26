package de.isc.emon.cms.connection;

import de.isc.emon.cms.EmoncmsException;


public interface EmoncmsConnection {
	
	public String getId();
	
	public String getAddress();

	public void writeRequest(String request) throws EmoncmsException;

	public EmoncmsResponse postRequest(String request, String parameters) throws EmoncmsException;
	
	public EmoncmsResponse getRequest(String request) throws EmoncmsException;
	
}
