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

import java.util.HashMap;
import java.util.Map;

import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.data.Value;

public class Configuration extends HashMap<String, Value> {
	private static final long serialVersionUID = -8674845221937360045L;

	protected Configuration() {
	}

	public Configuration(Map<String, String> map) {
    	for (Entry<String, String> entry : map.entrySet()) {
    		put(entry.getKey(), new StringValue(entry.getValue()));
    	}
    }

    public boolean contains(String key) {
    	return containsKey(key);
    }

	public Boolean getBoolean(String key) {
		if (contains(key)) {
			return get(key).asBoolean();
		}
		return null;
	}

	public boolean getBoolean(String key, boolean def) {
		try {
			if (contains(key)) {
				return get(key).asBoolean();
			}
		} catch (TypeConversionException e) {
		}
		return def;
	}

	public Byte getByte(String key) {
		if (contains(key)) {
			return get(key).asByte();
		}
		return null;
	}

	public byte getByte(String key, byte def) {
		try {
			if (contains(key)) {
				return get(key).asByte();
			}
		} catch (TypeConversionException e) {
		}
		return def;
	}

	public Short getShort(String key) {
		if (contains(key)) {
			return get(key).asShort();
		}
		return null;
	}

	public short getShort(String key, short def) {
		try {
			if (contains(key)) {
				return get(key).asShort();
			}
		} catch (TypeConversionException e) {
		}
		return def;
	}

	public Integer getInteger(String key) {
		if (contains(key)) {
			return get(key).asInt();
		}
		return null;
	}

	public int getInteger(String key, int def) {
		try {
			if (contains(key)) {
				return get(key).asInt();
			}
		} catch (TypeConversionException e) {
		}
		return def;
	}

	public Long getLong(String key) {
		if (contains(key)) {
			return get(key).asLong();
		}
		return null;
	}

	public long getLong(String key, long def) {
		try {
			if (contains(key)) {
				return get(key).asLong();
			}
		} catch (TypeConversionException e) {
		}
		return def;
	}

	public Float getFloat(String key) {
		if (contains(key)) {
			return get(key).asFloat();
		}
		return null;
	}

	public float getFloat(String key, float def) {
		try {
			if (contains(key)) {
				return get(key).asFloat();
			}
		} catch (TypeConversionException e) {
		}
		return def;
	}

	public Double getDouble(String key) {
		if (contains(key)) {
			return get(key).asDouble();
		}
		return null;
	}

	public double getDouble(String key, double def) {
		try {
			if (contains(key)) {
				return get(key).asDouble();
			}
		} catch (TypeConversionException e) {
		}
		return def;
	}

	public String getString(String key) {
		if (contains(key)) {
			return get(key).asString();
		}
		return null;
	}

	public String getString(String key, String def) {
		if (contains(key)) {
			return get(key).asString();
		}
		return def;
	}

}
