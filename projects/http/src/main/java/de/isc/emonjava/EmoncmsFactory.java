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
package de.isc.emonjava;

import java.util.ArrayList;
import java.util.List;

import de.isc.emonjava.com.http.HttpEmoncms;


/**
 * Factory and utility methods for {@link Emoncms} implementation, defined in this package. 
 * This class supports the following kinds of methods: 
 *
 * <ul>
 * <li>Methods that create and return an {@link HttpEmoncms} implementation, set up with commonly useful configuration settings.</li>
 * </ul>
 */
public class EmoncmsFactory {
	
	private static final List<HttpEmoncms> httpSingletonList = new ArrayList<HttpEmoncms>();
	
	
	public static Emoncms newEmoncmsHttpConnection(String address, String apiKey, int maxThreads) {

		String url = verifyAddress(address);
		
		for (HttpEmoncms emoncms : httpSingletonList) {
			if (emoncms.getAddress().equals(url) && emoncms.getApiKey().equals(apiKey)) {
				if (emoncms.getMaxThreads() != maxThreads) {
					emoncms.setMaxThreads(maxThreads);
				}
				return emoncms;
			}
		}
		HttpEmoncms emoncms = new HttpEmoncms(url, apiKey, maxThreads);
		httpSingletonList.add(emoncms);
		
		return emoncms;
	}
	
	public static Emoncms newEmoncmsHttpConnection(String address, String apiKey) {

		String url = verifyAddress(address);
		
		for (HttpEmoncms emoncms : httpSingletonList) {
			if (emoncms.getAddress().equals(url) && emoncms.getApiKey().equals(apiKey)) {
				return emoncms;
			}
		}
		HttpEmoncms emoncms = new HttpEmoncms(url, apiKey, 10);
		httpSingletonList.add(emoncms);
		
		return emoncms;
	}
	
	public static Emoncms newEmoncmsHttpConnection(String apiKey, int maxThreads) {
		
		return newEmoncmsHttpConnection("localhost", apiKey, maxThreads);
	}
	
	public static Emoncms newEmoncmsHttpConnection(String apiKey) {
		
		return newEmoncmsHttpConnection("localhost", apiKey);
	}
	
	private static String verifyAddress(String address) {

		String url;
		if (!address.startsWith("http://")) {
			url = "http://".concat(address);
		}
		else {
			url = address;
		}
		if (!url.endsWith("/")) {
			url = url.concat("/");
		}
		if (!url.endsWith("emoncms/")) {
			url = url.concat("emoncms/");
		}
		return url;
	}
}
