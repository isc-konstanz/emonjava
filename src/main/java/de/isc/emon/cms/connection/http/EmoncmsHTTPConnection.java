package de.isc.emon.cms.connection.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.connection.EmoncmsConnection;


public class EmoncmsHTTPConnection implements EmoncmsConnection {
	private static final Logger logger = LoggerFactory.getLogger(EmoncmsHTTPConnection.class);

	private final String URL;
	private final String KEY;
	
//  private final Map<Integer, Sample> queuedSamplesByInputId = Collections.synchronizedMap(new LinkedHashMap<Integer, Sample>());
    
	
	public EmoncmsHTTPConnection(String address, String apiKey) {
		String url = "";
		if (!address.startsWith("http://")) {
			url = "http://".concat(address);
		}
		if (!url.endsWith("/")) {
			url = url.concat("/");
		}
    	this.URL = url.concat("emoncms/");
    	this.KEY = apiKey;
    }

	@Override
	public String getId() {
		return "HTTP connection";
	}

	@Override
	public void postRequest(String request) {
		try {
	        String response = getResponse(request);
	        if (!response.equals("ok")) {
	        	logger.debug("Failed to post request: {}", response);
	        }
	        
		} catch (IOException e) {
			logger.debug("Exception while posting http request: {}", request);
		}
	}

	@Override
	public String getResponse(String request) throws IOException {
        URL url = new URL(URL + request + "&apikey=" + KEY);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream is = new BufferedInputStream(urlConnection.getInputStream());
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        
        return br.readLine();
	}

	@Override
	public Object getJSONResponse(String request) throws IOException, ParseException {
        URL url = new URL(URL + request + "&apikey=" + KEY);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream is = new BufferedInputStream(urlConnection.getInputStream());
        InputStreamReader isr = new InputStreamReader(is);
        JSONParser parser = new JSONParser();
        
        return parser.parse(isr);
	}
}
