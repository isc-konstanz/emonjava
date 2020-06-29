/* 
 * Copyright 2016-20 ISC Konstanz
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
import org.openmuc.framework.datalogger.Channel;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.options.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineChannel extends Channel {
	private final static Logger logger = LoggerFactory.getLogger(EngineChannel.class);

	@Setting(id = {"engine", "logger"}, mandatory = false)
	private EmoncmsType engine;

	protected EngineChannel() {
	}

	EngineChannel(LogChannel channel) throws ArgumentSyntaxException {
		this.doConfigure(channel);
	}

    @Override
    protected void onConfigure() throws ArgumentSyntaxException {
		try {
			if (engine == null) {
				engine = EmoncmsType.valueOf(EngineLogger.DEFAULT);
			}
		} catch (IllegalArgumentException e) {
			throw new ArgumentSyntaxException(e.getMessage());
		}
    }

	public EmoncmsType getEngine() {
		return engine;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			logger.trace("Skipped logging an invalid or empty value for channel \"{}\": {}", getId(), getFlag());
			return false;
		}
		logger.trace("Preparing record to log for channel {}", this);
		return true;
	}

}
