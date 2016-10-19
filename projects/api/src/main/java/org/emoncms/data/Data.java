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
package org.emoncms.data;

import java.util.LinkedList;
import java.util.List;


public class Data {
	private final int node;
	private final Long time;
	private final List<Double> values = new LinkedList<Double>();
	
	
	public Data(int node, Long time, double value) {
		this.node = node;
		this.time = time;
		values.add(value);
	}

	public void add(double value) {
		values.add(value);
	}

	public int getNode() {
		return node;
	}

	public Long getTime() {
		return time;
	}
	
	public List<Double> getValues() {
		return values;
	}
}
