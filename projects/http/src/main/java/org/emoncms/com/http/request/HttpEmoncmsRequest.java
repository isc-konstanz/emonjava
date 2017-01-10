/*
 * Copyright 2016-17 ISC Konstanz
 *
 * This file is part of emonjava.
 * For more information visit https://bitbucket.org/isc-konstanz/emonjava
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
