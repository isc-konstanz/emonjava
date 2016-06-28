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
package de.isc.emonjava.data;

import java.util.LinkedList;

import de.isc.emonjava.Feed;
import de.isc.emonjava.com.EmoncmsException;


public class FeedData {
	private final Feed feed;

	private String name;
	private String tag;
	private boolean visible;
	private int size;
	private Datatype datatype;
	private Engine engine;
	private Timevalue timevalue;


	public FeedData(Feed feed, String name, String tag, boolean visible, int size, Datatype datatype, Engine engine, Timevalue value) {
		this.feed = feed;
		this.name = name;
		this.tag = tag;
		this.visible = visible;
		this.size = size;
		this.datatype = datatype;
		this.engine = engine;
		this.timevalue = value;
	}

	public Feed getService() {
		return feed;
	}

	public int getId() {
		return feed.getId();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) throws EmoncmsException {
		feed.setField(Field.NAME, name);
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) throws EmoncmsException {
		feed.setField(Field.TAG, tag);
		this.tag = tag;
	}

	public boolean isPublic() {
		return visible;
	}

	public void setPublic(boolean visible) throws EmoncmsException {
		feed.setField(Field.PUBLIC, String.valueOf(visible));
		this.visible = visible;
	}

	public int getSize() {
		return size;
	}

	public Datatype getDatatype() {
		return datatype;
	}

	public Engine getEngine() {
		return engine;
	}

	public String getField(Field field) throws EmoncmsException {
		switch (field) {
		case NAME:
			return name;
		case TAG:
			return tag;
		case TIME:
			return String.valueOf(timevalue.getTime());
		case VALUE:
			return String.valueOf(timevalue.getTime());
		case PUBLIC:
			return String.valueOf(visible);
		case SIZE:
			return String.valueOf(size);
		case DATATYPE:
			return String.valueOf(datatype.getValue());
		case ENGINE:
			return String.valueOf(engine.getValue());
		default:
			return feed.getField(field);
		}
	}

	public void setField(Field field, String value) throws EmoncmsException {
		feed.setField(field, value);

		switch (field) {
		case NAME:
			this.name = value;
		case TAG:
			this.tag = value;
		case PUBLIC:
			this.visible = Boolean.valueOf(value);
		default:
			feed.setField(field, value);
		}
	}

	public double getLatestValue() throws EmoncmsException {
		return feed.getLatestValue();
	}

	public Timevalue getLatestTimevalue() throws EmoncmsException {
		return feed.getLatestTimevalue();
	}

	public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
		return feed.getData(start, end, interval);
	}

	public void insertData(Timevalue timevalue) throws EmoncmsException {
		feed.insertData(timevalue);
	}

	public void updateData(Timevalue timevalue) throws EmoncmsException {
		feed.updateData(timevalue);
	}

	public void deleteData(long time) throws EmoncmsException {
		feed.deleteData(time);
	}
	
	public void deleteDataRange(long start, long end) throws EmoncmsException {
		feed.deleteDataRange(start, end);
	}

	public ProcessList getProcessList() throws EmoncmsException {
		return feed.getProcessList();
	}

	public void setProcessList(ProcessList processList) throws EmoncmsException {
		feed.setProcessList(processList);
	}

	public void resetProcessList() throws EmoncmsException {
		feed.resetProcessList();
	}

	public void delete() throws EmoncmsException {
		feed.delete();
	}
}
