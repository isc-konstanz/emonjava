/* 
 * Copyright 2016-17 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.emoncms.com.http.request;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.emoncms.com.EmoncmsException;
import org.emoncms.com.http.HttpException;


public class HttpCallable implements Callable<HttpEmoncmsResponse> {
	
	private final static Charset CHARSET = StandardCharsets.UTF_8;
//	private final static int CONNECTION_TIMEOUT = 5000;
//	private final static int READ_TIMEOUT = 10000;

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
	        URL u = new URL(request.getRequest(CHARSET));
	        connection = (HttpURLConnection) u.openConnection();
	        
	        connection.setRequestMethod(HttpRequestMethod.GET.name());
	        connection.setRequestProperty("Charset", CHARSET.name());
	        connection.setRequestProperty("Accept-Charset", CHARSET.name());
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset="+CHARSET.name());
	        connection.setRequestProperty("Content-length", "0");
	        
	        connection.setInstanceFollowRedirects(false);
	        connection.setAllowUserInteraction(false);
	        connection.setUseCaches(false);
	        connection.setDoOutput(true);
	        connection.setDoInput(true);
//	        connection.setConnectTimeout(CONNECTION_TIMEOUT);
//	        connection.setReadTimeout(READ_TIMEOUT);
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
	        URL u = new URL(request.getRequest(CHARSET));
	        
	        connection = (HttpURLConnection) u.openConnection();
	        connection.setRequestMethod(HttpRequestMethod.POST.name());
	        connection.setRequestProperty("Charset", CHARSET.name());
	        connection.setRequestProperty("Accept-Charset", CHARSET.name());
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset="+CHARSET.name());
	        
        	byte[] content = null;
	        if (request.getParameters() != null) {
		        content = request.parseParameters(CHARSET).getBytes(CHARSET);
		        connection.setRequestProperty("Content-Length", Integer.toString(content.length));
	        }
	        else {
		        connection.setRequestProperty("Content-length", "0");
	        }
	        
	        connection.setInstanceFollowRedirects(false);
	        connection.setAllowUserInteraction(false);
	        connection.setUseCaches(false);
	        connection.setDoOutput(true);
	        connection.setDoInput(true);
//	        connection.setConnectTimeout(CONNECTION_TIMEOUT);
//	        connection.setReadTimeout(READ_TIMEOUT);
	
	        if (content != null) {
		        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.write(content);
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
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), CHARSET.name()));
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
