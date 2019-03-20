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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.emoncms.Emoncms;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.mqtt.request.MqttCallable;
import org.emoncms.com.mqtt.request.MqttRequestCallbacks;
import org.emoncms.data.Data;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class MqttEmoncms implements Emoncms, MqttRequestCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(MqttEmoncms.class);
	
	public static final String TIME = "time";

	private final String address;
	private final String userName;
	private final char[] password;
	private String publisherId;
	
	private MqttClient mqttClient = null;

	private int maxThreads;
	private ThreadPoolExecutor executor = null;

	public MqttEmoncms(String address, String publisherId, String userName, char[] password, int maxThreads) {
		this.address = address;
		if (publisherId == null) {
			this.publisherId = MqttClient.generateClientId();
		}
		else {
			this.publisherId = publisherId;
		}
		this.userName = userName;
		this.password = password;
		this.maxThreads = maxThreads;
	}

	public String getAddress() {
		return address;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int max) {
		this.maxThreads = max;
	}

	@Override
	public void start() throws EmoncmsUnavailableException {
		logger.info("Initializing Energy Monitoring Content Management System communication \"{}\"", address);
		initialize();
	}

	private void initialize() {
		// The HttpURLConnection implementation is in older JREs somewhat buggy with keeping connections alive. 
		// To avoid this, the mqtt.keepAlive system property can be set to false. 
		System.setProperty("mqtt.keepAlive", "false");
		
		if (executor != null) {
			executor.shutdown();
		}
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory("EmonJava HTTP request pool - thread-");
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads, namedThreadFactory);
	}

	@Override
	public void stop() {
		logger.info("Shutting emoncms connection \"{}\" down", address);
		
		if (executor != null) {
			executor.shutdown();
		}
		
		if (mqttClient != null) {
			try {
				mqttClient.disconnect();
			} catch (MqttException e) {
			}
			mqttClient = null;
		}
			
	}

	@Override
	public void post(String topic, String name, Timevalue timevalue) throws EmoncmsException {
		logger.debug("Sending to {} for input \"{}\" to topic \"{}\"", timevalue, name, topic);

		JsonObject json = new JsonObject();
		Long time =  getTimeInSeconds(timevalue.getTime());
		json.addProperty(TIME, time);
		json.addProperty(name, timevalue.getValue());
		
		submitRequest(topic, json);
	}

	@Override
	public void post(String topic, Long time, List<Namevalue> namevalues) throws EmoncmsException {
		logger.debug("Sending values for {} inputs to topic \\\"{}\\\"", namevalues.size(), topic);
		
		JsonObject json = new JsonObject();
		time =  getTimeInSeconds(time);
		json.addProperty(TIME, time);
		for (Namevalue namevalue : namevalues) {
			json.addProperty(namevalue.getName(), namevalue.getValue());
		}
		
		submitRequest(topic, json);
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
	
 	@Override
	public void onPost(String topic, JsonObject json) 
			throws EmoncmsException {
		submitRequest(topic, json);
	}

	private synchronized void submitRequest(String topic, JsonObject json) throws EmoncmsException {
		if (logger.isTraceEnabled()) {
			logger.trace("Requesting \"{}\" for Topic \"{}\"", json.toString(), topic);
		}
		
		try {
			if (mqttClient != null && !mqttClient.isConnected()) {
				mqttClient.disconnect();
				mqttClient = null;
			}
						
			MqttCallable task;
			if (mqttClient == null) {
				task = new MqttCallable(topic, json, address, publisherId, userName, password);
			}
			else {
				task = new MqttCallable(topic, json, mqttClient);
			}
			
			executor.submit(task);
			mqttClient = task.getMqttClient();
		} catch (Exception e) {
			initialize();
			throw new EmoncmsException("Energy Monitoring Content Management communication failed: " + e);
		}
	}

	private class NamedThreadFactory implements ThreadFactory {

		private final String name;
		private final AtomicInteger counter = new AtomicInteger(0);

		public NamedThreadFactory(String name) {
			this.name = name;
		}

		@Override
		public Thread newThread(Runnable r) {
			String threadName = name + counter.incrementAndGet();
			return new Thread(r, threadName);
		}
	}
}
