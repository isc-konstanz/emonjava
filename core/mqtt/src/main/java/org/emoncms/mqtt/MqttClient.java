/* 
 * Copyright 2016-21 ISC Konstanz
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
package org.emoncms.mqtt;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.emoncms.Emoncms;
import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.data.Data;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.mqtt.MqttInput.MqttCallbacks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class MqttClient implements Emoncms, MqttCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(MqttClient.class);

	private static final String TIME = "time";

	private final String id;
	private final String domain;
	private final int port;

	private final String user;
	private final char[] password;

	private MqttAsyncClient client = null;

	protected MqttClient(String id, String domain, int port, String user, char[] password) {
		if (id == null) {
			id = org.eclipse.paho.client.mqttv3.MqttClient.generateClientId();
		}
		this.id = id;
		
		this.domain = domain;
		this.port = port;
		this.user = user;
		this.password = password;
	}

	protected MqttClient(MqttBuilder builder) {
		this(builder.id, builder.domain, builder.port, builder.user, builder.password);
	}

	public String getDomain() {
		return domain;
	}

	public int getPort() {
		return port;
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.MQTT;
	}

	@Override
	public boolean isClosed() {
		if (client != null && client.isConnected()) {
			return true;
		}
		return false;
	}

	@Override
	public void close() {
		logger.info("Shutting emoncms MQTT connection \"{}:{}\" down", domain, port);
		if (client != null) {
			try {
				client.disconnect();
				
			} catch (MqttException e) {
			}
			client = null;
		}
	}

	@Override
	public void open() throws EmoncmsUnavailableException {
		logger.info("Initializing emoncms MQTT connection \"{}:{}\"", domain, port);
		initialize();
	}

	private void initialize() throws EmoncmsUnavailableException  {
		try {
			client = new MqttAsyncClient(domain+":"+port, id, 
					new MemoryPersistence());
			MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
			mqttConnectOptions.setUserName(user);
			mqttConnectOptions.setPassword(password);
			
//			mqttConnectOptions.setWill(lastWillTopic, lastWillMsg.getBytes("UTF-8"),
//					1, //QoS
//					true);
			IMqttToken token = client.connect(mqttConnectOptions);
			token.waitForCompletion();
			
		} catch (MqttException e) {
			throw new EmoncmsUnavailableException(e.getMessage());
		}
	}

	@Override
	public void post(String topic, String name, Timevalue timevalue) throws EmoncmsException {
		logger.debug("Sending to {} for input \"{}\" to topic \"{}\"", timevalue, name, topic);
		
		JsonObject json = new JsonObject();
		Long time =  getTimeInSeconds(timevalue.getTime());
		json.addProperty(TIME, time);
		json.addProperty(name, timevalue.getValue());
		
		publish(topic, json);
	}

	@Override
	public void post(String topic, Long time, List<Namevalue> namevalues) throws EmoncmsException {		
		JsonObject json = new JsonObject();
		time =  getTimeInSeconds(time);
		json.addProperty(TIME, time);
		for (Namevalue namevalue : namevalues) {
			json.addProperty(namevalue.getName(), namevalue.getValue());
		}
		logger.debug("Sending values for {} inputs {} to topic \"{}\"", namevalues.size(), json.toString(), topic);
		
		publish(topic, json);
	}

	@Override
	public void post(DataList dataList) throws EmoncmsException {
		logger.debug("Requesting to bulk post {} data sets", dataList.size());
		
		dataList.sort();
		for (Data data : dataList) {
			post(data.getNode(), data.getTime(), data.getNamevalues());
		}
	}

	private Long getTimeInSeconds(Long time) {
		if (time == null || time <= 0) {
			time = System.currentTimeMillis();
		}
		return TimeUnit.MILLISECONDS.toSeconds(time);
	}

	private void publish(String topic, JsonObject json) throws EmoncmsException {
		if (isClosed()) {
			throw new EmoncmsException("Unable to publish for closed connection");
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Requesting \"{}\" for Topic \"{}\"", json.toString(), topic);
		}
		try {
			int quality = 1;
			client.publish(topic, json.toString().getBytes(), quality, true);
			
		} catch (MqttException e) {
			throw new EmoncmsException("Energy Monitoring Content Management communication failed: " + e);
		}
	}

}
