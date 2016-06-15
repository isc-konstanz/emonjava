package de.isc.emon.cms.communication;

import java.util.List;

import de.isc.emon.cms.EmoncmsException;


public interface EmoncmsCommunication {
	
	public String getId();
	
	public String getAddress();

	public void writeRequest(String request, List<RequestParameter> parameters);

	public EmoncmsResponse sendRequest(String request, List<RequestParameter> parameters) throws EmoncmsException;
	
}
