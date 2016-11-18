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

import java.util.List;
import java.util.Map;

import org.emoncms.Emoncms;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.json.Const;
import org.emoncms.com.http.json.JsonInput;
import org.emoncms.com.http.json.ToJson;
import org.emoncms.com.http.request.HttpEmoncmsResponse;
import org.emoncms.com.http.request.HttpRequestAction;
import org.emoncms.com.http.request.HttpRequestAuthentication;
import org.emoncms.com.http.request.HttpRequestCallbacks;
import org.emoncms.com.http.request.HttpRequestMethod;
import org.emoncms.com.http.request.HttpRequestParameters;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpInput extends Input {
	private static final Logger logger = LoggerFactory.getLogger(HttpInput.class);

	/**
	 * The Inputs' current callback object, which is notified of request events
	 */
	private final HttpRequestCallbacks callbacks;
	
	
	public HttpInput(HttpRequestCallbacks callbacks, int id, String node, String name, 
			String description, ProcessList processList, Timevalue timevalue) {
		super(id, node, name, description, processList, timevalue);
		this.callbacks = callbacks;
	}

	public HttpInput(HttpRequestCallbacks callbacks, int id, String node, String name) {
		super(id, node, name);
		this.callbacks = callbacks;
	}

	@Override
	public void post(Timevalue timevalue) throws EmoncmsException {
		requestPost(null, timevalue);
	}

	@Override
	public void post(String devicekey, Timevalue timevalue) throws EmoncmsException {
		HttpRequestAuthentication authentication = new HttpRequestAuthentication(Const.DEVICE_KEY, devicekey);
		requestPost(authentication, timevalue);
	}
	
	private void requestPost(HttpRequestAuthentication authentication, Timevalue timevalue) throws EmoncmsException {

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
		
		if (authentication != null) {
			callbacks.onRequest("input", authentication, action, parameters, HttpRequestMethod.POST);
		}
		else {
			callbacks.onRequest("input", action, parameters, HttpRequestMethod.POST);
		}
	}

	@Override
	protected void setFields(Map<String, String> fields) throws EmoncmsException {

		logger.debug("Requesting to set {} fields for input \"{}\" of node \"{}\"", fields.size(), name, node);

		HttpRequestAction action = new HttpRequestAction("set");
		action.addParameter(Const.INPUTID, id);
		ToJson json = new ToJson();
		for (Map.Entry<String, String> field : fields.entrySet()) {
			json.addString(field.getKey(), field.getValue());
		}
		action.addParameter(Const.FIELDS, json);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onRequest("input", action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public void setProcessList(String processList) throws EmoncmsException {

		logger.debug("Requesting to set process list for input \"{}\" of node \"{}\": {}", name, node, processList);
		
		HttpRequestAction action = new HttpRequestAction("process/set");
		action.addParameter(Const.INPUTID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		parameters.addParameter(Const.PROCESSLIST.toLowerCase(), processList);
		
		callbacks.onRequest("input", action, parameters, HttpRequestMethod.POST);
	}

	@Override
	public void resetProcessList() throws EmoncmsException {

		logger.debug("Requesting to reset process list for input \"{}\" of node \"{}\"", name, node);
		
		HttpRequestAction action = new HttpRequestAction("process/reset");
		action.addParameter(Const.INPUTID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onRequest("input", action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public void delete() throws EmoncmsException {

		logger.debug("Requesting to delete input \"{}\" of node \"{}\"", name, node);
		
		HttpRequestAction action = new HttpRequestAction("delete");
		action.addParameter(Const.INPUTID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onRequest("input", action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public void load() throws EmoncmsException {

		logger.debug("Requesting input with id: {}", id);

		HttpRequestAction action = new HttpRequestAction("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onRequest("input", action, parameters, HttpRequestMethod.GET);
		try {
			List<JsonInput> jsonInputList = response.getInputList();

			for (JsonInput jsonInput : jsonInputList) {
				if (jsonInput.getId() == id) {
					this.name = jsonInput.getName();
					this.description = jsonInput.getDescription();
					this.processList = new ProcessList(jsonInput.getProcessList());
					this.timevalue = new Timevalue(jsonInput.getTime(), jsonInput.getValue());
					
					break;
				}
			}
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}
	
	public static Input connect(Emoncms connection, int id, String name, String node) throws EmoncmsUnavailableException {
		if (connection != null && connection instanceof HttpRequestCallbacks) {
			return new HttpInput((HttpRequestCallbacks) connection, id, name, node);
		}
		else throw new EmoncmsUnavailableException("HTTP connection to emoncms webserver invalid");
	}
}
