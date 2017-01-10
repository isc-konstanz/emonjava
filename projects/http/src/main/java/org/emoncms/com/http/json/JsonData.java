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
package org.emoncms.com.http.json;

import java.util.LinkedList;

import org.emoncms.data.Data;
import org.emoncms.data.DataList;


public class JsonData extends LinkedList<String> {
	private static final long serialVersionUID = -6298302842917994931L;

	Long referenceTime;
	
	
	public JsonData(DataList dataList) {
		super();
		
		// Get the last time value as reference time
		Long time = dataList.getLast().getTime();
		
		// If it is null, use now as a reference, except if no time values were passed at all
		if (time == null && dataList.getFirst().getTime() != null) {
			time = System.currentTimeMillis();
		}
		referenceTime = time;
		
		for (Data data : dataList) {
			add(data);
		}
	}
	
	public boolean add(Data data) {
		LinkedList<String> dataList = new LinkedList<String>();
		
		// Calculate the offset from the passed time value in seconds.
		long offset;
		if (referenceTime != null) {
			offset = (referenceTime - data.getTime())/1000;
		}
		else {
			offset = 0;
		}
		dataList.add(String.valueOf(offset));
		dataList.add(String.valueOf(data.getNode()));
		for (Double value : data.getValues()) {
			dataList.add(String.valueOf(value));
		}
		
		return super.add(dataList.toString());
	}
	
	public Long getTime() {
		return referenceTime;
	}
}
