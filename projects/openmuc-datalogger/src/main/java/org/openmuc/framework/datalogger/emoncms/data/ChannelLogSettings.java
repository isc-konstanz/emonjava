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
package org.openmuc.framework.datalogger.emoncms.data;

import java.util.HashMap;
import java.util.Map;

import org.emoncms.com.EmoncmsSyntaxException;
import org.emoncms.data.Authentication;
import org.emoncms.data.Authorization;

public class ChannelLogSettings {

	private final static String MAX_INTERVAL = "loggingMaxInterval";
	private final static String TOLERANCE = "loggingTolerance";
	private final static String AVERAGE = "average";
	private final static String NODE_ID = "nodeid";
	private final static String FEED_ID = "feedid";
	private final static String INTERVAL = "interval";
	private final static String AUTHORIZATION = "authorization";
	private final static String KEY = "key";

    private final Map<String, String> settingsMap = new HashMap<>();

    public ChannelLogSettings(String settings) {
    	if (settings != null) {
            String[] settingsArray = settings.split(",");
            for (String arg : settingsArray) {
                int p = arg.indexOf(":");
                if (p != -1) {
                    settingsMap.put(arg.substring(0, p).trim(), arg.substring(p + 1).trim());
                }
            }
    	}
    }

    public boolean isDynamic() {
        if (settingsMap.containsKey(MAX_INTERVAL)) {
        	return true;
        }
    	return false;
    }

    public Integer getMaxInterval() {
        if (settingsMap.containsKey(MAX_INTERVAL)) {
        	return Integer.parseInt(settingsMap.get(MAX_INTERVAL).trim());
        }
    	return null;
    }

    public double getTolerance() {
        if (settingsMap.containsKey(TOLERANCE)) {
        	return Double.parseDouble(settingsMap.get(TOLERANCE).trim());
        }
    	return 0;
    }

    public boolean isAveraging() {
        if (settingsMap.containsKey(AVERAGE)) {
        	return settingsMap.get(AVERAGE).toLowerCase().trim().equals("true");
        }
    	return false;
    }

    public String getNode() {
        if (settingsMap.containsKey(NODE_ID)) {
            String node = settingsMap.get(NODE_ID).trim();
            if (!node.isEmpty()) {
            	return node;
            }
        }
        return null;
    }

    public boolean hasFeedId() {
    	return settingsMap.containsKey(FEED_ID);
    }

    public Integer getFeedId() {
        if (settingsMap.containsKey(FEED_ID)) {
            return Integer.parseInt(settingsMap.get(FEED_ID).trim());
        }
        return null;
    }

    public Integer getInterval() {
        if (settingsMap.containsKey(INTERVAL)) {
        	return Integer.parseInt(settingsMap.get(INTERVAL).trim());
        }
    	return null;
    }

    public Authorization getAuthorization() {
        if (settingsMap.containsKey(AUTHORIZATION)) {
        	String authorization = settingsMap.get(AUTHORIZATION).trim();
        	if (!authorization.isEmpty()) {
        		return Authorization.valueOf(authorization);
        	}
        }
        return Authorization.DEFAULT;
    }

    public boolean hasAuthorization() {
        if (getAuthorization() != Authorization.NONE) {
        	return true;
        }
        return false;
    }

    public String getKey() {
        if (settingsMap.containsKey(KEY)) {
        	String key = settingsMap.get(KEY).trim();
        	if (!key.isEmpty()) {
        		return key;
        	}
        }
        return null;
    }

    public Authentication getAuthentication() throws EmoncmsSyntaxException {
    	Authorization authorization = getAuthorization();
    	switch(authorization) {
    	case NONE:
    		throw new EmoncmsSyntaxException("Emoncms authorization unconfigured");
    	case DEFAULT:
    		return new Authentication(authorization, null);
    	default:
    		return new Authentication(authorization, getKey());
    	}
    }

    public boolean isValid() {
        if (getNode() != null && getAuthorization() != null) {
        	switch(getAuthorization()) {
        	case DEFAULT:
        		return true;
        	case DEVICE:
        	case WRITE:
        	case READ:
        		if (getKey() != null) {
            		return true;
        		}
        	default:
        		return false;
        	}
        }
        return false;
    }
}
