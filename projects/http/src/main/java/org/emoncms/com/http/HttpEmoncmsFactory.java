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
package org.emoncms.com.http;

import java.util.ArrayList;
import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.data.Authentication;
import org.emoncms.data.Authorization;


/**
 * Factory and utility methods for {@link Emoncms} implementation, defined in this package. 
 * This class supports the following kinds of methods: 
 *
 * <ul>
 * <li>Methods that create and return an {@link HttpEmoncms} implementation, set up with commonly useful configuration settings.</li>
 * </ul>
 */
public class HttpEmoncmsFactory {

	public static final String ADDRESS_DEFAULT = "http://localhost/emoncms/";
	public static final Integer MAX_THREADS_DEFAULT = 1;

	private static final List<HttpEmoncms> httpSingletonList = new ArrayList<HttpEmoncms>();


	public static Emoncms newAuthenticatedConnection(String address, 
			String authorization, String authentication, int maxThreads) {

		Authentication credentials = getCredentials(authorization, authentication);
		
		return getConnection(address, credentials, maxThreads);
	}

	public static Emoncms newAuthenticatedConnection(String address, 
			String authorization, String authentication) {

		Authentication credentials = getCredentials(authorization, authentication);
		
		return getConnection(address, credentials, null);
	}

	public static Emoncms newAuthenticatedConnection(String authorization, String authentication, int maxThreads) {

		return newAuthenticatedConnection(null, authorization, authentication, maxThreads);
	}

	public static Emoncms newAuthenticatedConnection(String authorization, String authentication) {

		return newAuthenticatedConnection(null, authorization, authentication);
	}

	public static Emoncms newConnection(String address, int maxThreads) {

		return newAuthenticatedConnection(address, null, maxThreads);
	}

	public static Emoncms newConnection(String address) {

		return newAuthenticatedConnection(address, null);
	}

	public static Emoncms newConnection() {

		return newConnection(null);
	}

	private static Emoncms getConnection(String address, Authentication credentials, Integer maxThreads) {

		if (address != null) {
			address = verifyAddress(address);
		}
		else {
			address = ADDRESS_DEFAULT;
		}

		for (HttpEmoncms emoncms : httpSingletonList) {
			if (emoncms.getAddress().equals(address)) {
				if (!emoncms.getAuthentication().equals(credentials)) {
					emoncms.setAuthentication(credentials);
				}
				if (maxThreads != null && emoncms.getMaxThreads() != maxThreads) {
					emoncms.setMaxThreads(maxThreads);
				}
				return emoncms;
			}
		}
		if (maxThreads == null) {
			maxThreads = MAX_THREADS_DEFAULT;
		}
		HttpEmoncms emoncms = new HttpEmoncms(address, credentials, maxThreads);
		httpSingletonList.add(emoncms);

		return emoncms;
	}

	private static Authentication getCredentials(String type, String key) {
		
		Authentication authentication = null;
		if (type != null && key != null) {
			Authorization authorization = Authorization.valueOf(type);
			authentication = new Authentication(authorization, key);
		}
		return authentication;
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
		return url;
	}
}
