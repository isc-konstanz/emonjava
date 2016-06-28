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

import de.isc.emonjava.Input;
import de.isc.emonjava.com.EmoncmsException;

public class InputData {
	private final Input input;

	private String description;
	private ProcessList processList;
	private Timevalue timevalue;


	public InputData(Input input, String description, ProcessList processList, Timevalue value) {
		this.input = input;
		this.description = description;
		this.processList = processList;
		this.timevalue = value;
	}

	public Input getService() {
		return input;
	}

	public int getId() {
		return input.getId();
	}

	public String getNode() {
		return input.getNode();
	}

	public String getName() {
		return input.getName();
	}
	
	public void setName(String name) throws EmoncmsException {
		input.setName(name);
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) throws EmoncmsException {
		input.setDescription(description);
	}
	
	public Timevalue getLatestTimevalue() {
		return timevalue;
	}

	public void post(Timevalue timevalue) throws EmoncmsException {
		input.post(timevalue);
		if (timevalue.getTime() == null ||
				(this.timevalue.getTime() != null && timevalue.getTime() > timevalue.getTime())) {
			
			this.timevalue = timevalue;
		}
	}

	public ProcessList getProcessList() {
		return processList;
	}

	public void setProcessList(ProcessList processList) throws EmoncmsException {
		input.setProcessList(processList);
		this.processList = processList;
	}

	public void resetProcessList() throws EmoncmsException {
		input.resetProcessList();
	}

	public void delete() throws EmoncmsException {
		input.delete();
	}
}
