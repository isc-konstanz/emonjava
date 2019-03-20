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

import org.emoncms.com.EmoncmsException;
import org.emoncms.com.mqtt.MqttEmoncms;
import org.emoncms.data.DataList;
import org.emoncms.data.Timevalue;

import com.google.gson.JsonObject;


/**
 * Interface used to notify the {@link MqttEmoncms} 
 * implementation about request events
 */
public interface MqttRequestCallbacks {

	void onPost(String topic, JsonObject jsonObject) 
			throws EmoncmsException;

	public void post(String topic, String name, Timevalue timevalue) 
			throws EmoncmsException;
	
	public void post(DataList data) 
			throws EmoncmsException;

}
