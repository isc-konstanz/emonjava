/* 
 * Copyright 2016-17 ISC Konstanz
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
package org.emoncms.com.http.json;

import com.google.gson.annotations.SerializedName;

public class JsonFeed {
	int id;
	String name;
	String tag;
	@SerializedName("public")
	String visible;
	int datatype;
	int engine;
	int size;
	String processList;
	String time;
	String value;


	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}

	public boolean isPublic() {
		if (visible.equals("1")) return true;
		else return false;
	}

	public int getDatatype() {
		return datatype;
	}

	public int getEngine() {
		return engine;
	}

	public int getSize() {
		return size;
	}

	public String getProcessList() {
		return processList;
	}

	public Long getTime() {
		if (time == null || time.isEmpty()) return null;
		return Long.valueOf(time);
	}

	public Double getValue() {
		if (value == null || value.isEmpty()) return null;
		return Double.valueOf(value);
	}
}
