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
package org.emoncms.com.http;

import java.util.ArrayList;
import java.util.List;

import org.emoncms.Emoncms;


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


	public static Emoncms newAuthenticatedHttpEmoncmsConnection(String address, String apiKey, int maxThreads) {

		String url = verifyAddress(address);

		for (HttpEmoncms emoncms : httpSingletonList) {
			if (emoncms.getAddress().equals(url)) {
				if (!emoncms.getApiKey().equals(apiKey)) {
					emoncms.setApiKey(apiKey);
				}
				if (emoncms.getMaxThreads() != maxThreads) {
					emoncms.setMaxThreads(maxThreads);
				}
				return emoncms;
			}
		}
		HttpEmoncms emoncms = new HttpEmoncms(url, apiKey, maxThreads);
		httpSingletonList.add(emoncms);

		return emoncms;
	}

	public static Emoncms newAuthenticatedHttpEmoncmsConnection(String address, String apiKey) {

		String url = verifyAddress(address);

		for (HttpEmoncms emoncms : httpSingletonList) {
			if (emoncms.getAddress().equals(url)) {
				if (!emoncms.getApiKey().equals(apiKey)) {
					emoncms.setApiKey(apiKey);
				}
				return emoncms;
			}
		}
		HttpEmoncms emoncms = new HttpEmoncms(url, apiKey, MAX_THREADS_DEFAULT);
		httpSingletonList.add(emoncms);

		return emoncms;
	}

	public static Emoncms newAuthenticatedHttpEmoncmsConnection(String apiKey, int maxThreads) {

		return newAuthenticatedHttpEmoncmsConnection(ADDRESS_DEFAULT, apiKey, maxThreads);
	}

	public static Emoncms newAuthenticatedHttpEmoncmsConnection(String apiKey) {

		return newAuthenticatedHttpEmoncmsConnection(ADDRESS_DEFAULT, apiKey);
	}

	public static Emoncms newHttpEmoncmsConnection(String address, int maxThreads) {

		return newAuthenticatedHttpEmoncmsConnection(address, null, maxThreads);
	}

	public static Emoncms newHttpEmoncmsConnection(String address) {

		return newAuthenticatedHttpEmoncmsConnection(address, null);
	}

	public static Emoncms newHttpEmoncmsConnection() {

		return newAuthenticatedHttpEmoncmsConnection(null);
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
