/* 
 * Copyright 2016-20 ISC Konstanz
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

import java.util.List;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.Input;
import org.emoncms.data.DataList;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttInput implements Input {
	private static final Logger logger = LoggerFactory.getLogger(MqttInput.class);

    /**
     * Interface used by {@link MqttInput} to notify the {@link MqttClient} about events
     */
    public interface MqttCallbacks {

    	void post(String topic, String name, Timevalue timevalue) throws EmoncmsException;

    	void post(DataList data) throws EmoncmsException;
    }

	/**
	 * The Inputs' current callback object, which is notified of request events
	 */
	private final MqttCallbacks callbacks;

	protected String node;
	protected String name;

	public MqttInput(MqttCallbacks callbacks, String node, String name) {
		this.callbacks = callbacks;
		
		this.node = node;
		this.name = name;
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.HTTP;
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

	public static Input connect(MqttCallbacks callbacks, String name, String node) throws EmoncmsUnavailableException {
		if (callbacks == null) {
			throw new EmoncmsUnavailableException("MQTT connection to emoncms webserver invalid");
		}
		return new MqttInput(callbacks, name, node);
	}

}
