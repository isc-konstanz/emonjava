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

import java.util.Map;

import org.emoncms.com.EmoncmsException;
import org.emoncms.data.Field;
import org.emoncms.data.FieldList;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;


/**
 * The <code>Input</code> class is used to communicate with an emoncms webserver and handle input specific actions.
 * An input instance can be used to
 * <ul>
 * <li>Post data to the instanced input.</li>
 * <li>Configure logging or other data processing.</li>
 * <li>Get configuration information.</li>
 * </ul>
 * 
 * <p>
 * Inputs hold all their configuration fields in memory, but may be cleared to only hold their ID, node and name 
 * by calling {@link Input#clear()}. Input methods only need those variables to maintain their functionality 
 * and other resources may be released to save memory. 
 * Current values for all fields can be retrieved by calling {@link Input#load()}.
 */
public abstract class Input {

	protected final int id;
	protected final String node;
	protected String name;
	protected String description = null;
	protected ProcessList processList = null;
	protected Timevalue timevalue = null;

	protected Input(int id, String node, String name, 
			String description, ProcessList processList, Timevalue timevalue) {
		this.id = id;
		this.node = node;
		this.name = name;
		this.description = description;
		this.processList = processList;
		this.timevalue = timevalue;
	}

	protected Input(int id, String node, String name) {
		this.id = id;
		this.node = node;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getNode() {
		return node;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) throws EmoncmsException {

		this.setField(Field.NAME, name);
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) throws EmoncmsException {

		this.setField(Field.DESCRIPTION, description);
		this.description = description;
	}

	public ProcessList getProcessList() {
		return processList;
	}

	protected void setProcessList(ProcessList processes) throws EmoncmsException {
		
		this.setProcessList(processes.toString());
		this.processList = processes;
	}

	protected abstract void setProcessList(String processList) throws EmoncmsException;

	public abstract void resetProcessList() throws EmoncmsException;

	public Timevalue getTimevalue() {

		return timevalue;
	}

	public abstract void post(Timevalue timevalue) throws EmoncmsException;

	public abstract void post(String devicekey, Timevalue timevalue) throws EmoncmsException;

	public void setField(Field field, String value) throws EmoncmsException {

		FieldList fields = new FieldList(field, value);
		this.setFields(fields);
	}

	public void setFields(FieldList fields) throws EmoncmsException {

		this.setFields(fields.getValues());
	}

	protected abstract void setFields(Map<String, String> fields) throws EmoncmsException;

	public abstract void delete() throws EmoncmsException;

	public abstract void load() throws EmoncmsException;

	public void clear() {
		this.description = null;
		this.processList = null;
		this.timevalue = null;
	}
}
