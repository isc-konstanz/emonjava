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
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.emoncms.com.http.json.Const;

public class HttpRequestAuthentication {

	public static final String NONE = "none";

	private final String type;
	private final String key;

	public HttpRequestAuthentication(String type, String key) {
		this.type = type;
		this.key = key;
	}

	public HttpRequestAuthentication(String key) {
		this(Const.API_KEY, key);
	}

	public String getAuthentication(Charset charset) throws UnsupportedEncodingException {
		return URLEncoder.encode(type, charset.name()) + '=' + URLEncoder.encode(key, charset.name());
	}

	@Override
	public String toString() {
		return type + "=" + key;
	}
}