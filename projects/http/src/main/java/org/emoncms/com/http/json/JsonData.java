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
package org.emoncms.com.http.json;

import java.util.LinkedList;

import org.emoncms.data.Data;
import org.emoncms.data.DataList;


public class JsonData extends LinkedList<String> {
	private static final long serialVersionUID = -6298302842917994931L;

	private final Long referenceTime;
	
	
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
