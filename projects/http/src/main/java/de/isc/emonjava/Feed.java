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
package de.isc.emonjava;

import java.util.LinkedList;
import java.util.Map;

import de.isc.emonjava.com.EmoncmsException;
import de.isc.emonjava.data.Datatype;
import de.isc.emonjava.data.Engine;
import de.isc.emonjava.data.Field;
import de.isc.emonjava.data.FieldList;
import de.isc.emonjava.data.ProcessList;
import de.isc.emonjava.data.Timevalue;


public abstract class Feed {
	
	protected final int id;
	
	public Feed(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() throws EmoncmsException {
		return getField(Field.NAME);
	}
	
	public void setName(String name) throws EmoncmsException {
		this.setField(Field.NAME, name);
	}

	public String getTag() throws EmoncmsException {
		return getField(Field.TAG);
	}
	
	public void setTag(String tag) throws EmoncmsException {
		this.setField(Field.TAG, tag);
	}

	public boolean isPublic() throws EmoncmsException {
		return Boolean.valueOf(this.getField(Field.PUBLIC));
	}
	
	public void setPublic(boolean visible) throws EmoncmsException {
		this.setField(Field.PUBLIC, String.valueOf(visible));
	}

	public int getSize() throws EmoncmsException {
		return Integer.valueOf(this.getField(Field.SIZE));
	}

	public Datatype getDatatype() throws EmoncmsException {
		int id = Integer.valueOf(this.getField(Field.DATATYPE));
		return Datatype.getEnum(id);
	}

	public Engine getEngine() throws EmoncmsException {
		int id = Integer.valueOf(this.getField(Field.ENGINE));
		return Engine.getEnum(id);
	}

	public abstract String getField(Field field) throws EmoncmsException;

	public void setField(Field field, String value) throws EmoncmsException {
		FieldList fields = new FieldList(field, value);
		this.setFields(fields);
	}
	
	public void setFields(FieldList fields) throws EmoncmsException {
		this.setFields(fields.getValues());
	}
	
	protected abstract void setFields(Map<String, String> fields) throws EmoncmsException;

	public abstract double getLatestValue() throws EmoncmsException;

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
	
	public abstract ProcessList getProcessList() throws EmoncmsException;
	
	public abstract void setProcessList(ProcessList processes) throws EmoncmsException;
	
	public abstract void resetProcessList() throws EmoncmsException;

	public abstract void delete() throws EmoncmsException;
}
