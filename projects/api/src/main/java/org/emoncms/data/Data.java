/* 
 * Copyright 2016-19 ISC Konstanz
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

import java.util.LinkedList;
import java.util.List;


public class Data {
	private final Long time;
	private final String node;
	private final List<Namevalue> namevalues = new LinkedList<Namevalue>();
	
	
	public Data(Long time, String node, Namevalue namevalue) {
		this.time = time;
		this.node = node;
		namevalues.add(namevalue);
	}

	public void add(Namevalue namevalue) {
		namevalues.add(namevalue);
	}

	public Long getTime() {
		return time;
	}

	public String getNode() {
		return node;
	}
	
	public List<Namevalue> getNamevalues() {
		return namevalues;
	}
	
}
