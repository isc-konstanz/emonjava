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
package org.openmuc.framework.datalogger.emoncms;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

public class GeneralConfig {

	protected final static String CONFIG = "org.openmuc.framework.datalogger.emoncms.config";
	protected final static String SECTION = "General";

	protected final static String DISABLED_KEY = "disabled";

	protected final static String TYPE_KEY = "type";
	protected final static EmoncmsType TYPE_DEFAULT = EmoncmsType.MQTT;

	protected final Ini configs;

	public static GeneralConfig load() throws InvalidFileFormatException, IOException {
		String fileName = System.getProperty(CONFIG);
		if (fileName == null) {
			fileName = "conf" + File.separator + "emoncms.conf";
		}
		return new GeneralConfig(new Ini(new File(fileName)));
	}

	protected GeneralConfig(Ini configs) {
		this.configs = configs;
	}

	public EmoncmsType getDefault() {
		String type = configs.get(SECTION, TYPE_KEY);
		if (type != null && !type.isEmpty()) {
			return EmoncmsType.valueOf(type);
		}
		return TYPE_DEFAULT;
	}

	public boolean contains(EmoncmsType type) {
		if (configs.containsKey(type.name())) {
			return configs.get(type.name()).get(DISABLED_KEY, Boolean.class, true);
		}
		return false;
	}

	public <C extends Configuration> C get(EmoncmsType type, Class<C> clazz) throws EmoncmsException {
        try {
        	Section section = configs.get(type.name());
        	if (section == null) {
        		section = configs.get("Emoncms");
        	}
			return clazz.getDeclaredConstructor(Section.class).newInstance(section);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new EmoncmsException(e);
		}
	}

	static abstract class Configuration {

		protected final Section configs;

		protected Configuration(Section configs) throws EmoncmsException {
			if (configs == null) {
				throw new EmoncmsException();
			}
			this.configs = configs;
		}
	}

}
