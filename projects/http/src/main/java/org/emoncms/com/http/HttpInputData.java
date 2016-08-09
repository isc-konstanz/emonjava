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
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;


public class HttpInputData extends HttpInput {

	private String description;
	private ProcessList processList;
	private Timevalue timevalue;


	public HttpInputData(HttpInputCallbacks callbacks, int id, String node, String name, 
			String description, ProcessList processList, Timevalue value) {
		super(callbacks, id, node, name);
		
		this.description = description;
		this.processList = processList;
		this.timevalue = value;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) throws EmoncmsException {
		super.setDescription(description);
		this.description = description;
	}

	public Timevalue getLatestTimevalue() {
		return timevalue;
	}

	@Override
	public void post(Timevalue timevalue) throws EmoncmsException {
		super.post(timevalue);
		if (timevalue.getTime() == null ||
				(this.timevalue.getTime() != null && timevalue.getTime() > timevalue.getTime())) {
			
			this.timevalue = timevalue;
		}
	}

	@Override
	public ProcessList getProcessList() {
		return processList;
	}

	@Override
	public void setProcessList(ProcessList processList) throws EmoncmsException {
		super.setProcessList(processList);
		this.processList = processList;
	}
}
