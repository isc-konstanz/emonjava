/* 
 * Copyright 2016-18 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.framework.datalogger.emoncms;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;

public class EmoncmsConfig {

	private final static String CONFIG = "org.openmuc.framework.datalogger.emoncms.config";
	private final static String HTTP_SECTION = "HTTP";
	private final static String MQTT_SECTION = "MQTT";

	private final static String ADDRESS_KEY = "address";
	private final static String HTTP_ADDRESS_DEFAULT = "http://localhost/emoncms/";
	private final static String MQTT_ADDRESS_DEFAULT = "tcp://localhost:1883";
	private final static String AUTHORIZATION_KEY = "authorization";
	private final static String AUTHENTICATION_KEY = "authentication";
	private final static String MAX_THREADS_KEY = "maxThreads";
	private final static int    MAX_THREADS_DEFAULT = 1;
	public final static String MQTT_CON_TYPE = "MQTT";
	public final static String HTTP_CON_TYPE = "HTTP";
	private final static String USER_NAME_KEY = "userName";
	private final static String PASSWORD_KEY = "password";
	
	private final Preferences httpConfigs;
	private final Preferences mqttConfigs;

	public EmoncmsConfig() throws InvalidFileFormatException, IOException {
		String fileName = System.getProperty(CONFIG);
		if (fileName == null) {
			fileName = "conf" + File.separator + "emoncms.conf";
		}
		Ini ini = new Ini(new File(fileName));
		httpConfigs = new IniPreferences(ini).node(HTTP_SECTION);
		mqttConfigs = new IniPreferences(ini).node(MQTT_SECTION);
	}
	
	// HTTP
	
	public String getHttpAddress() {
		return httpConfigs.get(ADDRESS_KEY, HTTP_ADDRESS_DEFAULT);
	}

	public String getAuthorization() {
		return httpConfigs.get(AUTHORIZATION_KEY, null);
	}

	public String getAuthentication() {
		return httpConfigs.get(AUTHENTICATION_KEY, null);
	}

	public int getHttpMaxThreads() {
		return httpConfigs.getInt(MAX_THREADS_KEY, MAX_THREADS_DEFAULT);
	}

	public boolean hasAuthentication() {
		if (httpConfigs.get(AUTHENTICATION_KEY, null) != null && 
				httpConfigs.get(AUTHORIZATION_KEY, null) != null) {
			return true;
		}
		return false;
	}
	
	//MQTT
	
	public String getMqttAddress() {
		return mqttConfigs.get(ADDRESS_KEY, MQTT_ADDRESS_DEFAULT);
	}

	public int getMqttMaxThreads() {
		return mqttConfigs.getInt(MAX_THREADS_KEY, MAX_THREADS_DEFAULT);
	}

	public String getUserName() {
		return mqttConfigs.get(USER_NAME_KEY, null);
	}

	public String getPassword() {
		return mqttConfigs.get(PASSWORD_KEY, null);
	}

	public boolean hasUserName() {
		if (mqttConfigs.get(USER_NAME_KEY, null) != null && 
				mqttConfigs.get(PASSWORD_KEY, null) != null) {
			return true;
		}
		return false;
	}
}
