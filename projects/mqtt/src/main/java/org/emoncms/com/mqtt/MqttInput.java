/* 
 * Copyright 2016-19 ISC Konstanz
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
package org.emoncms.com.mqtt;

import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.mqtt.request.MqttRequestCallbacks;
import org.emoncms.data.DataList;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;


public class MqttInput implements Input {
	private static final Logger logger = LoggerFactory.getLogger(MqttInput.class);

	/**
	 * The Inputs' current callback object, which is notified of request events
	 */
	private final MqttRequestCallbacks callbacks;
	protected Integer id = null;
	protected String node;
	protected String name;
	
	
	public MqttInput(MqttRequestCallbacks callbacks, Integer id, String node, String name) {
		this.callbacks = callbacks;
		this.id = id;
		this.node = node;
		this.name = name;
	}

	public MqttInput(MqttRequestCallbacks callbacks, String node, String name) {
		this(callbacks, null, node, name);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getNode() {
		return node;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void post(Timevalue timevalue) throws EmoncmsException {
		callbacks.post(node, name, timevalue);
	}


	@Override
	public void post(List<Timevalue> timevalues) throws EmoncmsException {
		
		logger.debug("Requesting to bulk post {} data sets for input \"{}\" of node \"{}\"", timevalues.size(), name, node);
		
		DataList dataList = new DataList();
		for (Timevalue timevalue : timevalues) {
			dataList.add(node, name, timevalue);
		}
		
		callbacks.post(dataList);
	}

	@Override
	public void setProcessList(String processList) throws EmoncmsException {

		if (id != null) {
			logger.debug("Requesting to set process list for input \"{}\" of node \"{}\": {}", name, node, processList);

			JsonObject json = new JsonObject();
			json.addProperty(name, processList);
			
			callbacks.onPost(node, json);
		}
		else {
			throw new EmoncmsException("Input \""+ name + "\" of node \"" + node + "\" has no ID configured");
		}
	}

	public static Input connect(Emoncms connection, Integer id, String name, String node) throws EmoncmsUnavailableException {
		if (connection != null && connection instanceof MqttRequestCallbacks) {
			return new MqttInput((MqttRequestCallbacks) connection, id, name, node);
		}
		else throw new EmoncmsUnavailableException("HTTP connection to emoncms webserver invalid");
	}
	
	public static Input connect(Emoncms connection, String name, String node) throws EmoncmsUnavailableException {
		return connect(connection, null, name, node);
	}
}
