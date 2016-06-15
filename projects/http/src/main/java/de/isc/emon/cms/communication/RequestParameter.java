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
package de.isc.emon.cms.communication;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class RequestParameter {
	private final RequestMethod method;
	
	private final String key;
	private final String value;


	public RequestParameter(RequestMethod method, String key, String value) {
		this.key = key;
		this.value = value;
		
		this.method = method;
	}
	
	public RequestParameter(String key, String value) {
		this(RequestMethod.GET, key, value);
	}
	
	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public RequestMethod getMethod() {
		return method;
	}
	
	public String parseParameter() throws UnsupportedEncodingException {
        StringBuilder parameter = new StringBuilder();
        parameter.append(URLEncoder.encode(key, "UTF-8"));
        parameter.append('=');
        parameter.append(URLEncoder.encode(String.valueOf(value), "UTF-8"));
        
        return parameter.toString();
	}

	@Override
	public String toString() {
		StringBuilder parameter = new StringBuilder();
	    parameter.append(key);
	    parameter.append("=");
	    parameter.append(value);
    
	    return parameter.toString();
	}
}
