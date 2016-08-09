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
package org.emoncms.com.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.emoncms.com.http.json.Const;


public class HttpEmoncmsRequest {

	private final String url;
	private final String apiKey;
	private final HttpRequestAction action;
	private final HttpRequestParameters parameters;
	private final HttpRequestMethod method;


	public HttpEmoncmsRequest(String url, String apiKey, 
			HttpRequestAction action, HttpRequestParameters parameters, 
			HttpRequestMethod method) {
		
		this.url = url;
		this.apiKey = apiKey;
		this.action = action;
		this.parameters = parameters;
		this.method = method;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getApiKey() {
		return apiKey;
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
		if (apiKey != null) {
			if (action != null && action.size() > 0) {
				request += "&";
			}
			request += Const.API_KEY + "=" + URLEncoder.encode(apiKey, "UTF-8");
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
		if (apiKey != null) {
			if (parameters != null && parameters.size() > 0 &&
					action != null && action.size() > 0) {
				
				request += "&";
			}
			request += Const.API_KEY + "=" + apiKey;
		}
		return request;
	}
}
