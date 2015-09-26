package de.isc.emon.cms.connection.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import de.isc.emon.cms.EmoncmsException;
import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.EmoncmsResponse;
import de.isc.emon.cms.connection.http.EmoncmsTask.EmoncmsTaskCallbacks;


public class EmoncmsHTTPConnection implements EmoncmsConnection, EmoncmsTaskCallbacks {
//	private static final Logger logger = LoggerFactory.getLogger(EmoncmsHTTPConnection.class);

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
	public void onTaskFinished(EmoncmsTask task, EmoncmsResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailure(EmoncmsTask task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeRequest(String request) throws EmoncmsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EmoncmsResponse postRequest(String request, String parameters) throws EmoncmsException {
		String url = URL + request + "&apikey=" + KEY;
        byte[] postData = parameters.getBytes(Charset.forName("UTF-8"));
        
		HttpURLConnection connection = null;
        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(postData.length)); 
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(postData);
            
            int status = connection.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                	return new EmoncmsResponse(parseResponse(new InputStreamReader(connection.getInputStream(), "UTF-8")));
            }
            return null;

        } catch (IOException e) {
        	throw new EmoncmsException("Error while writing POST request to \"" + url + parameters + "\": " + e.getMessage());
        } finally {
           if (connection != null) {
              try {
                  connection.disconnect();
              } catch (Exception e) {
              	throw new EmoncmsException("Unknown exception while closing connection: " + e.getMessage());
              }
           }
        }
	}

	@Override
	public EmoncmsResponse getRequest(String request) throws EmoncmsException {
		String url = URL + request + "&apikey=" + KEY;
		HttpURLConnection connection = null;
        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-length", "0");
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.connect();
            
            int status = connection.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                	return new EmoncmsResponse(parseResponse(new InputStreamReader(connection.getInputStream(), "UTF-8")));
            }
            return null;

        } catch (IOException e) {
        	throw new EmoncmsException("Error while writing GET request to \"" + url + "\": " + e.getMessage());
        } finally {
           if (connection != null) {
              try {
                  connection.disconnect();
              } catch (Exception e) {
              	throw new EmoncmsException("Unknown exception while closing connection: " + e.getMessage());
              }
           }
        }
	}
	
	private String parseResponse(InputStreamReader input) throws IOException {
		BufferedReader br = new BufferedReader(input);
        StringBuilder sb = new StringBuilder();
        
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line); //+"\n");
        }
        br.close();

		return sb.toString();
	}
}
