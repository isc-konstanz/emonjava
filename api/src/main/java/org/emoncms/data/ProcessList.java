/* 
 * Copyright 2016-21 ISC Konstanz
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
package org.emoncms.data;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class ProcessList extends LinkedHashMap<Process, String> {
	private static final long serialVersionUID = 5511638846497628586L;

	public ProcessList() {
		super();
	}
	
	public ProcessList(Process process, String value) {
		super(1);
		put(process, value);
	}
	
	public ProcessList(Process process, double value) {
		super(1);
		put(process, String.valueOf(value));
	}
	
	public ProcessList(Process process, int value) {
		super(1);
		put(process, String.valueOf(value));
	}

	public ProcessList(String value) {
		super();
		
		if (value != null && !value.isEmpty()) {
			String[] list = value.split(",");
			for (String process: list) {
				String[] arr = process.split(":");
				
				int id = Integer.valueOf(arr[0]);
				put(Process.getEnum(id), arr[1]);
			}
		}
	}
	
	public String add(Process process, String value) {
		return put(process, value);
	}
	
	public String add(Process process, double value) {
		return put(process, String.valueOf(value));
	}
	
	public String add(Process process, int value) {
		return put(process, String.valueOf(value));
	}
	
	@Override
	public String toString() {
        StringBuilder processListBuilder = new StringBuilder();

		Iterator<Map.Entry<Process, String>> iteratorProcessList = super.entrySet().iterator();
		while (iteratorProcessList.hasNext()) {
			Map.Entry<Process, String> parameter = iteratorProcessList.next();
			
			processListBuilder.append(parameter.getKey().getId());
			processListBuilder.append(':');
			processListBuilder.append(parameter.getValue());

        	if (iteratorProcessList.hasNext()) {
        		processListBuilder.append(',');
        	}
		}
        
        return processListBuilder.toString();
	}
}
