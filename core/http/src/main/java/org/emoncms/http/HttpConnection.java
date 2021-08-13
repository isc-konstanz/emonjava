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
package org.emoncms.http;

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
import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.Feed;
import org.emoncms.Input;
import org.emoncms.data.Authentication;
import org.emoncms.data.Data;
import org.emoncms.data.DataList;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Options;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.emoncms.http.json.Const;
import org.emoncms.http.json.JsonArrayBuilder;
import org.emoncms.http.json.JsonFeed;
import org.emoncms.http.json.JsonInput;
import org.emoncms.http.json.JsonInputConfig;
import org.emoncms.http.json.JsonInputList;
import org.emoncms.http.json.JsonObjectBuilder;
import org.emoncms.http.request.HttpCallable;
import org.emoncms.http.request.HttpCallbacks;
import org.emoncms.http.request.HttpMethod;
import org.emoncms.http.request.HttpParameters;
import org.emoncms.http.request.HttpQuery;
import org.emoncms.http.request.HttpRequest;
import org.emoncms.http.request.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

public class HttpConnection implements Emoncms, HttpCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(HttpConnection.class);

	private static final String TIMEOUT = System.getProperty(HttpConnection.class.
    		getPackage().getName().toLowerCase() + ".timeout", "30000");

	private final String domain;
	private Authentication authentication;

	private ThreadPoolExecutor executor = null;
	private int maxThreads;

	private volatile boolean closed = true;

	protected HttpConnection(String domain, Authentication authentication, int maxThreads) {
		this.domain = domain;
		this.authentication = authentication;
		this.maxThreads = maxThreads;
	}

	protected HttpConnection(HttpBuilder builder) {
		this(builder.domain, builder.authentication, builder.maxThreads);
	}

	public String getDomain() {
		return domain;
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
	public EmoncmsType getType() {
		return EmoncmsType.HTTP;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() {
		logger.info("Shutting emoncms HTTP connection \"{}\" down", domain);
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
		closed = true;
	}

	@Override
	public void open() throws EmoncmsUnavailableException {
		// The HttpURLConnection implementation is in older JREs somewhat buggy with keeping connections alive. 
		// To avoid this, the http.keepAlive system property can be set to false. 
		System.setProperty("http.keepAlive", "false");
		
		logger.info("Initializing emoncms HTTP connection \"{}\"", domain);
		initialize();
		closed = false;
	}

	private void initialize() {
		if (executor != null) {
			executor.shutdown();
		}
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory("EmonJava HTTP request pool - thread-");
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads, namedThreadFactory);
	}

	@Override
	public void post(String node, String name, Timevalue timevalue) throws EmoncmsException {
		post(node, name, timevalue, authentication);
	}

	public void post(String node, String name, Timevalue timevalue, Authentication authentication) throws EmoncmsException {
		logger.debug("Requesting to post {} for input \"{}\" of node \"{}\"", timevalue, name, node);
		
		HttpQuery query = new HttpQuery("input", "post");
		HttpParameters parameters = new HttpParameters();
		parameters.addParameter(Const.NODE, node);
		
		if (timevalue.getTime() != null && timevalue.getTime() > 0) {
			// Posted UNIX time values need to be sent in seconds
			Long time = TimeUnit.MILLISECONDS.toSeconds(timevalue.getTime().longValue());
			parameters.addParameter(Const.TIME, time);
		}
		
		JsonObjectBuilder json = new JsonObjectBuilder();
		json.addDouble(name, timevalue.getValue());
		parameters.addParameter(Const.FULLJSON, json);
		
		onPost(query, parameters, authentication);
	}

	@Override
	public void post(String node, Long time, List<Namevalue> namevalues) throws EmoncmsException {
		post(node, time, namevalues, authentication);
	}

	public void post(String node, Long time, List<Namevalue> namevalues, Authentication authentication) throws EmoncmsException {
		logger.trace("Requesting to post values for {} inputs", namevalues.size());
		
		HttpQuery query = new HttpQuery("input", "post");
		HttpParameters parameters = new HttpParameters();
		parameters.addParameter(Const.NODE, node);
		
		if (time != null && time > 0) {
			// Posted UNIX time values need to be sent in seconds
			time = TimeUnit.MILLISECONDS.toSeconds(time);
			parameters.addParameter(Const.TIME, time);
		}
		
		JsonObjectBuilder json = new JsonObjectBuilder();
		for (Namevalue namevalue : namevalues) {
			logger.debug("Requesting to post value: {} for input \"{}\" of node \"{}\"", 
					namevalue.getValue(), namevalue.getName(), node);
			
			json.addDouble(namevalue.getName(), namevalue.getValue());
		}
		parameters.addParameter(Const.FULLJSON, json);
		
		onPost(query, parameters, authentication);
	}

	@Override
	public void post(DataList dataList) throws EmoncmsException {
		post(dataList, authentication);
	}

	public void post(DataList dataList, Authentication authentication) throws EmoncmsException {
		logger.debug("Requesting to bulk post {} data sets", dataList.size());
		
		HttpQuery query = new HttpQuery("input", "bulk");
		HttpParameters parameters = new HttpParameters();
		
		// Posted UNIX time values need to be sent in seconds
		long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		parameters.addParameter(Const.TIME, time);
		
		dataList.sort();
		
		JsonArrayBuilder json = new JsonArrayBuilder();
		for (Data data : dataList) {
			json.addData(time, data);
		}
		parameters.addParameter(Const.DATA, json.toString());
		
		onPost(query, parameters, authentication);
	}

	@Override
	public List<Input> getInputList(String node) throws EmoncmsException {
		logger.debug("Requesting input list for node \"{}\"", node);

		HttpQuery query = new HttpQuery("input", "get_inputs");
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = onGet(query, parameters);
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

		HttpQuery query = new HttpQuery("input", "list");
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = onGet(query, parameters);
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

		HttpQuery query = new HttpQuery("input", "get_inputs");
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = onGet(query, parameters);
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
		
		HttpQuery query = new HttpQuery("input", "list");
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = onGet(query, parameters);
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

		HttpQuery query = new HttpQuery("feed", "list.json");
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = onGet(query, parameters);
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
		
		HttpQuery query = new HttpQuery("feed", "aget.json");
		query.addParameter(Const.ID, id);
		
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = onGet(query, parameters);
		try {
			JsonFeed jsonFeed = response.getFeed();
			
			ProcessList processList = new ProcessList(jsonFeed.getProcessList());
			Timevalue timevalue = null;
			if (jsonFeed.getTime() != null && jsonFeed.getValue() != null) {
				timevalue = new Timevalue(jsonFeed.getTime(), jsonFeed.getValue());
			}
			HttpFeed feed = new HttpFeed(this, jsonFeed.getId(), 
					jsonFeed.getName(), jsonFeed.getTag(), jsonFeed.isPublic(), jsonFeed.getSize(),
					Datatype.getEnum(jsonFeed.getDatatype()), Engine.getEnum(jsonFeed.getEngine()),
					processList, timevalue);
			
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
		
		HttpQuery query = new HttpQuery("feed", "fetch.json");
		query.addParameter(Const.IDS, idsBuilder.toString());
		
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = onGet(query, parameters);
		try {
			return response.getValues(feedList);
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public int newFeed(String name, String tag, Datatype type, Engine engine, Options options) throws EmoncmsException {
		logger.debug("Requesting to add feed \"{}\"", name);

		HttpQuery query = new HttpQuery("feed", "create.json");
		query.addParameter(Const.NAME, name);
		query.addParameter(Const.TAG, tag);
		query.addParameter(Const.DATATYPE, type.getValue());
		query.addParameter(Const.ENGINE, engine.getValue());
		if (options != null && !options.isEmpty()) {
			JsonObjectBuilder json = new JsonObjectBuilder();
			json.addOptions(options);
			query.addParameter(Const.OPTIONS, json);
		}
		
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = onGet(query, parameters);
		try {
			return Integer.valueOf(response.getString(Const.FEEDID));
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public HttpResponse onPost(HttpQuery path, HttpParameters parameters) throws EmoncmsException {
		return sendRequest(HttpMethod.POST, path, parameters);
	}

	@Override
	public HttpResponse onGet(HttpQuery query, HttpParameters parameters) throws EmoncmsException {
		return sendRequest(HttpMethod.GET, query, parameters);
	}

	@Override
	public HttpResponse onGet(HttpQuery query, HttpParameters parameters, Authentication authentication) throws EmoncmsException {
		return sendRequest(HttpMethod.GET, query, parameters, authentication);
	}

	@Override
	public HttpResponse onPost(HttpQuery query, HttpParameters parameters, Authentication authentication) throws EmoncmsException {
		return sendRequest(HttpMethod.POST, query, parameters, authentication);
	}

	private HttpResponse sendRequest(HttpMethod method, 
			HttpQuery query, HttpParameters parameters) throws EmoncmsException {
		return sendRequest(method, query, parameters, authentication);
	}

	private HttpResponse sendRequest(HttpMethod method, 
			HttpQuery query, HttpParameters parameters, 
			Authentication authentication) throws EmoncmsException {
		
		HttpRequest request = new HttpRequest(method, domain, query, parameters, authentication);
		HttpResponse response = submitRequest(request);
		if (response != null) {
			return response;
		}
		throw new EmoncmsException("Emoncms request failed");
	}

	private synchronized HttpResponse submitRequest(HttpRequest request) throws EmoncmsException {
		if (closed) {
			throw new EmoncmsException("Unable to submit request for closed connection");
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Requesting \"{}\"", request.toString());
		}
		int timeout = Integer.valueOf(TIMEOUT);
		long start = System.currentTimeMillis();
		
		final HttpCallable task = new HttpCallable(request, timeout);
		final Future<HttpResponse> submit = executor.submit(task);
		try {
			HttpResponse response = submit.get(timeout, TimeUnit.MILLISECONDS);
			if (logger.isTraceEnabled()) {
				logger.trace("Received response after {}ms: {}", System.currentTimeMillis() - start, response);
			}
			return response;
			
		} catch (JsonSyntaxException e) {
			throw new EmoncmsException("Received invalid JSON response: " + e);
			
		} catch (ExecutionException | CancellationException | InterruptedException | TimeoutException e) {
			submit.cancel(true);
			throw new EmoncmsException("Request \"" + request.toString() + "\" failed: " + e);
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
