/* 
 * Copyright 2016-18 ISC Konstanz
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
package org.emoncms.com.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.emoncms.Emoncms;
import org.emoncms.Feed;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.json.Const;
import org.emoncms.com.http.json.JsonFeed;
import org.emoncms.com.http.json.JsonInput;
import org.emoncms.com.http.json.JsonInputConfig;
import org.emoncms.com.http.json.JsonInputList;
import org.emoncms.com.http.json.ToJsonArray;
import org.emoncms.com.http.json.ToJsonObject;
import org.emoncms.com.http.request.HttpCallable;
import org.emoncms.com.http.request.HttpEmoncmsRequest;
import org.emoncms.com.http.request.HttpEmoncmsResponse;
import org.emoncms.com.http.request.HttpRequestCallbacks;
import org.emoncms.com.http.request.HttpRequestMethod;
import org.emoncms.com.http.request.HttpRequestParameters;
import org.emoncms.com.http.request.HttpRequestURI;
import org.emoncms.data.Authentication;
import org.emoncms.data.Data;
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

	private static final int TIMEOUT = 30000;

	private final String address;
	private Authentication authentication;

	private int maxThreads;
	private ThreadPoolExecutor executor = null;


	public HttpEmoncms(String address, Authentication authentication, int maxThreads) {
		
		this.address = address;
		this.authentication = authentication;
		this.maxThreads = maxThreads;
	}

	public String getAddress() {
		return address;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int max) {
		this.maxThreads = max;
	}

	@Override
	public void start() throws EmoncmsUnavailableException {
		logger.info("Initializing Energy Monitoring Content Management System communication \"{}\"", address);
		initialize();
	}

	private void initialize() {
		// The HttpURLConnection implementation is in older JREs somewhat buggy with keeping connections alive. 
		// To avoid this, the http.keepAlive system property can be set to false. 
		System.setProperty("http.keepAlive", "false");
		
		if (executor != null) {
			executor.shutdown();
		}
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory("EmonJava HTTP request pool - thread-");
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads, namedThreadFactory);
	}

	@Override
	public void stop() {
		logger.info("Shutting emoncms connection \"{}\" down", address);
		
		if (executor != null) {
			executor.shutdown();
		}
	}

	@Override
	public void post(String node, String name, Timevalue timevalue) throws EmoncmsException {
		post(node, name, timevalue, authentication);
	}

	@Override
	public void post(String node, String name, Timevalue timevalue, Authentication authentication) throws EmoncmsException {
		logger.debug("Requesting to post {} for input \"{}\" of node \"{}\"", timevalue, name, node);

		HttpRequestURI uri = new HttpRequestURI("post");
		HttpRequestParameters parameters = new HttpRequestParameters();
		parameters.addParameter(Const.NODE, node);
		
		if (timevalue.getTime() != null && timevalue.getTime() > 0) {
			// Posted UNIX time values need to be sent in seconds
			Long time = TimeUnit.MILLISECONDS.toSeconds(timevalue.getTime().longValue());
			parameters.addParameter(Const.TIME, time);
		}
		
		ToJsonObject json = new ToJsonObject();
		json.addDouble(name, timevalue.getValue());
		parameters.addParameter(Const.FULLJSON, json);
		
		onPost("input", uri, parameters, authentication);
	}

	@Override
	public void post(String node, Long time, List<Namevalue> namevalues) throws EmoncmsException {
		
		post(node, time, namevalues, authentication);
	}

	@Override
	public void post(String node, Long time, List<Namevalue> namevalues, Authentication authentication) throws EmoncmsException {
		logger.debug("Requesting to post values for {} inputs", namevalues.size());
		
		HttpRequestURI uri = new HttpRequestURI("post");
		HttpRequestParameters parameters = new HttpRequestParameters();
		parameters.addParameter(Const.NODE, node);
		
		if (time != null && time > 0) {
			// Posted UNIX time values need to be sent in seconds
			time = TimeUnit.MILLISECONDS.toSeconds(time);
			parameters.addParameter(Const.TIME, time);
		}
		
		ToJsonObject json = new ToJsonObject();
		for (Namevalue namevalue : namevalues) {
			json.addDouble(namevalue.getName(), namevalue.getValue());
		}
		parameters.addParameter(Const.FULLJSON, json);
		
		onPost("input", uri, parameters, authentication);
	}

	@Override
	public void post(DataList dataList) throws EmoncmsException {
		post(dataList, authentication);
	}

	@Override
	public void post(DataList dataList, Authentication authentication) throws EmoncmsException {
		logger.debug("Requesting to bulk post {} data sets", dataList.size());
		
		HttpRequestURI uri = new HttpRequestURI("bulk");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		// Posted UNIX time values need to be sent in seconds
		long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		parameters.addParameter(Const.TIME, time);
		
		dataList.sort();
		
		ToJsonArray json = new ToJsonArray();
		for (Data data : dataList) {
			json.addData(time, data);
		}
		parameters.addParameter(Const.DATA, json.toString());
		
		onPost("input", uri, parameters, authentication);
	}

	@Override
	public List<Input> getInputList(String node) throws EmoncmsException {
		logger.debug("Requesting input list for node \"{}\"", node);

		HttpRequestURI uri = new HttpRequestURI("get_inputs");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = onGet("input", uri, parameters);
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

		HttpRequestURI uri = new HttpRequestURI("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = onGet("input", uri, parameters);
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

		HttpRequestURI uri = new HttpRequestURI("get_inputs");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = onGet("input", uri, parameters);
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

		HttpRequestURI uri = new HttpRequestURI("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = onGet("input", uri, parameters);
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

		HttpRequestURI uri = new HttpRequestURI("list.json");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = onGet("feed", uri, parameters);
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

		HttpRequestURI uri = new HttpRequestURI("aget.json");
		uri.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = onGet("feed", uri, parameters);
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
		
		HttpRequestURI uri = new HttpRequestURI("fetch.json");
		uri.addParameter(Const.IDS, idsBuilder.toString());
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = onGet("feed", uri, parameters);
		try {
			return response.getValues(feedList);
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public int newFeed(String name, String tag, Datatype type, Engine engine, Options options) throws EmoncmsException {
		logger.debug("Requesting to add feed \"{}\"", name);

		HttpRequestURI uri = new HttpRequestURI("create.json");
		uri.addParameter(Const.NAME, name);
		uri.addParameter(Const.TAG, tag);
		uri.addParameter(Const.DATATYPE, type.getValue());
		uri.addParameter(Const.ENGINE, engine.getValue());
		if (options != null && !options.isEmpty()) {
			ToJsonObject json = new ToJsonObject();
			json.addOptions(options);
			uri.addParameter(Const.OPTIONS, json);
		}
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = onGet("feed", uri, parameters);
		try {
			return Integer.valueOf(response.getString(Const.FEEDID));
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public HttpEmoncmsResponse onGet(String parent, HttpRequestURI uri, HttpRequestParameters parameters, 
			Authentication authentication) throws EmoncmsException {
		return sendRequest(HttpRequestMethod.GET, parent, uri, parameters, authentication);
	}

	@Override
	public HttpEmoncmsResponse onGet(String parent, HttpRequestURI uri, HttpRequestParameters parameters) 
			throws EmoncmsException {
		return sendRequest(HttpRequestMethod.GET, parent, uri, parameters);
	}

	@Override
	public HttpEmoncmsResponse onPost(String parent, HttpRequestURI uri, HttpRequestParameters parameters, 
			Authentication authentication) throws EmoncmsException {
		return sendRequest(HttpRequestMethod.POST, parent, uri, parameters, authentication);
	}

	@Override
	public HttpEmoncmsResponse onPost(String parent, HttpRequestURI uri, HttpRequestParameters parameters) 
			throws EmoncmsException {
		return sendRequest(HttpRequestMethod.POST, parent, uri, parameters);
	}

	private HttpEmoncmsResponse sendRequest(HttpRequestMethod method, String path, 
			HttpRequestURI uri, HttpRequestParameters parameters) throws EmoncmsException {
		return sendRequest(method, path, uri, parameters, authentication);
	}

	private HttpEmoncmsResponse sendRequest(HttpRequestMethod method, String path, 
			HttpRequestURI uri, HttpRequestParameters parameters, 
			Authentication authentication) throws EmoncmsException {
		
		String url = address + path.toLowerCase();
		if (!url.endsWith("/"))
			url += "/";
		
		HttpEmoncmsRequest request = new HttpEmoncmsRequest(method, url, uri, parameters, authentication);
		HttpEmoncmsResponse response = submitRequest(request);
		if (response != null) {
			if (response.isSuccess()) {
				return response;
			}
			throw new EmoncmsException("Emoncms request responsed \"false\"");
		}
		throw new EmoncmsException("Emoncms request failed");
	}

	private synchronized HttpEmoncmsResponse submitRequest(HttpEmoncmsRequest request) throws EmoncmsException {
		if (logger.isTraceEnabled()) {
			logger.trace("Requesting \"{}\"", request.toString());
		}
		long start = System.currentTimeMillis();
		
		try {
			HttpCallable task = new HttpCallable(request);
			final Future<HttpEmoncmsResponse> submit = executor.submit(task);
			try {
				HttpEmoncmsResponse response = submit.get(TIMEOUT, TimeUnit.MILLISECONDS);
				
				if (logger.isTraceEnabled()) {
					String rsp = "Returned null";
					if (response != null) {
						rsp = response.getResponse();
					}
					logger.trace("Received response after {}ms: {}", System.currentTimeMillis() - start, rsp);
				}
				return response;
			}
			catch (JsonSyntaxException e) {
				throw new EmoncmsException("Received invalid JSON response: " + e);
			}
			catch (CancellationException | TimeoutException e) {
				submit.cancel(true);
				throw new EmoncmsException("Aborted request \"" + request.toString() + "\": " + e);
			}
		} catch (InterruptedException | ExecutionException e) {
			initialize();
			throw new EmoncmsException("Energy Monitoring Content Management communication failed: " + e);
		}
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
