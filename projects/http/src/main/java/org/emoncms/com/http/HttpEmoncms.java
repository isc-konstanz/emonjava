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
import java.util.concurrent.TimeUnit;

import org.emoncms.Emoncms;
import org.emoncms.Feed;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.HttpFeed.HttpFeedCallbacks;
import org.emoncms.com.http.HttpInput.HttpInputCallbacks;
import org.emoncms.com.http.json.Const;
import org.emoncms.com.http.json.JsonData;
import org.emoncms.com.http.json.JsonFeed;
import org.emoncms.com.http.json.JsonInput;
import org.emoncms.com.http.json.JsonInputConfig;
import org.emoncms.com.http.json.JsonInputList;
import org.emoncms.com.http.json.ToJson;
import org.emoncms.data.DataList;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Options;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpEmoncms implements Emoncms, HttpInputCallbacks, HttpFeedCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(HttpEmoncms.class);

	private static final int TIMEOUT = 15000;

	private final String address;
	private final String apiKey;

	private ScheduledExecutorService executor = null;
	private int maxThreads;


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

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int max) {
		this.maxThreads = max;
	}
	
	@Override
	public void start() throws EmoncmsUnavailableException {
    	
    	logger.info("Registering energy monitoring content management \"{}\"", address);

		if (executor != null) {
			executor.shutdown();
		}
		executor = Executors.newScheduledThreadPool(maxThreads);
    	
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
		
		logger.info("Shutting energy monitoring content management \"{}\" down", address);
    	
		if (executor != null) {
			executor.shutdown();
		}
	}

	@Override
	public void post(String node, String name, Timevalue timevalue) throws EmoncmsException {

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
		
		sendRequest("input", action, parameters, HttpRequestMethod.POST);
	}
	
	public void post(String node, Long time, List<Namevalue> namevalues) throws EmoncmsException {

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
		
		sendRequest("input", action, parameters, HttpRequestMethod.POST);
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
		if (node != null) {
			logger.debug("Requesting input list for node \"{}\"", node);
		}

		HttpRequestAction action = new HttpRequestAction("get_inputs");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("input", action, parameters, HttpRequestMethod.GET);
		try {
			JsonInputList jsonInputList = response.getInputConfigList(node);
			
			List<Input> inputList = new ArrayList<Input>(jsonInputList.size());
			for (JsonInputConfig jsonInput : jsonInputList) {
				HttpInput input = new HttpInput(this,
						jsonInput.getId(), jsonInput.getNodeid(), jsonInput.getName());
				
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

		return getInputList(null);
	}

	@Override
	public List<Input> loadInputList() throws EmoncmsException {
		
		logger.debug("Requesting to load input list");

		HttpRequestAction action = new HttpRequestAction("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("input", action, parameters, HttpRequestMethod.GET);
		try {
			List<JsonInput> jsonInputList = response.getInputList();

			List<Input> inputList = new ArrayList<Input>(jsonInputList.size());
			for (JsonInput jsonInput : jsonInputList) {
				
				ProcessList processList = new ProcessList(jsonInput.getProcessList());
				Timevalue timevalue = new Timevalue(jsonInput.getTime(), jsonInput.getValue());
				HttpInputData input = new HttpInputData(this,
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
	public void cleanInputList() throws EmoncmsException {

		logger.debug("Requesting to clean input list");
		
		HttpRequestAction action = new HttpRequestAction("clean");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("input", action, parameters, HttpRequestMethod.GET);
		
		String result = response.getResponse();
		if (logger.isDebugEnabled()) {
			logger.debug("{} deleted inputs: {}", 
					result.substring(0, result.toLowerCase().indexOf("delete")), 
					result.substring(result.indexOf(":")+1, result.indexOf("<br>")-1));
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
			return new HttpInput(this,
					jsonInputConfig.getId(), jsonInputConfig.getNodeid(), jsonInputConfig.getName());
							
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public Input loadInput(int id) throws EmoncmsException {

		logger.debug("Requesting to load input with id: ", id);
		
		List<Input> inputList = loadInputList();
		for (Input input : inputList) {
			if (input.getId() == id) {
				return input;
			}
		}
		throw new EmoncmsException("Input not found for id: " + id);
	}

	@Override
	public List<Feed> getFeedList() throws EmoncmsException {
		
		logger.debug("Requesting feed list");

		List<JsonFeed> jsonFeedList = getJsonFeedList();

		List<Feed> feedList = new ArrayList<Feed>(jsonFeedList.size());
		for (JsonFeed jsonFeed : jsonFeedList) {
			HttpFeed feed = new HttpFeed(this, jsonFeed.getId());
			
			feedList.add(feed);
		}
		return feedList;
	}

	@Override
	public List<Feed> loadFeedList() throws EmoncmsException {
		
		logger.debug("Requesting to load feed list");

		List<JsonFeed> jsonFeedList = getJsonFeedList();

		List<Feed> feedList = new ArrayList<Feed>(jsonFeedList.size());
		for (JsonFeed jsonFeed : jsonFeedList) {
			Timevalue timevalue = null;
			if (jsonFeed.getTime() != null && jsonFeed.getValue() != null) {
				timevalue = new Timevalue(jsonFeed.getTime(), jsonFeed.getValue());
			}
			HttpFeedData feed = new HttpFeedData(this, jsonFeed.getId(), 
					jsonFeed.getName(), jsonFeed.getTag(), jsonFeed.isPublic(), jsonFeed.getSize(),
					Datatype.getEnum(jsonFeed.getDatatype()), Engine.getEnum(jsonFeed.getEngine()),
					timevalue);
			
			feedList.add(feed);
		}
		return feedList;
	}

	private List<JsonFeed> getJsonFeedList() throws EmoncmsException {

		HttpRequestAction action = new HttpRequestAction("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = sendRequest("feed", action, parameters, HttpRequestMethod.GET);
		try {
			return response.getFeedList();
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public Feed getFeed(int id) {

		logger.debug("Returning new feed instance with id: {}", id);
		return new HttpFeed(this, id);
	}

	@Override
	public HttpFeedData loadFeed(int id) throws EmoncmsException {

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
	public HttpEmoncmsResponse onInputRequest(HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method) throws EmoncmsException {
		return this.sendRequest("input", action, parameters, method);
	}

	@Override
	public HttpEmoncmsResponse onFeedRequest(HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method) throws EmoncmsException {
		return this.sendRequest("feed", action, parameters, method);
	}
	
	private HttpEmoncmsResponse sendRequest(String path, HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method) throws EmoncmsException {
		String url = address + path.toLowerCase();
		if (!url.endsWith("/"))
			url += "/";
		
		HttpEmoncmsRequest request = new HttpEmoncmsRequest(url, apiKey, 
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
	    		
		} catch (InterruptedException | ExecutionException e) {
			throw new EmoncmsException("Error while requesting \"" + request.toString() + "\" :" + e);
		}
	}

	private HttpEmoncmsResponse submitRequest(HttpEmoncmsRequest request) throws InterruptedException, ExecutionException {
		long start = System.currentTimeMillis();
		
		HttpCallable task = new HttpCallable(request);
		Future<HttpEmoncmsResponse> submit;
		synchronized (executor) {
			submit = executor.submit(task);
			executor.schedule(new Runnable(){
			     public void run(){
			    	 submit.cancel(true);
			     }
			}, TIMEOUT, TimeUnit.MILLISECONDS);
		}
		HttpEmoncmsResponse response = submit.get();

    	if (logger.isTraceEnabled()) {
    		logger.trace("Request took {}ms to respond", System.currentTimeMillis() - start);
    	}
		return response;
	}
}
