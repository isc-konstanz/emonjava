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
package org.emoncms.mqtt;

import java.util.ArrayList;
import java.util.List;

import org.emoncms.Emoncms;

/**
 * Builds and opens a {@link MqttClient} instance as an {@link Emoncms} implementation.
 * 
 */
public class MqttBuilder {

	private static final List<MqttClient> mqttSingletons = new ArrayList<MqttClient>();

	protected String id = null;
	protected String domain = "tcp://localhost";
	protected int port = 1883;

	protected String user = null;
	protected char[] password = null;

	private MqttBuilder() {
	}

	private MqttBuilder(String domain) {
		if (!domain.startsWith("tcp://")) {
			domain = "tcp://".concat(domain);
		}
		if (domain.endsWith("/")) {
			domain = domain.substring(0, domain.length()-1);
		}
		this.domain = domain;
	}

	public static MqttBuilder create() {
		return new MqttBuilder();
	}

	public static MqttBuilder create(String address) {
		return new MqttBuilder(address);
	}

	public MqttBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public MqttBuilder setCredentials(String user, String password) {
		this.user = user;
		this.password = password.toCharArray();
		return this;
	}

	public Emoncms build() {
		MqttClient mqttClient = null;
		for (MqttClient emoncms : mqttSingletons) {
			if (emoncms.getDomain().equals(domain) && emoncms.getPort() == port) {
				mqttClient = emoncms;
				break;
			}
		}
		if (mqttClient == null) {
			mqttClient = new MqttClient(this);
			mqttSingletons.add(mqttClient);
		}
		return mqttClient;
	}

}
