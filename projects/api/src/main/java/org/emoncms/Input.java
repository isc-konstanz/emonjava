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


public abstract class Input {
	
	protected final int id;
	protected final String node;
	protected String name;
	
	public Input(int id, String node, String name) {
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
	
	public void setDescription(String description) throws EmoncmsException {
		this.setField(Field.DESCRIPTION, description);
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

	public abstract ProcessList getProcessList() throws EmoncmsException;

	public abstract void setProcessList(ProcessList processes) throws EmoncmsException;

	public abstract void resetProcessList() throws EmoncmsException;

	public abstract void delete() throws EmoncmsException;

	public abstract Input clear();
}