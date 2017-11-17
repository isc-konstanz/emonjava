/*
 * Copyright 2016-17 ISC Konstanz
 *
 * This file is part of emonjava.
 * For more information visit https://bitbucket.org/isc-konstanz/emonjava
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
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.datalogger.emoncms;

import java.util.HashMap;
import java.util.Map;

import org.emoncms.com.EmoncmsSyntaxException;
import org.emoncms.data.Authentication;
import org.emoncms.data.Authorization;

public class SettingsHelper {

	private final static String NODE_ID = "nodeid";
	private final static String INPUT_ID = "inputid";
	private final static String FEED_ID = "feedid";
	private final static String AUTHORIZATION = "authorization";
	private final static String KEY = "key";

    private final Map<String, String> settingsMap = new HashMap<>();

    public SettingsHelper(String settings) {
    	if (settings != null) {
            String[] settingsArray = settings.split(",");
            for (String arg : settingsArray) {
                int p = arg.indexOf(":");
                if (p != -1) {
                    settingsMap.put(arg.substring(0, p).toLowerCase().trim(), arg.substring(p + 1).trim());
                }
            }
    	}
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

    public Integer getInputId() {
        if (settingsMap.containsKey(INPUT_ID)) {
            return Integer.parseInt(settingsMap.get(INPUT_ID).trim());
        }
        return null;
    }

    public Integer getFeedId() {
        if (settingsMap.containsKey(FEED_ID)) {
            return Integer.parseInt(settingsMap.get(FEED_ID).trim());
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
