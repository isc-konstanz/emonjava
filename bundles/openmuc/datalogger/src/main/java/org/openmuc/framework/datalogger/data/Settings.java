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
package org.openmuc.framework.datalogger.data;

import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.datalogger.spi.LogChannel;

public class Settings extends Configuration {
    private static final long serialVersionUID = -1562428342279468583L;

    private final static String LOGGER = "logger";
    private final static String OFFSET = "intervalOffset";
    private final static String INTERVAL = "interval";
    private final static String INTERVAL_MAX = "intervalMax";
    private final static String TOLERANCE = "tolerance";
    private final static String AVERAGE = "average";

    public Settings(LogChannel channel) {
        if (channel.getLoggingInterval() != null) {
            put(INTERVAL, new IntValue(channel.getLoggingInterval()));
        }
        if (channel.getLoggingTimeOffset() != null) {
            put(OFFSET, new IntValue(channel.getLoggingTimeOffset()));
        }
        if (channel.getLoggingSettings() != null) {
            parse(channel.getLoggingSettings());
        }
    }

    protected void parse(String settings) {
        String[] arr = settings.trim().split(",");
        for (String arg : arr) {
            int p = arg.indexOf(":");
            if (p != -1) {
                String k = arg.substring(0, p).trim();
                String v = arg.substring(p + 1).trim();
                if (!k.isEmpty() && !v.isEmpty()) {
                    put(k, new StringValue(v));
                }
            }
        }
    }

    public String getLogger() {
        return getString(LOGGER);
    }

    public Integer getIntervalOffset() {
        return getInteger(OFFSET);
    }

    public Integer geInterval() {
        return getInteger(INTERVAL);
    }

    public Integer getIntervalMax() {
        if (contains(INTERVAL_MAX)) {
            return getInteger(INTERVAL_MAX);
        }
    	return getInteger("loggingMaxInterval");
    }

    public double getTolerance() {
        if (contains(TOLERANCE)) {
            return getInteger(TOLERANCE, 0);
        }
    	return getInteger("loggingTolerance", 0);
    }

    public boolean isDynamic() {
        return contains(INTERVAL_MAX);
    }

    public boolean isAveraging() {
        return getBoolean(AVERAGE, false);
    }

    @Override
    public String toString() {
        int i = 0;
        String[] list = new String[size()];
        for (Entry<String, Value> entry : entrySet()) {
            list[i] = entry.getKey()+":"+entry.getValue().asString();
            i++;
        }
        return String.join(",", list);
    }

}
