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
package org.emoncms.com.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.emoncms.data.Authentication;


public class HttpEmoncmsRequest {

	private final HttpRequestMethod method;
	private final String url;
	private final HttpRequestURI uri;
	private final HttpRequestParameters parameters;
	private final Authentication authentication;


	public HttpEmoncmsRequest(HttpRequestMethod method, String url, 
			HttpRequestURI uri, HttpRequestParameters parameters, 
			Authentication authentication) {

		this.method = method;
		this.url = url;
		this.uri = uri;
		this.parameters = parameters;
		this.authentication = authentication;
	}

	public HttpRequestMethod getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public HttpRequestURI getUri() {
		return uri;
	}

	public String parseUri(Charset charset) throws UnsupportedEncodingException {
		return uri.parse(charset);
	}

	public HttpRequestParameters getParameters() {
		return parameters;
	}

	public String parseParameters(Charset charset) throws UnsupportedEncodingException {
		String content = parameters.parse(charset);
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

	public String getAuthentication(Charset charset) throws UnsupportedEncodingException {
		if (authentication != null) {
			return URLEncoder.encode(authentication.getAuthorization().getValue(), charset.name()) + 
					'=' + URLEncoder.encode(authentication.getKey(), charset.name());
		}
		return null;
	}

	public String parse(Charset charset) throws UnsupportedEncodingException {
		String request = url;
		if (uri != null) {
			request += uri.parse(charset);
		}
		if (authentication != null) {
			switch (method) {
				case POST:
				case PUT:
					break;
				default:
					if (uri != null && uri.size() > 0) {
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
		try {
			String request = parse(StandardCharsets.UTF_8);
			if (parameters != null) {
				if (parameters.size() > 0 && uri.size() == 0) {
					request += '?';
				}
				if (uri != null && uri.size() > 0) {
					request += '&';
				}
				request += parseParameters(StandardCharsets.UTF_8);
			}
			return request;
			
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
}
