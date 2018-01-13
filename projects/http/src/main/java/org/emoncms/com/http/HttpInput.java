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
package org.emoncms.com.http;

import java.util.List;
import java.util.Map;

import org.emoncms.Emoncms;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.json.Const;
import org.emoncms.com.http.json.JsonInput;
import org.emoncms.com.http.json.ToJsonArray;
import org.emoncms.com.http.json.ToJsonObject;
import org.emoncms.com.http.request.HttpEmoncmsResponse;
import org.emoncms.com.http.request.HttpRequestAction;
import org.emoncms.com.http.request.HttpRequestCallbacks;
import org.emoncms.com.http.request.HttpRequestMethod;
import org.emoncms.com.http.request.HttpRequestParameters;
import org.emoncms.data.Data;
import org.emoncms.data.DataList;
import org.emoncms.data.Authentication;
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
	
	
	public HttpInput(HttpRequestCallbacks callbacks, Integer id, String node, String name, 
			String description, ProcessList processList, Timevalue timevalue) {
		super(id, node, name, description, processList, timevalue);
		this.callbacks = callbacks;
	}

	public HttpInput(HttpRequestCallbacks callbacks, Integer id, String node, String name) {
		super(id, node, name);
		this.callbacks = callbacks;
	}

	public HttpInput(HttpRequestCallbacks callbacks, String node, String name) {
		this(callbacks, null, node, name);
	}

	@Override
	public void post(Timevalue timevalue) throws EmoncmsException {
		post(timevalue, null);
	}

	@Override
	public void post(Timevalue timevalue, Authentication authentication) throws EmoncmsException {

		logger.debug("Requesting to post {} for input \"{}\" of node \"{}\"", timevalue, name, node);

		HttpRequestAction action = new HttpRequestAction("post/"+node);
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		if (timevalue.getTime() != null && timevalue.getTime() > 0) {
			// Posted UNIX time values need to be sent in seconds
			parameters.addParameter(Const.TIME, (int) Math.round(timevalue.getTime().doubleValue()/1000));
		}
		
		ToJsonObject json = new ToJsonObject();
		json.addDouble(name, timevalue.getValue());
		parameters.addParameter(Const.FULLJSON, json);
		
		if (authentication != null) {
			callbacks.onRequest("input", authentication, action, parameters, HttpRequestMethod.POST);
		}
		else {
			callbacks.onRequest("input", action, parameters, HttpRequestMethod.POST);
		}
	}

	@Override
	public void post(List<Timevalue> timevalues) throws EmoncmsException {
		post(timevalues, null);
	}

	@Override
	public void post(List<Timevalue> timevalues, Authentication authentication) throws EmoncmsException {
		
		logger.debug("Requesting to bulk post {} data sets for input \"{}\" of node \"{}\"", timevalues.size(), name, node);
		
		HttpRequestAction action = new HttpRequestAction("bulk");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		DataList dataList = new DataList();
		for (Timevalue timevalue : timevalues) {
			dataList.add(node, name, timevalue);
		}
		Long time = dataList.getTime();
		if (time != null && time > 0) {
			// Posted UNIX time values need to be sent in seconds
			parameters.addParameter(Const.TIME, (int) Math.round(time.doubleValue()/1000));
		}
		else {
			time = System.currentTimeMillis();
		}
		dataList.sort();
		
		ToJsonArray json = new ToJsonArray();
		for (Data data : dataList) {
			json.addData(time, data);
		}
		parameters.addParameter(Const.DATA, json.toString());
		
		if (authentication != null) {
			callbacks.onRequest("input", authentication, action, parameters, HttpRequestMethod.POST);
		}
		else {
			callbacks.onRequest("input", action, parameters, HttpRequestMethod.POST);
		}
	}

	@Override
	protected void setFields(Map<String, String> fields) throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to set {} fields for input \"{}\" of node \"{}\"", fields.size(), name, node);

			HttpRequestAction action = new HttpRequestAction("set");
			action.addParameter(Const.INPUTID, id);
			ToJsonObject json = new ToJsonObject();
			for (Map.Entry<String, String> field : fields.entrySet()) {
				json.addString(field.getKey(), field.getValue());
			}
			action.addParameter(Const.FIELDS, json);
			
			HttpRequestParameters parameters = new HttpRequestParameters();
			
			callbacks.onRequest("input", action, parameters, HttpRequestMethod.GET);
		}
		else {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
	}

	@Override
	public void setProcessList(String processList) throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to set process list for input \"{}\" of node \"{}\": {}", name, node, processList);
			
			HttpRequestAction action = new HttpRequestAction("process/set");
			action.addParameter(Const.INPUTID, id);
			
			HttpRequestParameters parameters = new HttpRequestParameters();
			parameters.addParameter(Const.PROCESSLIST.toLowerCase(), processList);
			
			callbacks.onRequest("input", action, parameters, HttpRequestMethod.POST);
		}
		else {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
	}

	@Override
	public void resetProcessList() throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to reset process list for input \"{}\" of node \"{}\"", name, node);
			
			HttpRequestAction action = new HttpRequestAction("process/reset");
			action.addParameter(Const.INPUTID, id);
			
			HttpRequestParameters parameters = new HttpRequestParameters();
			
			callbacks.onRequest("input", action, parameters, HttpRequestMethod.GET);
		}
		else {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
	}

	@Override
	public void delete() throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to delete input \"{}\" of node \"{}\"", name, node);
			
			HttpRequestAction action = new HttpRequestAction("delete");
			action.addParameter(Const.INPUTID, id);
			
			HttpRequestParameters parameters = new HttpRequestParameters();
			
			callbacks.onRequest("input", action, parameters, HttpRequestMethod.GET);
		}
		else {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
	}

	@Override
	public void load() throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting input with id: {}", id);
		}
		else {
			logger.debug("Requesting input \"{}\" of node \"{}\"", name, node);
		}

		HttpRequestAction action = new HttpRequestAction("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onRequest("input", action, parameters, HttpRequestMethod.GET);
		try {
			List<JsonInput> jsonInputList = response.getInputList();

			for (JsonInput jsonInput : jsonInputList) {
				if (id != null && jsonInput.getId() == id) {
					this.node = jsonInput.getNodeid();
					this.name = jsonInput.getName();
					this.description = jsonInput.getDescription();
					this.processList = new ProcessList(jsonInput.getProcessList());
					this.timevalue = new Timevalue(jsonInput.getTime(), jsonInput.getValue());
					
					break;
				}
				else if (jsonInput.getNodeid().equals(node) && jsonInput.getName().equals(name)) {
					this.id = jsonInput.getId();
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

	public static Input connect(Emoncms connection, Integer id, String name, String node) throws EmoncmsUnavailableException {
		if (connection != null && connection instanceof HttpRequestCallbacks) {
			return new HttpInput((HttpRequestCallbacks) connection, id, name, node);
		}
		else throw new EmoncmsUnavailableException("HTTP connection to emoncms webserver invalid");
	}
	
	public static Input connect(Emoncms connection, String name, String node) throws EmoncmsUnavailableException {
		return connect(connection, null, name, node);
	}
}
