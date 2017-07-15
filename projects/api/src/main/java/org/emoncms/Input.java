/*
 * Copyright 2016-17 ISC Konstanz
 *
 * This file is part of emonjava.
 * For more information visit https://bitbucket.org/isc-konstanz/emonjava
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
 *
 */
package org.emoncms;

import java.util.List;
import java.util.Map;

import org.emoncms.com.EmoncmsException;
import org.emoncms.data.Authentication;
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

	public void setProcessList(ProcessList processes) throws EmoncmsException {
		
		this.setProcessList(processes.toString());
		this.processList = processes;
	}

	protected abstract void setProcessList(String processList) throws EmoncmsException;

	public abstract void resetProcessList() throws EmoncmsException;

	public Timevalue getTimevalue() {

		return timevalue;
	}

	public abstract void post(Timevalue timevalue) throws EmoncmsException;

	public abstract void post(Timevalue timevalue, Authentication authentication) throws EmoncmsException;

	public abstract void post(List<Timevalue> timevalues) throws EmoncmsException;

	public abstract void post(List<Timevalue> timevalues, Authentication authentication) throws EmoncmsException;

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
