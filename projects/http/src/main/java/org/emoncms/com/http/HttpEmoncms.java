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
package org.emoncms.com.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.emoncms.Emoncms;
import org.emoncms.Feed;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.json.Const;
import org.emoncms.com.http.json.JsonData;
import org.emoncms.com.http.json.JsonFeed;
import org.emoncms.com.http.json.JsonInput;
import org.emoncms.com.http.json.JsonInputConfig;
import org.emoncms.com.http.json.JsonInputList;
import org.emoncms.com.http.json.ToJson;
import org.emoncms.com.http.request.HttpCallable;
import org.emoncms.com.http.request.HttpEmoncmsRequest;
import org.emoncms.com.http.request.HttpEmoncmsResponse;
import org.emoncms.com.http.request.HttpRequestAction;
import org.emoncms.com.http.request.HttpRequestAuthentication;
import org.emoncms.com.http.request.HttpRequestCallbacks;
import org.emoncms.com.http.request.HttpRequestMethod;
import org.emoncms.com.http.request.HttpRequestParameters;
import org.emoncms.data.DataList;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Options;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;


public class HttpEmoncms implements Emoncms, HttpRequestCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(HttpEmoncms.class);

	private static final int TIMEOUT = 15000;

	private final String address;
	private String apiKey;

	private int maxThreads;
	private ThreadPoolExecutor executor = null;
	private ScheduledExecutorService scheduler = null;


	public HttpEmoncms(String address, String apiKey, int maxThreads) {
		
    	this.address = address;
    	this.apiKey = apiKey;
    	this.maxThreads = maxThreads;
    }

	public String getAddress() {
		return address;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int max) {
		this.maxThreads = max;
	}
	
	@Override
	public void start() throws EmoncmsUnavailableException {
    	
    	logger.info("Registering Energy Monitoring Content Management System connection \"{}\"", address);

		if (executor != null) {
			executor.shutdown();
		}
        NamedThreadFactory namedThreadFactory = new NamedThreadFactory("EmonJava HTTP request pool - thread-");
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads, namedThreadFactory);

		if (scheduler != null) {
			scheduler.shutdown();
		}
		scheduler = Executors.newScheduledThreadPool(maxThreads);
    	
		// Verify the connection to the given address
    	try {
    		HttpEmoncmsRequest request = new HttpEmoncmsRequest(address, null, null, null, HttpRequestMethod.GET);
    		submitRequest(request);
    		
		} catch (InterruptedException | ExecutionException e) {
        	throw new EmoncmsUnavailableException("Unable to connect to \"" + address + "\": " + e);
        }
	}
	
	@Override
	public void stop() {
		
		logger.info("Shutting emoncms connection \"{}\" down", address);
    	
		if (executor != null) {
			executor.shutdown();
		}
	}

	@Override
	public void post(String node, String name, Timevalue timevalue, String devicekey) throws EmoncmsException {

		HttpRequestAuthentication authentication = new HttpRequestAuthentication(Const.DEVICE_KEY, devicekey);
		post(node, name, timevalue, authentication);
	}

	@Override
	public void post(String node, String name, Timevalue timevalue) throws EmoncmsException {

		HttpRequestAuthentication authentication = null;
		if (apiKey != null) {
			authentication = new HttpRequestAuthentication(Const.API_KEY, apiKey);
		}
		post(node, name, timevalue, authentication);
	}
	
	private void post(String node, String name, Timevalue timevalue, HttpRequestAuthentication authentication) throws EmoncmsException {

		logger.debug("Requesting to post {} for input \"{}\" of node \"{}\"", timevalue, name, node);

		HttpRequestAction action = new HttpRequestAction("post");
		action.addParameter(Const.NODE, node);
		if (timevalue.getTime() != null && timevalue.getTime() > 0) {
			// Posted UNIX time values need to be sent in seconds
			action.addParameter(Const.TIME, timevalue.getTime()/1000);
		}
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		ToJson json = new ToJson();
		json.addDouble(name, timevalue.getValue());
		parameters.addParameter(Const.DATA, json);
		
		sendRequest("input", authentication, action, parameters, HttpRequestMethod.POST);
	}

	@Override
	public void post(String node, Long time, List<Namevalue> namevalues, String devicekey) throws EmoncmsException {

		HttpRequestAuthentication authentication = new HttpRequestAuthentication(Const.DEVICE_KEY, devicekey);
		post(node, time, namevalues, authentication);
	}

	@Override
	public void post(String node, Long time, List<Namevalue> namevalues) throws EmoncmsException {
		
		HttpRequestAuthentication authentication = null;
		if (apiKey != null) {
			authentication = new HttpRequestAuthentication(Const.API_KEY, apiKey);
		}
		post(node, time, namevalues, authentication);
	}

	private void post(String node, Long time, List<Namevalue> namevalues, HttpRequestAuthentication authentication) throws EmoncmsException {

		logger.debug("Requesting to post values for {} inputs", namevalues.size());
		
		HttpRequestAction action = new HttpRequestAction("post");
		action.addParameter(Const.NODE, node);
		if (time != null && time > 0) {
			// Posted UNIX time values need to be sent in seconds
			action.addParameter(Const.TIME, time/1000);
		}
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		ToJson json = new ToJson();
		for (Namevalue namevalue : namevalues) {
			json.addDouble(namevalue.getName(), namevalue.getValue());
		}
		parameters.addParameter(Const.DATA, json);
		
		sendRequest("input", authentication, action, parameters, HttpRequestMethod.POST);
	}

	@Override
	public void post(DataList data) throws EmoncmsException {

		logger.debug("Requesting to post values for {} nodes", data.size());
		
		HttpRequestAction action = new HttpRequestAction("bulk");
		JsonData json = new JsonData(data);
		if (json.getTime() != null && json.getTime() > 0) {
			// Posted UNIX time values need to be sent in seconds
			action.addParameter(Const.TIME, json.getTime()/1000);
		}
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		parameters.addParameter(Const.DATA, json.toString());
		
		sendRequest("input", action, parameters, HttpRequestMethod.POST);
	}

	@Override
	public List<Input> getInputList(String node) throws EmoncmsException {
		logger.debug("Requesting input list for node \"{}\"", node);

		HttpRequestAction action = new HttpRequestAction("get_inputs");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("input", action, parameters, HttpRequestMethod.GET);
		try {
			JsonInputList jsonInputList = response.getInputConfigList(node);
			
			List<Input> inputList = new ArrayList<Input>(jsonInputList.size());
			for (JsonInputConfig jsonInput : jsonInputList) {
				
				ProcessList processList = new ProcessList(jsonInput.getProcessList());
				HttpInput input = new HttpInput(this,
						jsonInput.getId(), jsonInput.getNodeid(), jsonInput.getName(), null, processList, null);
				
				inputList.add(input);
			}
			return inputList;
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public List<Input> getInputList() throws EmoncmsException {
		
		logger.debug("Requesting input list");

		HttpRequestAction action = new HttpRequestAction("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("input", action, parameters, HttpRequestMethod.GET);
		try {
			List<JsonInput> jsonInputList = response.getInputList();

			List<Input> inputList = new ArrayList<Input>(jsonInputList.size());
			for (JsonInput jsonInput : jsonInputList) {
				
				ProcessList processList = new ProcessList(jsonInput.getProcessList());
				Timevalue timevalue = new Timevalue(jsonInput.getTime(), jsonInput.getValue());
				HttpInput input = new HttpInput(this,
						jsonInput.getId(), jsonInput.getNodeid(), jsonInput.getName(), 
						jsonInput.getDescription(), processList, timevalue);
				
				inputList.add(input);
			}
			return inputList;
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public Input getInput(String node, String name) throws EmoncmsException {

		logger.debug("Requesting input \"{}\" for node \"{}\"", name, node);

		HttpRequestAction action = new HttpRequestAction("get_inputs");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("input", action, parameters, HttpRequestMethod.GET);
		try {
			JsonInputConfig jsonInputConfig = response.getInputConfig(node, name);
			
			ProcessList processList = new ProcessList(jsonInputConfig.getProcessList());
			return new HttpInput(this,
					jsonInputConfig.getId(), jsonInputConfig.getNodeid(), jsonInputConfig.getName(), null, processList, null);
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public Input getInput(int id) throws EmoncmsException {

		logger.debug("Requesting input with id: {}", id);

		HttpRequestAction action = new HttpRequestAction("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("input", action, parameters, HttpRequestMethod.GET);
		try {
			List<JsonInput> jsonInputList = response.getInputList();

			for (JsonInput jsonInput : jsonInputList) {
				if (jsonInput.getId() == id) {
					ProcessList processList = new ProcessList(jsonInput.getProcessList());
					Timevalue timevalue = new Timevalue(jsonInput.getTime(), jsonInput.getValue());
					return new HttpInput(this,
							jsonInput.getId(), jsonInput.getNodeid(), jsonInput.getName(), 
							jsonInput.getDescription(), processList, timevalue);
				}
			}
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
		return null;
	}

	@Override
	public List<Feed> getFeedList() throws EmoncmsException {
		
		logger.debug("Requesting feed list");

		HttpRequestAction action = new HttpRequestAction("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("feed", action, parameters, HttpRequestMethod.GET);
		try {
			List<JsonFeed> jsonFeedList = response.getFeedList();

			List<Feed> feedList = new ArrayList<Feed>(jsonFeedList.size());
			for (JsonFeed jsonFeed : jsonFeedList) {
				Timevalue timevalue = null;
				if (jsonFeed.getTime() != null && jsonFeed.getValue() != null) {
					timevalue = new Timevalue(jsonFeed.getTime(), jsonFeed.getValue());
				}
				ProcessList processList = new ProcessList(jsonFeed.getProcessList());
				HttpFeed feed = new HttpFeed(this, jsonFeed.getId(), 
						jsonFeed.getName(), jsonFeed.getTag(), jsonFeed.isPublic(), jsonFeed.getSize(),
						Datatype.getEnum(jsonFeed.getDatatype()), Engine.getEnum(jsonFeed.getEngine()),
						processList, timevalue);
				
				feedList.add(feed);
			}
			return feedList;
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public Feed getFeed(int id) throws EmoncmsException {

		logger.debug("Requesting feed with id: {}", id);

		HttpRequestAction action = new HttpRequestAction("aget");
		action.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("feed", action, parameters, HttpRequestMethod.GET);
		try {
			JsonFeed jsonFeed = response.getFeed();
			Timevalue timevalue = null;
			if (jsonFeed.getTime() != null && jsonFeed.getValue() != null) {
				timevalue = new Timevalue(jsonFeed.getTime(), jsonFeed.getValue());
			}
			HttpFeedData feed = new HttpFeedData(this, jsonFeed.getId(), 
					jsonFeed.getName(), jsonFeed.getTag(), jsonFeed.isPublic(), jsonFeed.getSize(),
					Datatype.getEnum(jsonFeed.getDatatype()), Engine.getEnum(jsonFeed.getEngine()),
					timevalue);
			
			return feed;
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public Map<Feed, Double> getFeedValues(List<Feed> feeds) throws EmoncmsException {
		
		logger.debug("Requesting to get latest values for {} feeds", feeds.size());

		LinkedList<Feed> feedList = new LinkedList<Feed>();
		StringBuilder idsBuilder = new StringBuilder();
		Iterator<Feed> iteratorFeedList = feeds.iterator();
		while (iteratorFeedList.hasNext()) {
			Feed feed = iteratorFeedList.next();
			feedList.add(feed);
			
			idsBuilder.append(feed.getId());

        	if (iteratorFeedList.hasNext()) {
        		idsBuilder.append(',');
        	}
		}
		
		HttpRequestAction action = new HttpRequestAction("fetch");
		action.addParameter(Const.IDS, idsBuilder.toString());
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("feed", action, parameters, HttpRequestMethod.GET);
		try {
			return response.getValues(feedList);
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public int newFeed(String name, String tag, Datatype type, Engine engine, Options options) throws EmoncmsException {
		
		logger.debug("Requesting to add feed \"{}\"", name);

		HttpRequestAction action = new HttpRequestAction("create");
		action.addParameter(Const.NAME, name);
		action.addParameter(Const.TAG, tag);
		action.addParameter(Const.DATATYPE, type.getValue());
		action.addParameter(Const.ENGINE, engine.getValue());
		if (options != null && !options.isEmpty()) {
			ToJson json = new ToJson();
			json.addOptions(options);
			action.addParameter(Const.OPTIONS, json);
		}
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("feed", action, parameters, HttpRequestMethod.GET);
		try {
    		return Integer.valueOf(response.getString(Const.FEEDID));
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public HttpEmoncmsResponse onRequest(String parent, HttpRequestAuthentication authentication, HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method) throws EmoncmsException {
		return sendRequest(parent, authentication, action, parameters, method);
	}

	@Override
	public HttpEmoncmsResponse onRequest(String parent, HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method) throws EmoncmsException {
		return sendRequest(parent, action, parameters, method);
	}
	
	private HttpEmoncmsResponse sendRequest(String path, HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method) throws EmoncmsException {
		HttpRequestAuthentication authentication = null;
		if (apiKey != null) {
			authentication = new HttpRequestAuthentication(Const.API_KEY, apiKey);
		}
		return sendRequest(path, authentication, action, parameters, method);
	}
	
	private synchronized HttpEmoncmsResponse sendRequest(String path, HttpRequestAuthentication authentication, HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method) throws EmoncmsException {
		String url = address + path.toLowerCase();
		if (!url.endsWith("/"))
			url += "/";
		
		HttpEmoncmsRequest request = new HttpEmoncmsRequest(url, authentication, 
				action, parameters, method);

        try {
    		HttpEmoncmsResponse response = submitRequest(request);
    		if (response != null) {
	    		if (response.isSuccess()) {
	    			return response;
	    		}
	    		else {
	    			throw new EmoncmsException("Emoncms request responsed \"false\"");
	    		}
    		}
    		else throw new EmoncmsException("Emoncms request failed");
	    		
		} catch (InterruptedException | ExecutionException | JsonSyntaxException e) {
			throw new EmoncmsException("Error while requesting \"" + request.toString() + "\" :" + e);
		}
	}

	private HttpEmoncmsResponse submitRequest(HttpEmoncmsRequest request) throws InterruptedException, ExecutionException {
		long start = System.currentTimeMillis();
		
		HttpCallable task = new HttpCallable(request);
		
		final Future<HttpEmoncmsResponse> submit = executor.submit(task);
		final ScheduledFuture<?> timeout = scheduler.schedule(new Runnable(){
		     public void run(){
		    	 submit.cancel(true);
		     }
		}, TIMEOUT, TimeUnit.MILLISECONDS);
		
		HttpEmoncmsResponse response = submit.get();
		timeout.cancel(true);

    	if (logger.isTraceEnabled()) {
    		logger.trace("Request took {}ms to respond", System.currentTimeMillis() - start);
    	}
		return response;
	}

	private class NamedThreadFactory implements ThreadFactory {

	    private final String name;
	    private final AtomicInteger counter = new AtomicInteger(0);

	    public NamedThreadFactory(String name) {
	        this.name = name;
	    }

	    @Override
	    public Thread newThread(Runnable r) {
	        String threadName = name + counter.incrementAndGet();
	        return new Thread(r, threadName);
	    }
	}
}
