/* 
 * Copyright 2016-18 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit mqtts://github.com/isc-konstanz/emonjava
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
 * along with emonjava.  If not, see <mqtt://www.gnu.org/licenses/>.
 */
package org.emoncms.com.mqtt;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.emoncms.Emoncms;


/**
 * Factory and utility methods for {@link Emoncms} implementation, defined in this package. 
 * This class supports the following kinds of methods: 
 *
 * <ul>
 * <li>Methods that create and return an {@link MqttEmoncms} implementation, set up with commonly useful configuration settings.</li>
 * </ul>
 */
public class MqttEmoncmsFactory {

	public static final Integer MAX_THREADS_DEFAULT = 1;

	private static final List<MqttEmoncms> mqttSingletonList = new ArrayList<MqttEmoncms>();


	public static Emoncms newAuthenticatedConnection(String address, 
			String userName, char[] password, int maxThreads) {

		return getConnection(address, null, userName, password, maxThreads);
	}
	
	public static Emoncms newAuthenticatedConnection(String address, 
			String userName, char[] password) {
		
		return getConnection(address, null, userName, password, null);
	}

	public static Emoncms newAuthenticatedConnection(String userName, char[] password, int maxThreads) {

		return newAuthenticatedConnection(null, userName, password, maxThreads);
	}

	public static Emoncms newAuthenticatedConnection(String userName, char[] password) {

		return newAuthenticatedConnection(null, userName, password);
	}

	public static Emoncms newConnection(String address, int maxThreads) {

		String userName = null;
		char[] password = null;
		return newAuthenticatedConnection(address, userName, password, maxThreads);
	}

	public static Emoncms newConnection(String address) {

		String userName = null;
		char[] password = null;
		return newAuthenticatedConnection(address, userName, password);
	}

	public static Emoncms newConnection() {

		return newConnection(null);
	}

	private static Emoncms getConnection(String address, String publisherId, 
			final String userName, final char[]password, Integer maxThreads) {

		if (address != null) {
			address = verifyAddress(address);
		}
		else {
			throw new InvalidParameterException("Wrong Address!");
		}

		for (MqttEmoncms emoncms : mqttSingletonList) {
			if (emoncms.getAddress().equals(address)) {
				if (maxThreads != null && emoncms.getMaxThreads() != maxThreads) {
					emoncms.setMaxThreads(maxThreads);
				}
				return emoncms;
			}
		}
		if (maxThreads == null) {
			maxThreads = MAX_THREADS_DEFAULT;
		}
		MqttEmoncms emoncms = new MqttEmoncms(address, publisherId, userName, password, maxThreads);
		mqttSingletonList.add(emoncms);

		return emoncms;
	}

	private static String verifyAddress(String address) {

		return address;
	}
}
