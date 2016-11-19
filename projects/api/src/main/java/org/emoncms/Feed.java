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
package org.emoncms;

import java.util.LinkedList;
import java.util.Map;

import org.emoncms.com.EmoncmsException;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Field;
import org.emoncms.data.FieldList;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;


/**
 * The <code>Feed</code> class is used to communicate with an emoncms webserver and handle feed specific actions.
 * An feed instance can be used to
 * <ul>
 * <li>Retrieve the latest and historical data.</li>
 * <li>Get configuration information.</li>
 * </ul>
 * 
 * <p>
 * Feeds hold all their configuration fields in memory, but may be cleared to only hold their ID  
 * by calling {@link Feed#clear()}. Feed methods only need the ID to maintain their functionality 
 * and other resources may be released to save memory. 
 * Current values for all fields can be retrieved by calling {@link Feed#load()}.
 */
public abstract class Feed {

	protected final int id;
	protected String name = null;
	protected String tag = null;
	protected Boolean visible = null;
	protected Integer size = null;
	protected Datatype datatype = null;
	protected Engine engine = null;
	protected ProcessList processList = null;
	protected Timevalue timevalue = null;

	public Feed(int id, String name, String tag, 
			boolean visible, int size, Datatype datatype, Engine engine, ProcessList processList, Timevalue value) {
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
	
	public Feed(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() throws EmoncmsException {
		return name;
	}

	public void setName(String name) throws EmoncmsException {

		this.setField(Field.NAME, name);
		this.name = name;
	}

	public String getTag() throws EmoncmsException {
		return tag;
	}

	public void setTag(String tag) throws EmoncmsException {

		this.setField(Field.TAG, tag);
		this.tag = tag;
	}

	public boolean isPublic() throws EmoncmsException {
		return visible;
	}

	public void setPublic(boolean visible) throws EmoncmsException {

		this.setField(Field.PUBLIC, String.valueOf(visible));
		this.visible = visible;
	}

	public int getSize() throws EmoncmsException {
		return size;
	}

	public Datatype getDatatype() throws EmoncmsException {
		return datatype;
	}

	public Engine getEngine() throws EmoncmsException {
		return engine;
	}

	public ProcessList getProcessList() {
		return processList;
	}

	public void setProcessList(ProcessList processes) throws EmoncmsException {
		
		this.setProcessList(processes.toString());
		this.processList = processes;
	}

	protected abstract void setProcessList(String processList) throws EmoncmsException;

	public abstract void resetProcessList() throws EmoncmsException;

	public String getField(Field field) throws EmoncmsException {

		return this.getField(field.getValue());
	}

	protected abstract String getField(String field) throws EmoncmsException;

	public void setField(Field field, String value) throws EmoncmsException {

		FieldList fields = new FieldList(field, value);
		this.setFields(fields);
	}

	public void setFields(FieldList fields) throws EmoncmsException {

		this.setFields(fields.getValues());
	}

	protected abstract void setFields(Map<String, String> fields) throws EmoncmsException;

	public Double getValue() throws EmoncmsException {
		if (timevalue != null) {
			return timevalue.getValue();
		}
		return null;
	}

	public abstract Double getLatestValue() throws EmoncmsException;

	public Timevalue getTimevalue() throws EmoncmsException {
		return timevalue;
	}

	public abstract Timevalue getLatestTimevalue() throws EmoncmsException;

	public abstract LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException;

	public void insertData(Timevalue timevalue) throws EmoncmsException {
		this.insertData(timevalue.getTime(), timevalue.getValue());
	}

	protected abstract void insertData(long time, double value) throws EmoncmsException;

	public void updateData(Timevalue timevalue) throws EmoncmsException {
		this.updateData(timevalue.getTime(), timevalue.getValue());
	}

	protected abstract void updateData(long time, double value) throws EmoncmsException;

	public abstract void deleteData(long time) throws EmoncmsException;

	public abstract void deleteDataRange(long start, long end) throws EmoncmsException;

	public abstract void delete() throws EmoncmsException;

	public abstract void load() throws EmoncmsException;

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