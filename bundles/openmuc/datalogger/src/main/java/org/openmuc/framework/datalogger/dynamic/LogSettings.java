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
package org.openmuc.framework.datalogger.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;

public class LogSettings {

	private final static String AVERAGE = "average";
	private final static String TOLERANCE = "loggingTolerance";
	private final static String INTERVAL_MAX = "loggingMaxInterval";
	private final static String INTERVAL_OFFSET = "loggingTimeOffset";
	private final static String INTERVAL = "loggingInterval";

	private final Map<String, Value> settings = new HashMap<String, Value>();

    public LogSettings(LogChannel channel) {
    	if (channel.getLoggingInterval() != null) {
    		settings.put(INTERVAL, new IntValue(channel.getLoggingInterval()));
    	}
    	if (channel.getLoggingTimeOffset() != null) {
    		settings.put(INTERVAL_OFFSET, new IntValue(channel.getLoggingTimeOffset()));
    	}
    	if (channel.getLoggingSettings() != null) {
            String[] settingsArray = channel.getLoggingSettings().trim().split(",");
            for (String arg : settingsArray) {
                int p = arg.indexOf(":");
                if (p != -1) {
                	String k = arg.substring(0, p).trim();
                	String v = arg.substring(p + 1).trim();
                	if (!k.isEmpty() && !v.isEmpty()) {
                        settings.put(k, new StringValue(v));
                	}
                }
            }
    	}
    }

    public boolean contains(String key) {
    	return settings.containsKey(key);
    }

    public Value get(String key) {
    	return settings.get(key);
    }

    public boolean isAveraging() {
        if (contains(AVERAGE)) {
        	return get(AVERAGE).asBoolean();
        }
    	return false;
    }

    public boolean isDynamic() {
        if (contains(INTERVAL_MAX)) {
        	return true;
        }
    	return false;
    }

    public double getTolerance() {
        if (contains(TOLERANCE)) {
        	return get(TOLERANCE).asDouble();
        }
    	return 0;
    }

    public Integer getMaxInterval() {
        if (contains(INTERVAL_MAX)) {
        	return get(INTERVAL_MAX).asInt();
        }
    	return null;
    }

    public Integer getIntervalOffset() {
        if (contains(INTERVAL_OFFSET)) {
        	return get(INTERVAL_OFFSET).asInt();
        }
    	return null;
    }

    public Integer geInterval() {
        if (contains(INTERVAL)) {
        	return get(INTERVAL).asInt();
        }
    	return null;
    }

    @Override
    public String toString() {
    	int i = 0;
    	String[] list = new String[settings.size()];
    	for (Entry<String, Value> entry : settings.entrySet()) {
    		list[i] = entry.getKey()+":"+entry.getValue().asString();
    		i++;
    	}
    	return String.join(",", list);
    }

}
