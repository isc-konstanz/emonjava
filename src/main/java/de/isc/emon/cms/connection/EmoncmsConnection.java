package de.isc.emon.cms.connection;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import de.isc.emon.cms.EmoncmsException;


public interface EmoncmsConnection {
	
	public String getId();

	public void postRequest(String request) throws EmoncmsException;
	
	public String getResponse(String request) throws IOException;

	public Object getJSONResponse(String request) throws IOException, ParseException;
	
}
