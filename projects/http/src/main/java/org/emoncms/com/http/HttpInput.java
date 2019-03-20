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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.emoncms.Emoncms;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.json.Const;
import org.emoncms.com.http.json.JsonInput;
import org.emoncms.com.http.json.ToJsonArray;
import org.emoncms.com.http.json.ToJsonObject;
import org.emoncms.com.http.request.HttpEmoncmsResponse;
import org.emoncms.com.http.request.HttpRequestCallbacks;
import org.emoncms.com.http.request.HttpRequestParameters;
import org.emoncms.com.http.request.HttpRequestURI;
import org.emoncms.data.Authentication;
import org.emoncms.data.Data;
import org.emoncms.data.DataList;
import org.emoncms.data.Field;
import org.emoncms.data.FieldList;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpInput implements Input {
	private static final Logger logger = LoggerFactory.getLogger(HttpInput.class);

	/**
	 * The Inputs' current callback object, which is notified of request events
	 */
	private final HttpRequestCallbacks callbacks;
	protected Integer id = null;
	protected String node;
	protected String name;
	protected String description = null;
	protected ProcessList processList = null;
	protected Timevalue timevalue = null;
	
	
	public HttpInput(HttpRequestCallbacks callbacks, Integer id, String node, String name, 
			String description, ProcessList processList, Timevalue timevalue) {
		this.id = id;
		this.node = node;
		this.name = name;
		this.callbacks = callbacks;
		this.description = description;
		this.processList = processList;
		this.timevalue = timevalue;
	}

	public HttpInput(HttpRequestCallbacks callbacks, Integer id, String node, String name) {
		this(callbacks, id, node, name, null, null, null);
	}

	public HttpInput(HttpRequestCallbacks callbacks, String node, String name) {
		this(callbacks, null, node, name);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getNode() {
		return node;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) throws EmoncmsException {

		this.setField(Field.DESCRIPTION, description);
		this.description = description;
	}

	@Override
	public ProcessList getProcessList() {
		return processList;
	}

	@Override
	public void setProcessList(ProcessList processes) throws EmoncmsException {
		
		this.setProcessList(processes.toString());
		this.processList = processes;
	}

	@Override
	public void post(Timevalue timevalue) throws EmoncmsException {
		post(timevalue, null);
	}

	@Override
	public void post(Timevalue timevalue, Authentication authentication) throws EmoncmsException {

		logger.debug("Requesting to post {} for input \"{}\" of node \"{}\"", timevalue, name, node);

		HttpRequestURI uri = new HttpRequestURI("post");
		HttpRequestParameters parameters = new HttpRequestParameters();
		parameters.addParameter(Const.NODE, node);
		
		if (timevalue.getTime() != null && timevalue.getTime() > 0) {
			// Posted UNIX time values need to be sent in seconds
			parameters.addParameter(Const.TIME, (int) Math.round(timevalue.getTime().doubleValue()/1000));
		}
		
		ToJsonObject json = new ToJsonObject();
		json.addDouble(name, timevalue.getValue());
		parameters.addParameter(Const.FULLJSON, json);
		
		if (authentication != null) {
			callbacks.onPost("input", uri, parameters, authentication);
		}
		else {
			callbacks.onPost("input", uri, parameters);
		}
	}

	@Override
	public void post(List<Timevalue> timevalues) throws EmoncmsException {
		post(timevalues, null);
	}

	@Override
	public void post(List<Timevalue> timevalues, Authentication authentication) throws EmoncmsException {
		
		logger.debug("Requesting to bulk post {} data sets for input \"{}\" of node \"{}\"", timevalues.size(), name, node);
		
		HttpRequestURI uri = new HttpRequestURI("bulk");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		DataList dataList = new DataList();
		for (Timevalue timevalue : timevalues) {
			dataList.add(node, name, timevalue);
		}
		dataList.sort();
		
		// Posted UNIX time values need to be sent in seconds
		long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		parameters.addParameter(Const.TIME, time);
		
		ToJsonArray json = new ToJsonArray();
		for (Data data : dataList) {
			json.addData(time, data);
		}
		parameters.addParameter(Const.DATA, json.toString());
		
		if (authentication != null) {
			callbacks.onPost("input", uri, parameters, authentication);
		}
		else {
			callbacks.onPost("input", uri, parameters);
		}
	}

	@Override
	public void setFields(Map<String, String> fields) throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to set {} fields for input \"{}\" of node \"{}\"", fields.size(), name, node);

			HttpRequestURI uri = new HttpRequestURI("set");
			uri.addParameter(Const.INPUTID, id);
			
			ToJsonObject json = new ToJsonObject();
			for (Map.Entry<String, String> field : fields.entrySet()) {
				json.addString(field.getKey(), field.getValue());
			}
			uri.addParameter(Const.FIELDS, json);
			
			HttpRequestParameters parameters = new HttpRequestParameters();
			
			callbacks.onGet("input", uri, parameters);
		}
		else {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
	}

	@Override
	public void setProcessList(String processList) throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to set process list for input \"{}\" of node \"{}\": {}", name, node, processList);
			
			HttpRequestURI uri = new HttpRequestURI("process/set");
			uri.addParameter(Const.INPUTID, id);
			
			HttpRequestParameters parameters = new HttpRequestParameters();
			parameters.addParameter(Const.PROCESSLIST.toLowerCase(), processList);
			
			callbacks.onPost("input", uri, parameters);
		}
		else {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
	}

	@Override
	public Timevalue getTimevalue() {

		return timevalue;
	}

	@Override
	public void setField(Field field, String value) throws EmoncmsException {

		FieldList fields = new FieldList(field, value);
		this.setFields(fields);
	}

	@Override
	public void setFields(FieldList fields) throws EmoncmsException {

		this.setFields(fields.getValues());
	}

	@Override
	public void clear() {
		this.description = null;
		this.processList = null;
		this.timevalue = null;
	}

	@Override
	public void resetProcessList() throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to reset process list for input \"{}\" of node \"{}\"", name, node);
			
			HttpRequestURI uri = new HttpRequestURI("process/reset");
			uri.addParameter(Const.INPUTID, id);
			
			HttpRequestParameters parameters = new HttpRequestParameters();
			
			callbacks.onGet("input", uri, parameters);
		}
		else {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
	}

	@Override
	public void delete() throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to delete input \"{}\" of node \"{}\"", name, node);
			
			HttpRequestURI uri = new HttpRequestURI("delete");
			uri.addParameter(Const.INPUTID, id);
			
			HttpRequestParameters parameters = new HttpRequestParameters();
			
			callbacks.onGet("input", uri, parameters);
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

		HttpRequestURI uri = new HttpRequestURI("list");
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onGet("input", uri, parameters);
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
