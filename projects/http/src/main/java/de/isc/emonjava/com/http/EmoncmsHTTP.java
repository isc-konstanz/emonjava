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

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emonjava.EmoncmsException;
import de.isc.emonjava.com.EmoncmsCommunication;
import de.isc.emonjava.com.EmoncmsResponse;
import de.isc.emonjava.com.RequestParameter;
import de.isc.emonjava.com.http.HTTPTask.EmoncmsTaskCallbacks;


public class EmoncmsHTTP implements EmoncmsCommunication, EmoncmsTaskCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(EmoncmsHTTP.class);
	
	private static final int SEND_RETRY_INTERVAL = 30000;
	
	private final String URL;
	private final String KEY;

	private ExecutorService executor = null;
	private volatile Timer timer = null;
	
	private final List<HTTPTask> queuedTasks = new LinkedList<HTTPTask>();
    
	
	public EmoncmsHTTP(String address, String apiKey) {
		String url;
		if (!address.startsWith("http://")) {
			url = "http://".concat(address);
		}
		else {
			url = address;
		}
    	this.URL = url.concat("emoncms/");
    	this.KEY = apiKey;

		executor = Executors.newCachedThreadPool();
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
	public void writeRequest(String request, List<RequestParameter> parameters) {
		executeTask(URL + request + "&apikey=" + KEY, parameters);
	}
	
	@Override
	public EmoncmsResponse sendRequest(String request, List<RequestParameter> parameters) throws EmoncmsException {
		CountDownLatch taskFinishedSignal = new CountDownLatch(1);
		
		HTTPTask task = new HTTPTask(this, taskFinishedSignal, URL + request + "&apikey=" + KEY, parameters);
		executor.execute(task);
		try {
			taskFinishedSignal.await();
		} catch (InterruptedException e) {
		}
		
		EmoncmsResponse response = task.getResponse();
		if (response != null) {
			return response;
		}
		throw new EmoncmsException("Error while connecting to \"" + request + "\"");
	}
	
	@Override
	public void onConnectionFailure(HTTPTask task) {
		StringBuilder request = new StringBuilder();
		request.append(task.getRequest());
		if (task.getParameters() != null) {
			for (RequestParameter r : task.getParameters()) {
				request.append("&");
				request.append(r.toString());
			}
		}
		logger.debug("Error sending request \"{}\"", request);
		
		synchronized (queuedTasks) {
			queuedTasks.add(task);

	    	if (timer == null) {
	    		LinkedList<HTTPTask> tasks = new LinkedList<HTTPTask>(queuedTasks);
	    		ResendTask resendTask = new ResendTask(tasks);
        		queuedTasks.clear();
        		
	    		timer = new Timer();
	            timer.schedule(resendTask, SEND_RETRY_INTERVAL);
			}
		}
	}
	
	private void executeTask(CountDownLatch taskFinishedSignal, String request, List<RequestParameter> parameters) {
		HTTPTask task = new HTTPTask(this, taskFinishedSignal, request, parameters);
		executor.execute(task);
	}
	
	private void executeTask(String request, List<RequestParameter> parameters) {
		executeTask(null, request, parameters);
	}
	
	private class ResendTask extends TimerTask {
		private final List<HTTPTask> tasks;
		
		public ResendTask(List<HTTPTask> tasks) {
			this.tasks = tasks;
		}
		
		@Override
		public void run() {
			logger.debug("Resending {} messages, failed to be sent", tasks.size());
			for (HTTPTask t : tasks) {
				if (logger.isTraceEnabled()) {
					StringBuilder request = new StringBuilder();
					request.append(t.getRequest());
					for (RequestParameter r : t.getParameters()) {
						request.append("&");
						request.append(r.toString());
					}
					logger.trace("Resending request \"{}\"", request);
				}
				executeTask(t.getRequest(), t.getParameters());
			}

			synchronized (queuedTasks) {
		    	timer.cancel();
		    	timer = null;
			}
		}
	}
}
