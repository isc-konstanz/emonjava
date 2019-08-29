/* 
 * Copyright 2016-19 ISC Konstanz
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.emoncms.Emoncms;
import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.Input;
import org.emoncms.data.Authentication;
import org.emoncms.data.Data;
import org.emoncms.data.DataList;
import org.emoncms.data.Field;
import org.emoncms.data.FieldList;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.emoncms.http.json.Const;
import org.emoncms.http.json.JsonInput;
import org.emoncms.http.json.JsonArrayBuilder;
import org.emoncms.http.json.JsonObjectBuilder;
import org.emoncms.http.request.HttpCallbacks;
import org.emoncms.http.request.HttpParameters;
import org.emoncms.http.request.HttpQuery;
import org.emoncms.http.request.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>HttpInput</code> implementation provides the {@link Emoncms} interface for HTTP input requests.
 * 
 * Inputs hold all their configuration fields in memory, but may be cleared to only hold their ID, node and name 
 * by calling {@link HttpInput#clear()}. Input methods only need those variables to maintain their functionality 
 * and other resources may be released to save memory.
 * 
 * Current values for all fields can be retrieved by calling {@link HttpInput#load()}.
 */
public class HttpInput implements Input {
	private static final Logger logger = LoggerFactory.getLogger(HttpInput.class);

	/**
	 * The Inputs' current callback object, which is notified of request events
	 */
	private final HttpCallbacks callbacks;

	protected Integer id = null;
	protected String node;
	protected String name;
	protected String description = null;
	protected ProcessList processList = null;

	protected Timevalue timevalue = null;

	public static HttpInput connect(HttpCallbacks callbacks, String name, String node) throws EmoncmsUnavailableException {
		if (callbacks == null) {
			throw new EmoncmsUnavailableException("HTTP connection to emoncms webserver invalid");
		}
		return new HttpInput(callbacks, name, node);
	}

	protected HttpInput(HttpCallbacks callbacks, Integer id, String node, String name, 
			String description, ProcessList processList, Timevalue timevalue) {
		this.callbacks = callbacks;
		
		this.id = id;
		this.node = node;
		this.name = name;
		this.description = description;
		this.processList = processList;
		this.timevalue = timevalue;
	}

	protected HttpInput(HttpCallbacks callbacks, String node, String name) {
		this.callbacks = callbacks;
		
		this.node = node;
		this.name = name;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.HTTP;
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
	public void setFields(Map<String, String> fields) throws EmoncmsException {
		logger.debug("Requesting to set {} fields for input \"{}\" of node \"{}\"", fields.size(), name, node);
		if (id == null) {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
		HttpQuery query = new HttpQuery("input", "set");
		query.addParameter(Const.INPUTID, id);
		
		JsonObjectBuilder json = new JsonObjectBuilder();
		for (Map.Entry<String, String> field : fields.entrySet()) {
			json.addString(field.getKey(), field.getValue());
		}
		query.addParameter(Const.FIELDS, json);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	@Override
	public void setProcessList(String processList) throws EmoncmsException {
		logger.debug("Requesting to set process list for input \"{}\" of node \"{}\": {}", name, node, processList);
		if (id == null) {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
		
		HttpQuery query = new HttpQuery("input", "process", "set");
		query.addParameter(Const.INPUTID, id);
		
		HttpParameters parameters = new HttpParameters();
		parameters.addParameter(Const.PROCESSLIST.toLowerCase(), processList);
		
		callbacks.onPost(query, parameters);
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
	public void resetProcessList() throws EmoncmsException {
		logger.debug("Requesting to reset process list for input \"{}\" of node \"{}\"", name, node);
		if (id == null) {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
		
		HttpQuery query = new HttpQuery("input", "process", "reset");
		query.addParameter(Const.INPUTID, id);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	@Override
	public Timevalue getTimevalue() {
		return timevalue;
	}

	@Override
	public void post(Timevalue timevalue) throws EmoncmsException {
		post(timevalue, null);
	}

	public void post(Timevalue timevalue, Authentication authentication) throws EmoncmsException {
		logger.debug("Requesting to post {} for input \"{}\" of node \"{}\"", timevalue, name, node);

		HttpQuery query = new HttpQuery("input", "post");
		HttpParameters parameters = new HttpParameters();
		parameters.addParameter(Const.NODE, node);
		
		if (timevalue.getTime() != null && timevalue.getTime() > 0) {
			// Posted UNIX time values need to be sent in seconds
			parameters.addParameter(Const.TIME, (int) Math.round(timevalue.getTime().doubleValue()/1000));
		}
		
		JsonObjectBuilder json = new JsonObjectBuilder();
		json.addDouble(name, timevalue.getValue());
		parameters.addParameter(Const.FULLJSON, json);
		
		if (authentication != null) {
			callbacks.onPost(query, parameters, authentication);
		}
		else {
			callbacks.onPost(query, parameters);
		}
		this.timevalue = timevalue;
	}

	@Override
	public void post(List<Timevalue> timevalues) throws EmoncmsException {
		post(timevalues, null);
	}

	public void post(List<Timevalue> timevalues, Authentication authentication) throws EmoncmsException {
		logger.debug("Requesting to bulk post {} data sets for input \"{}\" of node \"{}\"", timevalues.size(), name, node);
		
		HttpQuery query = new HttpQuery("input", "bulk");
		HttpParameters parameters = new HttpParameters();
		
		DataList dataList = new DataList();
		for (Timevalue timevalue : timevalues) {
			dataList.add(node, name, timevalue);
		}
		dataList.sort();
		
		// Posted UNIX time values need to be sent in seconds
		long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		parameters.addParameter(Const.TIME, time);
		
		JsonArrayBuilder json = new JsonArrayBuilder();
		for (Data data : dataList) {
			json.addData(time, data);
		}
		parameters.addParameter(Const.DATA, json.toString());
		
		if (authentication != null) {
			callbacks.onPost(query, parameters, authentication);
		}
		else {
			callbacks.onPost(query, parameters);
		}
		this.timevalue = timevalues.get(timevalues.size()-1);
	}

	@Override
	public void delete() throws EmoncmsException {
		logger.debug("Requesting to delete input \"{}\" of node \"{}\"", name, node);
		if (id == null) {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
		
		HttpQuery query = new HttpQuery("input", "delete");
		query.addParameter(Const.INPUTID, id);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	public void load() throws EmoncmsException {
		if (id != null) {
			logger.debug("Requesting input with id: {}", id);
		}
		else {
			logger.debug("Requesting input \"{}\" of node \"{}\"", name, node);
		}
		HttpQuery query = new HttpQuery("input", "list");
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = callbacks.onGet(query, parameters);
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

	public void clear() {
		this.description = null;
		this.processList = null;
		this.timevalue = null;
	}

}
