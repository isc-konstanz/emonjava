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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emonjava.Input;
import de.isc.emonjava.com.EmoncmsException;
import de.isc.emonjava.com.http.json.Const;
import de.isc.emonjava.com.http.json.ToJson;
import de.isc.emonjava.data.ProcessList;
import de.isc.emonjava.data.Timevalue;


public class HttpInput extends Input {
	private static final Logger logger = LoggerFactory.getLogger(HttpInput.class);

	/**
	 * The Inputs' current callback object, which is notified of request events
	 */
	private final HttpInputCallbacks callbacks;
	
	/**
	 * Interface used by {@link HttpInput} to notify the {@link HttpEmoncms} 
	 * implementation about request events
	 */
	public static interface HttpInputCallbacks {
		
		HttpEmoncmsResponse onInputRequest(HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method)
			throws EmoncmsException;
	}
	
	
	public HttpInput(HttpInputCallbacks callbacks, int id, String node, String name) {
		super(id, node, name);
		this.callbacks = callbacks;
	}

	@Override
	public void post(Timevalue timevalue) throws EmoncmsException {

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
		
		callbacks.onInputRequest(action, parameters, HttpRequestMethod.POST);
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
		
		callbacks.onInputRequest(action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public ProcessList getProcessList() throws EmoncmsException {

		logger.debug("Requesting process list for input \"{}\" of node \"{}\"", name, node);
		
		HttpRequestAction action = new HttpRequestAction("process/get");
		action.addParameter(Const.INPUTID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onInputRequest(action, parameters, HttpRequestMethod.GET);
		return new ProcessList(response.getResponse().replace("\"", ""));
	}

	@Override
	public void setProcessList(ProcessList processList) throws EmoncmsException {

		logger.debug("Requesting to set process list for input \"{}\" of node \"{}\": {}", name, node, processList);
		
		HttpRequestAction action = new HttpRequestAction("process/set");
		action.addParameter(Const.INPUTID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		parameters.addParameter(Const.PROCESSLIST.toLowerCase(), processList.toString());
		
		callbacks.onInputRequest(action, parameters, HttpRequestMethod.POST);
	}

	@Override
	public void resetProcessList() throws EmoncmsException {

		logger.debug("Requesting to reset process list for input \"{}\" of node \"{}\"", name, node);
		
		HttpRequestAction action = new HttpRequestAction("process/reset");
		action.addParameter(Const.INPUTID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onInputRequest(action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public void delete() throws EmoncmsException {

		logger.debug("Requesting to delete input \"{}\" of node \"{}\"", name, node);
		
		HttpRequestAction action = new HttpRequestAction("delete");
		action.addParameter(Const.INPUTID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onInputRequest(action, parameters, HttpRequestMethod.GET);
	}
}
