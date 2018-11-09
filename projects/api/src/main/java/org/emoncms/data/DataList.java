/* 
 * Copyright 2016-18 ISC Konstanz
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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;


public class DataList extends LinkedList<Data> {
	private static final long serialVersionUID = 3972451487664220862L;

	public boolean add(Long time, String node, Namevalue namevalue) {
		boolean result = false;
		
		// Posted UNIX time values need to be sent in seconds
		time = TimeUnit.MILLISECONDS.toSeconds(time);
		for (Data data : this) {
			if (data.getNode().equals(node) && data.getTime().equals(time)) {
				data.add(namevalue);
				
				result = true;
				break;
			}
		}
		if (!result) {
			Data newData = new Data(time, node, namevalue);
			result = add(newData);
		}
		return result;
	}

	public boolean add(String node, String name, Timevalue timevalue) {
		return add(timevalue.getTime(), node, new Namevalue(timevalue.getValue()));
	}

	public void sort() {
    	Comparator<Data> comparator = new SortTime();
		sort(comparator);
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
			if (d1.getTime() > d2.getTime()){
				return 1; 
			}
			else if (d1.getTime() < d2.getTime()){
				return -1; 
			}
			return 0;
		}
    }
}
