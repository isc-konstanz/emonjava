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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;


public class DataList extends LinkedList<Data> {
	private static final long serialVersionUID = 3972451487664220862L;
	
	@Override
	public boolean add(Data data) {
		boolean result = super.add(data);

        if (result) {
        	Comparator<Data> comparator = new SortTime();
    		Collections.sort(this, comparator);
        }
		return result;
	}
	
	public boolean add(int node, Timevalue timevalue) {
		boolean result = false;
		
		for (Data data : this) {
			if (data.getNode() == node && data.getTime() == timevalue.getTime()) {
				data.add(timevalue.getValue());
				
				result = true;
				break;
			}
		}
		if (!result) {
			Data newData = new Data(node, timevalue.getTime(), timevalue.getValue());
			result = this.add(newData);
		}
		return result;
	}
	
	private class SortTime implements Comparator<Data>{
		@Override
		public int compare(Data d1, Data d2){
			if (d1.getTime() == null) {
				if (d2.getTime() == null) {
					return 0;
				}
				return 1;
			}
			if (d1.getTime() < d2.getTime()){
				return -1; 
			}
			if (d1.getTime() > d2.getTime()){
				return 1; 
			}
			return 0;
		}
    }
}
