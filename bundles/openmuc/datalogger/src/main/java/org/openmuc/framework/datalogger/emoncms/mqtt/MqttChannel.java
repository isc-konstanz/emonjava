/* 
 * Copyright 2016-19 ISC Konstanz
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
package org.openmuc.framework.datalogger.emoncms.mqtt;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.datalogger.emoncms.EngineChannel;
import org.openmuc.framework.options.Setting;

public class MqttChannel extends EngineChannel {

	@Setting(id = {"node", "nodeid"})
	private String node;

	protected void onConfigure() throws ArgumentSyntaxException {
		super.onConfigure();
		switch(getValueType()) {
		case DOUBLE:
		case FLOAT:
		case LONG:
		case INTEGER:
		case SHORT:
		case BYTE:
		case BOOLEAN:
			break;
		default:
			throw new ArgumentSyntaxException("Invalid value type: " + getValueType());
		}
	}

	public String getNode() {
		return node;
	}

}
