/* 
 * Copyright 2016-2021 ISC Konstanz
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
package org.emoncms.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.emoncms.data.Authentication;

public class HttpRequest {

	private final HttpMethod method;
	private final String domain;
	private final HttpQuery path;
	private final HttpParameters parameters;
	private final Authentication authentication;

	public HttpRequest(HttpMethod method, String domain, HttpQuery path, HttpParameters parameters, 
			Authentication authentication) {
		this.method = method;
		this.domain = domain;
		this.path = path;
		this.parameters = parameters;
		this.authentication = authentication;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getDomain() {
		return domain;
	}

	public HttpQuery getPath() {
		return path;
	}

	public String parseUri(Charset charset) throws UnsupportedEncodingException {
		return path.parse(charset);
	}

	public HttpParameters getParameters() {
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
					content += parseAuthentication(charset);
					break;
				default:
					break;
			}
		}
		return content;
	}

	public String parseAuthentication(Charset charset) throws UnsupportedEncodingException {
		if (authentication != null) {
			return URLEncoder.encode(authentication.getAuthorization().getValue(), charset.name()) + 
					'=' + URLEncoder.encode(authentication.getKey(), charset.name());
		}
		return null;
	}

	public String parse(Charset charset) throws UnsupportedEncodingException {
		String request = domain;
		if (path != null) {
			request += path.parse(charset);
		}
		if (authentication != null) {
			switch (method) {
				case POST:
				case PUT:
					break;
				default:
					if (path != null && path.size() > 0) {
						request += '&';
					}
					else {
						request += '?';
					}
					request += parseAuthentication(charset);
					break;
			}
		}
		return request;
	}

	public String toString(Charset charset) {
		try {
			String request = domain;
			if (path != null) {
				request += path.parse(charset);
			}
			if (parameters != null) {
				if (path != null && path.size() > 0) {
					request += '&';
				}
				else {
					request += '?';
				}
				request += parameters.parse(charset);
			}
			if (authentication != null) {
				if ((path != null && path.size() > 0) || (parameters != null && parameters.size() > 0)) {
					request += '&';
				}
				else {
					request += '?';
				}
				request += parseAuthentication(charset);
			}
			return request;
			
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	@Override
	public String toString() {
		return toString(StandardCharsets.UTF_8);
	}
}
