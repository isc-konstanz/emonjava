/* 
 * Copyright 2016-19 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
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
 */
package org.emoncms.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.Input;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Transaction;

public class RedisInput implements Input {
	private static final Logger logger = LoggerFactory.getLogger(RedisInput.class);

    protected static final String INPUT_PREFIX = "input:";

    protected final RedisCallbacks callbacks;

    protected final Integer id;

    public static RedisInput connect(RedisCallbacks redis, Integer id) 
            throws EmoncmsException {
        if (id != null && id < 1) {
            throw new EmoncmsException("Invalid input id: "+id);
        }
        return new RedisInput(redis, id);
    }

    protected RedisInput(RedisCallbacks callbacks, Integer id) throws EmoncmsException {
        this.callbacks = callbacks;
        this.id = id;
    }

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.REDIS;
    }

	@Override
	public String getNode() throws EmoncmsException {
		return getField("nodeid");
	}

	@Override
	public String getName() throws EmoncmsException {
		return getField("name");
	}

	protected String getField(String field) throws EmoncmsException {
        String key = parseKey();
        logger.debug("Retrieving cached {} for {}", field, key);
        if (!callbacks.exists(key, field)) {
            throw new RedisException("Nothing cached yet for id:"+id);
        }
        return callbacks.get(key, field);
	}

	@Override
	public void post(Timevalue timevalue) throws EmoncmsException {
		cache(timevalue.getTime(), timevalue.getValue());
	}

	@Override
	public void post(List<Timevalue> timevalues) throws EmoncmsException {
		Timevalue timevalue = timevalues.get(timevalues.size()-1);
		cache(timevalue.getTime(), timevalue.getValue());
	}

    public void cache(long timestamp, double value) throws EmoncmsException {
        String key = parseKey("lastvalue:");
        logger.debug("Caching value {}:{}", key, value);
        
        if (!callbacks.exists(key, "time")) {
            throw new RedisException("No value cached yet for id:"+id);
        }
        Map<String, String> values = new HashMap<String, String>();
    	values.put("time", String.valueOf((int) Math.round((double) timestamp/1000.0)));
    	values.put("value", String.valueOf(value));
    	
        callbacks.set(key, values);
    }

    public void cache(Transaction transaction, long timestamp, double value) throws EmoncmsException {
        String key = parseKey("lastvalue:");
        logger.debug("Caching value {}:{}", key, value);
        
        Map<String, String> values = new HashMap<String, String>();
    	values.put("time", String.valueOf((int) Math.round((double) timestamp/1000.0)));
    	values.put("value", String.valueOf(value));
    	
    	callbacks.set(transaction, key, values);
    }

    private String parseKey(String path) throws RedisUnavailableException {
        if (callbacks == null) {
            throw new RedisUnavailableException();
        }
        if (id == null) {
            throw new RedisUnavailableException("No inputid configured");
        }
        return INPUT_PREFIX+path+String.valueOf(id);
    }

    private String parseKey() throws RedisUnavailableException {
    	return parseKey("");
    }

}
