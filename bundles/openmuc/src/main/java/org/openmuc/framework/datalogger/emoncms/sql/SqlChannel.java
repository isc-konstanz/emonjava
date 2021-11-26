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
package org.openmuc.framework.datalogger.emoncms.sql;

import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.option.annotation.Option;
import org.openmuc.framework.datalogger.annotation.Configure;
import org.openmuc.framework.datalogger.emoncms.EngineChannel;

public class SqlChannel extends EngineChannel {

	@Option(id = {"input", "inputid"}, mandatory = false)
	private int input = -1;

	@Option(id = {"feed", "feedid"}, mandatory = false)
	private int feed = -1;

	@Configure
	public void configure(SqlEngine engine) throws ArgumentSyntaxException {
		if (engine.client.hasCache()) {
			if (input < 0) {
				throw new ArgumentSyntaxException("Input ID needs to be configured for redis caching");
			}
			if (feed < 0) {
				throw new ArgumentSyntaxException("Feed ID needs to be configured for redis caching");
			}
		}
	}

	public boolean hasInput() {
		return input > 0;
	}

	public int getInput() {
		return input;
	}

	public boolean hasFeed() {
		return feed > 0;
	}

	public int getFeed() {
		return feed;
	}

}
