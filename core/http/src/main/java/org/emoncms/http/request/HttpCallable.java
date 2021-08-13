/* 
 * Copyright 2016-2021 ISC Konstanz
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
package org.emoncms.http.request;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.emoncms.EmoncmsException;
import org.emoncms.http.HttpException;

public class HttpCallable implements Callable<HttpResponse> {

	private final static Charset CHARSET = StandardCharsets.UTF_8;

	private final int timeout;

	private final HttpRequest request;
	private HttpURLConnection connection = null;
	private InputStream stream = null;

	private volatile long start;

	public HttpCallable(HttpRequest request, int timeout) {
		this.request = request;
		this.timeout = timeout;
	}

	public HttpRequest getRequest() {
		return request;
	}

	@Override
	public HttpResponse call() throws Exception {
		start = System.currentTimeMillis();
		
		HttpMethod method = request.getMethod();
		switch (method) {
		case GET:
			return get(request);
		case POST:
			return post(request);
		default:
		  	throw new EmoncmsException("No HTTP request method " + method.toString() + "implemented");
		}
	}

	private HttpResponse get(HttpRequest request) throws IOException {
		try {
			URL url = new URL(request.parse(CHARSET));
			connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestMethod(HttpMethod.GET.name());
			connection.setRequestProperty("Charset", CHARSET.name());
			connection.setRequestProperty("Accept-Charset", CHARSET.name());
			connection.setRequestProperty("Connection", "Close");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset="+CHARSET.name());
			connection.setRequestProperty("Content-length", "0");
			
			connection.setInstanceFollowRedirects(false);
			connection.setAllowUserInteraction(false);
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.connect();
			
			if (verifyResponse(connection.getResponseCode())) {
				stream = connection.getInputStream();
				
				return read();
			}
			throw new HttpException("HTTP status code " + connection.getResponseCode() + ": " + connection.getResponseMessage());
		
		} finally {
			try {
				if (stream != null) {
					stream.close();
					stream = null;
				}
				if (connection != null) {
				  	connection.disconnect();
				  	connection = null;
			   	}
		  	} catch (Exception e) {
				throw new HttpException("Unknown exception while closing connection: " + e.getMessage());
		  	}
		}
	}

	private HttpResponse post(HttpRequest request) throws IOException {
		try {
			URL url = new URL(request.parse(CHARSET));
			
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(HttpMethod.POST.name());
			connection.setRequestProperty("Connection", "Close");
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
			
			if (content != null) {
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.write(content);
				wr.flush();
				wr.close();
			}
			connection.connect();
			
			if (verifyResponse(connection.getResponseCode())) {
				stream = connection.getInputStream();
				
				return read();
			}
			throw new HttpException("HTTP status code " + connection.getResponseCode() + ": " + connection.getResponseMessage());
		
		} finally {
			try {
				if (stream != null) {
					stream.close();
					stream = null;
				}
				if (connection != null) {
				  	connection.disconnect();
				  	connection = null;
			   	}
		  	} catch (Exception e) {
				throw new HttpException("Unknown exception while closing connection: " + e.getMessage());
		  	}
		}
	}

	private HttpResponse read() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, CHARSET.name()));
		
		StringBuilder sb = new StringBuilder();
		String line;
		while (!Thread.currentThread().isInterrupted() && 
				System.currentTimeMillis() - start <= timeout && 
				(line = reader.readLine()) != null) {
			
			sb.append(line);
		}
		if (sb.length() != 0 && !sb.toString().isEmpty()) {
			return new HttpResponse(sb.toString());
		}
		throw new IOException("Nothing received");
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
