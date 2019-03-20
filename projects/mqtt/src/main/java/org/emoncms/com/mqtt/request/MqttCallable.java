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
package org.emoncms.com.mqtt.request;

import java.util.concurrent.Callable;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.JsonObject;


public class MqttCallable implements Callable<Void> {

	private final static String TOPIC_PREFIX = "emon/";
	
	private final String topic;
	private final JsonObject json;
	private String address;
	private String publisherId; // client Id 
	private String userName;
	private char[] password;
	private MqttClient mqttClient = null;

	public MqttCallable(String topic, JsonObject json, String address, String publisherId, 
			String userName, char[] password) {
		this.topic = TOPIC_PREFIX + topic;
		this.json = json;
		this.address = address;
		this.publisherId = publisherId;
		this.userName = userName;
		this.password = password;
	}

	public MqttCallable(String topic, JsonObject json, MqttClient mqttClient) {
		this.topic = TOPIC_PREFIX + topic;
		this.json = json;
		this.mqttClient = mqttClient;
	}

	public MqttClient getMqttClient() {
		return mqttClient;
	}
	
	public String getTopic() {
		return topic;
	}
	
	public JsonObject getJsonObject() {
		return json;
	}
	
	@Override
	public Void call() throws Exception {
		if (mqttClient == null) {
			connect(address, userName, password);
		}
		
		int qos = 1;
		mqttClient.publish(topic, json.toString().getBytes(), qos, true);
		return null;
	}

	private void connect(String address, String userName, char[] password) throws org.eclipse.paho.client.mqttv3.MqttException {
		mqttClient = new MqttClient(address, publisherId, 
				new MemoryPersistence());
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setUserName(userName);
		mqttConnectOptions.setPassword(password);
		
//		mqttConnectOptions.setWill(lastWillTopic, lastWillMsg.getBytes("UTF-8"),
//				1, //QoS
//				true);
		mqttClient.connect(mqttConnectOptions);
	}
	
}
