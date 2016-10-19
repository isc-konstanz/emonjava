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
package org.emoncms.com.http;

import org.emoncms.com.EmoncmsException;
import org.emoncms.com.http.request.HttpRequestCallbacks;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Field;
import org.emoncms.data.Timevalue;


public class HttpFeedData extends HttpFeed {

	private String name;
	private String tag;
	private boolean visible;
	private int size;
	private Datatype datatype;
	private Engine engine;
	private Timevalue timevalue;


	public HttpFeedData(HttpRequestCallbacks callbacks, int id, 
			String name, String tag, boolean visible, int size, Datatype datatype, Engine engine, Timevalue value) {
		super(callbacks, id);
		
		this.name = name;
		this.tag = tag;
		this.visible = visible;
		this.size = size;
		this.datatype = datatype;
		this.engine = engine;
		this.timevalue = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) throws EmoncmsException {
		super.setField(Field.NAME, name);
		this.name = name;
	}

	@Override
	public String getTag() {
		return tag;
	}

	@Override
	public void setTag(String tag) throws EmoncmsException {
		super.setField(Field.TAG, tag);
		this.tag = tag;
	}

	@Override
	public boolean isPublic() {
		return visible;
	}

	@Override
	public void setPublic(boolean visible) throws EmoncmsException {
		super.setField(Field.PUBLIC, String.valueOf(visible));
		this.visible = visible;
	}

	@Override
	public int getSize() {
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
			return super.getField(field);
		}
	}

	@Override
	public void setField(Field field, String value) throws EmoncmsException {
		super.setField(field, value);

		switch (field) {
		case NAME:
			this.name = value;
		case TAG:
			this.tag = value;
		case PUBLIC:
			this.visible = Boolean.valueOf(value);
		default:
			super.setField(field, value);
		}
	}
}
