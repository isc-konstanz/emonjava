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

import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsType;
import org.emoncms.data.Authentication;
import org.emoncms.data.Authorization;
import org.openmuc.framework.datalogger.dynamic.LogChannel;

public class Channel extends LogChannel {

	private final static String TYPE = "type";

	private final static String NODE_ID = "nodeid";
	private final static String FEED_ID = "feedid";

	private final static String API_KEY = "apikey";
	private final static String API_AUTH = "authorization";

	public Channel(LogChannel channel) {
		super(channel, channel.getSettings());
		this.record = channel.getRecord();
	}

	public EmoncmsType getType() {
		if (settings.contains(TYPE)) {
			return EmoncmsType.valueOf(settings.get(TYPE).asString());
		}
		return EmoncmsType.DEFAULT;
	}

	public boolean hasNode() {
		return settings.contains(NODE_ID);
	}

	public String getNode() {
		if (settings.contains(NODE_ID)) {
			return settings.get(NODE_ID).asString();
		}
		return null;
	}

	public boolean hasFeedId() {
		return settings.contains(FEED_ID);
	}

	public Integer getFeedId() {
		if (settings.contains(FEED_ID)) {
			return settings.get(FEED_ID).asInt();
		}
		return null;
	}

	public boolean hasApiKey() {
		return settings.contains(API_KEY);
	}

	public String getApiKey() {
		if (settings.contains(API_KEY)) {
			return settings.get(API_KEY).asString();
		}
		return null;
	}

    public Authentication getAuthenticator() throws EmoncmsSyntaxException {
    	Authorization authorization = getAuthorization();
    	switch(authorization) {
    	case NONE:
    		throw new EmoncmsSyntaxException("Emoncms authorization unconfigured");
    	case DEFAULT:
    		return new Authentication(authorization, null);
    	default:
    		return new Authentication(authorization, getApiKey());
    	}
    }

	public Authorization getAuthorization() {
		if (settings.contains(API_AUTH)) {
			return Authorization.valueOf(settings.get(API_AUTH).asString());
		}
		return Authorization.DEFAULT;
	}

}
