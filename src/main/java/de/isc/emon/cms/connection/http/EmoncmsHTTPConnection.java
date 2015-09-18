package de.isc.emon.cms.connection.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.EmoncmsException;
import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.EmoncmsResponse;


public class EmoncmsHTTPConnection implements EmoncmsConnection {
	private static final Logger logger = LoggerFactory.getLogger(EmoncmsHTTPConnection.class);

	private final String URL;
	private final String KEY;
	
//  private final Map<Integer, Value> queuedValuesByInputId = Collections.synchronizedMap(new LinkedHashMap<Integer, Value>());
    
	
	public EmoncmsHTTPConnection(String address, String apiKey) {
		String url;
		if (!address.startsWith("http://")) {
			url = "http://".concat(address);
		}
		else {
			url = address;
		}
    	this.URL = url.concat("emoncms/");
    	this.KEY = apiKey;
    }

	@Override
	public String getId() {
		return "HTTP connection";
	}

	@Override
	public String getAddress() {
		return URL;
	}

	@Override
	public void postRequest(String request) {
		// TODO add task scheduling
		try {
	        getResponse(request);
	        
		} catch (EmoncmsException e) {
			logger.debug("Exception while posting http request: {}", request);
		}
	}

	@Override
	public EmoncmsResponse getResponse(String request) throws EmoncmsException {
		String url = URL + request + "&apikey=" + KEY;
		HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(5000);
            c.setReadTimeout(10000);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return new EmoncmsResponse(sb.toString());
            }

        } catch (IOException e) {
        	throw new EmoncmsException("Error while connecting to \"" + url + "\": " + e.getMessage());
        } finally {
           if (c != null) {
              try {
                  c.disconnect();
              } catch (Exception e) {
              	throw new EmoncmsException("Unknown exception while closing connection: " + e.getMessage());
              }
           }
        }
        return null;
	}
}
