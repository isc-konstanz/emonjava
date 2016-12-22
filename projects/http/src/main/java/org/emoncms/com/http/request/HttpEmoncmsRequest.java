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
package org.emoncms.com.http.request;

import java.io.UnsupportedEncodingException;


public class HttpEmoncmsRequest {

	private final String url;
	private final HttpRequestAuthentication authentication;
	private final HttpRequestAction action;
	private final HttpRequestParameters parameters;
	private final HttpRequestMethod method;


	public HttpEmoncmsRequest(String url, HttpRequestAuthentication authentication, 
			HttpRequestAction action, HttpRequestParameters parameters, 
			HttpRequestMethod method) {
		
		this.url = url;
		this.authentication = authentication;
		this.action = action;
		this.parameters = parameters;
		this.method = method;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getAuthentication() throws UnsupportedEncodingException {
		return authentication.getAuthentication();
	}

	public HttpRequestAction getAction() {
		return action;
	}
	
	public String parseAction() throws UnsupportedEncodingException {
		return action.parseAction();
	}

	public HttpRequestParameters getParameters() {
		return parameters;
	}
	
	public String parseParameters() throws UnsupportedEncodingException {
		return parameters.parseParameters();
	}
	
	public HttpRequestMethod getMethod() {
		return method;
	}
	
	public String getRequest() throws UnsupportedEncodingException {
		
		String request = url;
		if (action != null) {
			request += action.parseAction();
		}
		if (authentication != null) {
			if (action != null && action.size() > 0) {
				request += "&";
			}
			request += authentication.getAuthentication();
		}
		return request;
	}

	@Override
	public String toString() {
		
		String request = url;
		if (action != null) {
			request += action.toString();
		}
		if (parameters != null) {
			if (action != null && action.size() > 0) {
				request += "&";
			}
			request += parameters.toString();
		}
		if (authentication != null) {
			if (parameters != null && parameters.size() > 0 &&
					action != null && action.size() > 0) {
				
				request += "&";
			}
			request += authentication.toString();
		}
		return request;
	}
}
