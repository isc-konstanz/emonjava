/* 
 * Copyright 2016-21 ISC Konstanz
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
package org.emoncms.http;

import java.util.ArrayList;
import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.data.Authentication;
import org.emoncms.data.Authorization;

/**
 * Builds and opens a {@link HttpConnection} instance as an {@link Emoncms} implementation.
 * 
 */
public class HttpBuilder {

	private static final List<HttpConnection> httpSingletons = new ArrayList<HttpConnection>();

	protected String domain = "http://localhost/emoncms/";

	protected Authentication authentication = null;

	protected int maxThreads = 1;

	private HttpBuilder() {
	}

	private HttpBuilder(String domain) {
		if (!domain.startsWith("http://")) {
			domain = "http://".concat(domain);
		}
		if (!domain.endsWith("/")) {
			domain = domain.concat("/");
		}
		this.domain = domain;
	}

    public static HttpBuilder create() {
        return new HttpBuilder();
    }

    public static HttpBuilder create(String address) {
        return new HttpBuilder(address);
    }

	public HttpBuilder setCredentials(String type, String key) {
		if (type != null && key != null) {
			Authorization authorization = Authorization.valueOf(type);
			authentication = new Authentication(authorization, key);
		}
		return this;
	}

	public HttpBuilder setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
		return this;
	}

	public Emoncms build() {
		HttpConnection httpConnection = null;
		for (HttpConnection connection : httpSingletons) {
			if (connection.getDomain().equals(domain)) {
				if (!connection.getAuthentication().equals(authentication)) {
					connection.setAuthentication(authentication);
				}
				if (connection.getMaxThreads() != maxThreads) {
					connection.setMaxThreads(maxThreads);
				}
				httpConnection = connection;
				break;
			}
		}
		if (httpConnection == null) {
			httpConnection = new HttpConnection(this);
			httpSingletons.add(httpConnection);
		}
		return httpConnection;
	}

}
