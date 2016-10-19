/*
 * Copyright 2016 ISC Konstanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.emoncms.com.http.request;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import org.emoncms.com.EmoncmsException;
import org.emoncms.com.http.HttpException;


public class HttpCallable implements Callable<HttpEmoncmsResponse> {
	
	private final static int CONNECTION_TIMEOUT = 5000;
	private final static int READ_TIMEOUT = 10000;

	private final HttpEmoncmsRequest request;


	public HttpCallable(HttpEmoncmsRequest request) {
		
		this.request = request;
	}

	public HttpEmoncmsRequest getRequest() {
		
		return request;
	}
	
	@Override
	public HttpEmoncmsResponse call() throws Exception {
		
    	HttpRequestMethod method = request.getMethod();
        switch (method) {
        case GET:
        	return get(request);
        case POST:
        	return post(request);
        default:
          	throw new EmoncmsException("No HTTP request method " + method.toString() + "implemented");
        }
	}
	
	private HttpEmoncmsResponse get(HttpEmoncmsRequest request) throws IOException {
		
		HttpURLConnection connection = null;
        try {
	        URL u = new URL(request.getRequest());
	        connection = (HttpURLConnection) u.openConnection();

	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Charset", "UTF-8");
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Content-length", "0");
	        
	        connection.setInstanceFollowRedirects(false);
	        connection.setAllowUserInteraction(false);
	        connection.setUseCaches(false);
	        connection.setDoOutput(true);
	        connection.setConnectTimeout(CONNECTION_TIMEOUT);
	        connection.setReadTimeout(READ_TIMEOUT);
	        connection.connect();
	    	
	        if (verifyResponse(connection.getResponseCode())) {
	        	return read(connection);
	        }
	        else throw new HttpException("HTTP status code " + connection.getResponseCode() + ": " + connection.getResponseMessage());
        
        } finally {
        	if (connection != null) {
        	   try {
                  	connection.disconnect();
              	} catch (Exception e) {
        	        throw new HttpException("Unknown exception while closing connection: " + e);
              	}
           	}
        }
	}
	
	private HttpEmoncmsResponse post(HttpEmoncmsRequest request) throws IOException {
		
		HttpURLConnection connection = null;
        try {
	        URL u = new URL(request.getRequest());
	        connection = (HttpURLConnection) u.openConnection();

	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Charset", "UTF-8");
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        
        	byte[] data = null;
	        if (request.getParameters() != null) {
		        data = request.parseParameters().getBytes(Charset.forName("UTF-8"));
		        connection.setRequestProperty("Content-Length", String.valueOf(data.length));
	        }
	        else {
		        connection.setRequestProperty("Content-length", "0");
	        }
	        
	        connection.setInstanceFollowRedirects(false);
	        connection.setAllowUserInteraction(false);
	        connection.setUseCaches(false);
	        connection.setDoOutput(true);
	        connection.setConnectTimeout(CONNECTION_TIMEOUT);
	        connection.setReadTimeout(READ_TIMEOUT);
	
	        if (data != null) {
		        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		        wr.write(data);
				wr.flush();
				wr.close();
	        }
	        else {
	        	connection.connect();
	        }
	
	        if (verifyResponse(connection.getResponseCode())) {
	        	return read(connection);
	        }
	        else throw new HttpException("HTTP status code " + connection.getResponseCode() + ": " + connection.getResponseMessage());
        
	    } finally {
	    	if (connection != null) {
	    	   try {
	              	connection.disconnect();
	          	} catch (Exception e) {
	    	        throw new HttpException("Unknown exception while closing connection: " + e.getMessage());
	          	}
	       	}
	    }
	}
	
	private HttpEmoncmsResponse read(HttpURLConnection connection) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        
        String line;
        while ((line = br.readLine()) != null && 
        		!Thread.currentThread().isInterrupted()) {
        	
            sb.append(line);
        }
        br.close();

        if (sb.length() != 0 && !sb.toString().isEmpty()) {
            return new HttpEmoncmsResponse(sb.toString());
        }
        else return null;
	}
	
	private boolean verifyResponse(int httpStatus) throws HttpException {
		switch (httpStatus) {
        case HttpURLConnection.HTTP_OK:
            return true;
        default:
        	return false;
		}
	}
}
