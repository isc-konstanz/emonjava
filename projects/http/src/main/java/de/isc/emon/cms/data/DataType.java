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
package de.isc.emon.cms.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import de.isc.emon.cms.EmoncmsException;


public class DataType {
	private final int id;
	
	
	public DataType(String key) throws EmoncmsException {
		Properties properties = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream("conf/emoncms.properties");
			properties.load(in);

		} catch (IOException e) {
        	throw new EmoncmsException("Error reading emoncms.properties: " + e.getMessage());
		}
		finally {
			try {
				in.close();
			} catch (IOException e) {
              	throw new EmoncmsException("Unknown exception while reading datatype property: " + e.getMessage());
			}
		}
		if (properties.containsKey("datatype." + key)) {
			id = Integer.valueOf(properties.getProperty("datatype." + key));
		}
		else throw new EmoncmsException("Datatype \"" + key + "\" not found in emoncms.properties");
	}
	
	public DataType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
