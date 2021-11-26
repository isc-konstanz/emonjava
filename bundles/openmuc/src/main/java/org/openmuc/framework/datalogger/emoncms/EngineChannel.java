/* 
 * Copyright 2016-2021 ISC Konstanz
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

import org.emoncms.EmoncmsType;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.datalogger.LoggingChannel;
import org.openmuc.framework.datalogger.annotation.Configure;

public class EngineChannel extends LoggingChannel {

	public final static String ENGINE = "engine";
	public final static String LOGGER = "logger";

	@Option(id = {ENGINE, LOGGER}, mandatory = false)
	private EmoncmsType engine;

	@Configure
	public void configure() throws ArgumentSyntaxException {
		try {
			if (engine == null) {
				engine = EmoncmsType.valueOf(EngineLogger.DEFAULT);
			}
		} catch (IllegalArgumentException e) {
			throw new ArgumentSyntaxException(e.getMessage());
		}
	}

	final void invokeConfigure(Engine<? extends EngineChannel> engine) throws ArgumentSyntaxException {
		invokeMethod(Configure.class, this, engine);
	}

	public EmoncmsType getEngine() {
		return engine;
	}

}
