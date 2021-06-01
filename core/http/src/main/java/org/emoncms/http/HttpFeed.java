/* 
 * Copyright 2016-21 ISC Konstanz
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

import java.util.LinkedList;
import java.util.Map;

import org.emoncms.Emoncms;
import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.Feed;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Field;
import org.emoncms.data.FieldList;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.emoncms.http.json.Const;
import org.emoncms.http.json.JsonFeed;
import org.emoncms.http.json.JsonTimevalue;
import org.emoncms.http.json.JsonObjectBuilder;
import org.emoncms.http.request.HttpCallbacks;
import org.emoncms.http.request.HttpParameters;
import org.emoncms.http.request.HttpQuery;
import org.emoncms.http.request.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>HttpFeed</code> implementation provides the {@link Emoncms} interface for HTTP feed requests.
 * 
 * Feeds hold all their configuration fields in memory, but may be cleared to only hold their ID  
 * by calling {@link HttpFeed#clear()}. Feed methods only need the ID to maintain their functionality 
 * and other resources may be released to save memory.
 * 
 * Current values for all fields can be retrieved by calling {@link HttpFeed#load()}.
 */
public class HttpFeed implements Feed {
	private static final Logger logger = LoggerFactory.getLogger(HttpFeed.class);

	/**
	 * The Feeds' current callback object, which is notified of request events
	 */
	private final HttpCallbacks callbacks;

	protected final int id;

	protected String name = null;
	protected String tag = null;
	protected Boolean visible = null;
	protected Integer size = null;
	protected Datatype datatype = null;
	protected Engine engine = null;
	protected ProcessList processList = null;
	protected Timevalue timevalue = null;

	public static HttpFeed connect(HttpCallbacks callbacks, int id) throws EmoncmsUnavailableException {
		if (callbacks == null) {
			throw new EmoncmsUnavailableException("HTTP connection to emoncms webserver invalid");
		}
		return new HttpFeed(callbacks, id);
	}

	protected HttpFeed(HttpCallbacks callbacks, int id, String name, String tag, 
			boolean visible, Integer size, Datatype datatype, Engine engine, 
			ProcessList processList, Timevalue value) {
		
		this.callbacks = callbacks;
		
		this.id = id;
		this.name = name;
		this.tag = tag;
		this.visible = visible;
		this.size = size;
		this.datatype = datatype;
		this.engine = engine;
		this.processList = processList;
		this.timevalue = value;
	}

	protected HttpFeed(HttpCallbacks callbacks, int id) {
		this.callbacks = callbacks;
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.HTTP;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) throws EmoncmsException {
		this.setField(Field.NAME, name);
		this.name = name;
	}

	@Override
	public String getTag() {
		return tag;
	}

	@Override
	public void setTag(String tag) throws EmoncmsException {
		this.setField(Field.TAG, tag);
		this.tag = tag;
	}

	@Override
	public boolean isPublic() {
		return visible;
	}

	@Override
	public void setPublic(boolean visible) throws EmoncmsException {
		this.setField(Field.PUBLIC, String.valueOf(visible));
		this.visible = visible;
	}

	@Override
	public Integer getSize() {
		return size;
	}

	@Override
	public Datatype getDatatype() {
		return datatype;
	}

	@Override
	public Engine getEngine() {
		return engine;
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

	protected void setProcessList(String processList) throws EmoncmsException {
		logger.debug("Requesting to set process list for feed with id: {}", id, processList);
		
		HttpQuery query = new HttpQuery("feed", "process", "set.json");
		query.addParameter(Const.ID, id);
		
		HttpParameters parameters = new HttpParameters();
		parameters.addParameter(Const.PROCESSLIST.toLowerCase(), processList);
		
		callbacks.onPost(query, parameters);
	}

	@Override
	public void resetProcessList() throws EmoncmsException {
		logger.debug("Requesting to reset process list for feed with id: {}", id);
		
		HttpQuery query = new HttpQuery("feed", "process", "reset.json");
		query.addParameter(Const.ID, id);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	@Override
	public String getField(Field field) throws EmoncmsException {
		return this.getField(field.getValue());
	}

	protected String getField(String field) throws EmoncmsException {
		logger.debug("Requesting to get field \"{}\" for feed with id: {}", field, id);

		HttpQuery query = new HttpQuery("feed", "get.json");
		query.addParameter(Const.ID, id);
		query.addParameter(Const.FIELD, field);
		
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = callbacks.onGet(query, parameters);
		return response.toString().replaceAll("\"", "");
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

	protected void setFields(Map<String, String> fields) throws EmoncmsException {
		logger.debug("Requesting to set {} fields for feed with id: {}", fields.size(), id);

		HttpQuery query = new HttpQuery("feed", "set.json");
		query.addParameter(Const.ID, id);
		
		JsonObjectBuilder json = new JsonObjectBuilder();
		for (Map.Entry<String, String> field : fields.entrySet()) {
			json.addString(field.getKey(), field.getValue());
		}
		query.addParameter(Const.FIELDS, json);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	public Double getValue() {
		if (timevalue != null) {
			return timevalue.getValue();
		}
		return null;
	}

	@Override
	public Double getLatestValue() throws EmoncmsException {
		logger.debug("Requesting to get latest value for feed with id: {}", id);

		HttpQuery query = new HttpQuery("feed", "value.json");
		query.addParameter(Const.ID, id);
		
		HttpParameters parameters = new HttpParameters();
		HttpResponse response = callbacks.onGet(query, parameters);
		return Double.valueOf(response.toString().replaceAll("\"", ""));
	}

	public Timevalue getTimevalue() {
		return timevalue;
	}

	@Override
	public Timevalue getLatestTimevalue() throws EmoncmsException {
		
		logger.debug("Requesting to get latest timevalue for feed with id: {}", id);

		HttpQuery query = new HttpQuery("feed", "timevalue.json");
		query.addParameter(Const.ID, id);
		
		HttpParameters parameters = new HttpParameters();
		HttpResponse response = callbacks.onGet(query, parameters);
		try {
			JsonTimevalue jsonTimevalue = response.getTimevalue();
			Timevalue timevalue = new Timevalue(jsonTimevalue.getTime(), jsonTimevalue.getValue());
			
			return timevalue;
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
		logger.debug("Requesting to fetch data from {} to {} for feed with id: {}", start, end, id);

		HttpQuery query = new HttpQuery("feed", "data.json");
		query.addParameter(Const.ID, id);
		query.addParameter(Const.START, start);
		query.addParameter(Const.END, end);
		query.addParameter(Const.INTERVAL, interval);
		
		HttpParameters parameters = new HttpParameters();
		HttpResponse response = callbacks.onGet(query, parameters);
		try {
			return response.getTimevalues();
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public void insertData(Timevalue timevalue) throws EmoncmsException {
		this.insertData(timevalue.getTime(), timevalue.getValue());
	}

	protected void insertData(long time, double value) throws EmoncmsException {
		logger.debug("Requesting to insert value: {}, time: {} for feed with id: {}", value, time, id);

		HttpQuery query = new HttpQuery("feed", "insert.json");
		query.addParameter(Const.ID, id);
		query.addParameter(Const.TIME, time);
		query.addParameter(Const.VALUE, value);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	@Override
	public void updateData(Timevalue timevalue) throws EmoncmsException {
		this.updateData(timevalue.getTime(), timevalue.getValue());
	}

	protected void updateData(long time, double value) throws EmoncmsException {
		logger.debug("Requesting to update value: {} at time: {} for feed with id: {}", value, time, id);

		HttpQuery query = new HttpQuery("feed", "update.json");
		query.addParameter(Const.ID, id);
		query.addParameter(Const.TIME, time);
		query.addParameter(Const.VALUE, value);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	@Override
	public void deleteData(long time) throws EmoncmsException {
		logger.debug("Requesting to delete value at time: {} for feed with id: {}", time, id);

		HttpQuery query = new HttpQuery("feed", "deletedatapoint.json");
		query.addParameter(Const.ID, id);
		query.addParameter(Const.TIME, time);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	@Override
	public void deleteDataRange(long start, long end) throws EmoncmsException {
		logger.debug("Requesting to delete values from {} to {} for feed with id: {}", start, end, id);

		HttpQuery query = new HttpQuery("feed", "deletedatarange.json");
		query.addParameter(Const.ID, id);
		query.addParameter(Const.START, start);
		query.addParameter(Const.END, end);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	@Override
	public void delete() throws EmoncmsException {
		logger.debug("Requesting to delete feed with id: {}", id);
		
		HttpQuery query = new HttpQuery("feed", "delete.json");
		query.addParameter(Const.ID, id);
		
		HttpParameters parameters = new HttpParameters();
		
		callbacks.onGet(query, parameters);
	}

	public void load() throws EmoncmsException {

		logger.debug("Requesting feed with id: {}", id);

		HttpQuery query = new HttpQuery("feed", "aget.json");
		query.addParameter(Const.ID, id);
		
		HttpParameters parameters = new HttpParameters();
		
		HttpResponse response = callbacks.onGet(query, parameters);
		try {
			JsonFeed jsonFeed = response.getFeed();
			
			this.name = jsonFeed.getName();
			this.tag = jsonFeed.getTag();
			this.visible = jsonFeed.isPublic();
			this.size = jsonFeed.getSize();
			this.datatype =  Datatype.getEnum(jsonFeed.getDatatype());
			this.engine = Engine.getEnum(jsonFeed.getEngine());
			if (jsonFeed.getTime() != null && jsonFeed.getValue() != null) {
				this.timevalue = new Timevalue(jsonFeed.getTime(), jsonFeed.getValue());
			}
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	public void clear() {
		this.name = null;
		this.tag = null;
		this.visible = null;
		this.size = null;
		this.datatype = null;
		this.engine = null;
		this.processList = null;
		this.timevalue = null;
	}
}
