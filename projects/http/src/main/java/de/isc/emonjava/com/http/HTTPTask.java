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
package de.isc.emonjava.com.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emonjava.com.EmoncmsResponse;
import de.isc.emonjava.com.RequestParameter;


public class HTTPTask extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(HTTPTask.class);
	
	private final String request;
	private final List<RequestParameter> parameters;

	private final CountDownLatch taskFinishedSignal;
	private EmoncmsResponse response = null;
	
	/**
	 * The Tasks current callback object, which is notified of task events
	 */
	private EmoncmsTaskCallbacks callback;
	
	/**
	 * Interface used by {@link HTTPTask} to notify the {@link EmoncmsHTTP} handler about task events
	 */
	public static interface EmoncmsTaskCallbacks {
		
		void onConnectionFailure(HTTPTask task);
	}
	
	public HTTPTask(EmoncmsTaskCallbacks callback, CountDownLatch taskFinishedSignal, 
			String request, List<RequestParameter> parameters) {
		this.callback = (EmoncmsTaskCallbacks) callback;
		this.taskFinishedSignal = taskFinishedSignal;
		
		this.request = request;
		this.parameters = parameters;
	}
	
	public String getRequest() {
		return request;
	}

	public List<RequestParameter> getParameters() {
		return parameters;
	}

	public EmoncmsResponse getResponse() {
		return response;
	}
	
	@Override
	public final void run() {
		HttpURLConnection connection = null;
        try {
            StringBuilder url = new StringBuilder();
            url.append(request);
            
            StringBuilder postParams = null;
            if (parameters != null) {
                for (RequestParameter p : parameters) {
                    switch (p.getMethod()) {
                        case GET:
                        	url.append("&");
                        	url.append(p.parseParameter());
                        	break;
                        case POST:
                        	if (postParams == null) {
                        		postParams = new StringBuilder();
                        	}
                            if (postParams.length() != 0) postParams.append('&');
                        	postParams.append(p.parseParameter());
                        	break;
                    }
                }
            }
            
            URL u = new URL(url.toString());
            connection = (HttpURLConnection) u.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setAllowUserInteraction(false);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Charset", "UTF-8");
            
            if (postParams != null) {
                byte[] postData = postParams.toString().getBytes(Charset.forName("UTF-8"));
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", String.valueOf(postData.length));
                connection.setRequestMethod("POST");

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.write(postData);
            }
            else {
                connection.setRequestProperty("Content-length", "0");
                connection.setRequestMethod("GET");
                connection.connect();
            }
            
            int status = connection.getResponseCode();
            switch (status) {
                case 200:
                case 201:
            		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line); //+"\n");
                    }
                    br.close();

                    response = new EmoncmsResponse(sb.toString());
            }

        } catch (IOException e) {
        	callback.onConnectionFailure(this);
        } finally {
        	if (connection != null) {
        	   try {
                  	connection.disconnect();
              	} catch (Exception e) {
              		logger.warn("Unknown exception while closing connection: " + e.getMessage());
              	}
           	}
	   		if (taskFinishedSignal != null) {
	   			taskFinishedSignal.countDown();
	   		}
        }
	}
}
