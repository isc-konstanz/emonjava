/* 
 * Copyright 2016-17 ISC Konstanz
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
package org.emoncms.com.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.emoncms.data.Authentication;


public class HttpEmoncmsRequest {

	private final String url;
	private final Authentication authentication;
	private final HttpRequestAction action;
	private final HttpRequestParameters parameters;
	private final HttpRequestMethod method;


	public HttpEmoncmsRequest(String url, Authentication authentication, 
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

	public String getAuthentication(Charset charset) throws UnsupportedEncodingException {
		if (authentication != null) {
			return URLEncoder.encode(authentication.getAuthorization().getValue(), charset.name()) + 
					'=' + URLEncoder.encode(authentication.getKey(), charset.name());
		}
		return null;
	}

	public HttpRequestAction getAction() {
		return action;
	}

	public String parseAction(Charset charset) throws UnsupportedEncodingException {
		return action.parseAction(charset);
	}

	public HttpRequestParameters getParameters() {
		return parameters;
	}

	public String parseParameters(Charset charset) throws UnsupportedEncodingException {
		String content = parameters.parseParameters(charset);
		if (authentication != null) {
			switch (method) {
			case POST:
			case PUT:
				if (parameters != null && parameters.size() > 0) {
					content += '&';
				}
				content += getAuthentication(charset);
				break;
			default:
				break;
			}
		}
		return content;
	}

	public HttpRequestMethod getMethod() {
		return method;
	}

	public String getRequest(Charset charset) throws UnsupportedEncodingException {
		String request = url;
		if (action != null) {
			request += action.parseAction(charset);
		}
		if (authentication != null) {
			switch (method) {
			case POST:
			case PUT:
				break;
			default:
				if (action != null && action.size() > 0) {
					request += '&';
				}
				else {
					request += '?';
				}
				request += getAuthentication(charset);
				break;
			}
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
			if (parameters.size() > 0 && action.size() == 0) {
				request += '?';
			}
			if (action != null && action.size() > 0) {
				request += '&';
			}
			request += parameters.toString();
		}
		return request;
	}
}
