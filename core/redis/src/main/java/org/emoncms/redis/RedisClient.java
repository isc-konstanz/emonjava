/* 
 * Copyright 2016-18 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit mqtts://github.com/isc-konstanz/emonjava
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
 * along with emonjava.  If not, see <mqtt://www.gnu.org/licenses/>.
 */
package org.emoncms.redis;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoncms.Emoncms;
import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

public class RedisClient implements Emoncms, RedisCallbacks {
    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

    protected final String host;
    protected final int port;

    protected final String password;
    protected final String prefix;

    protected Jedis jedis;

    protected RedisClient(String host, int port, String password, String prefix) {
    	this.host = host;
    	this.port = port;
    	this.password = password;
    	this.prefix = prefix;
    }

	public String getPrefix() {
		return prefix;
	}

    @Override
    public EmoncmsType getType() {
        return EmoncmsType.REDIS;
    }

    @Override
    public boolean isClosed() {
    	if (jedis == null) {
        	return true;
    	}
        return !jedis.isConnected();
    }

    @Override
    public void close() throws IOException {
    	jedis.close();
    }

    @Override
    public void open() throws EmoncmsUnavailableException {
        logger.info("Initializing emoncms Redis connection \"{}:{}\"", host, port);

    	jedis = new Jedis(host, port);
    	if (password != null) {
    		jedis.auth(password);
    	}
    }

    @Override
    public void post(String node, String name, Timevalue timevalue) throws EmoncmsException {
        throw new UnsupportedOperationException("Unsupported for type "+getType());
    }

    @Override
    public void post(String node, Long time, List<Namevalue> namevalues) throws EmoncmsException {
        throw new UnsupportedOperationException("Unsupported for type "+getType());
    }

    @Override
    public void post(DataList data) throws EmoncmsException {
        throw new UnsupportedOperationException("Unsupported for type "+getType());
    }

	@Override
	public Transaction getTransaction() {
		return jedis.multi();
	}

	@Override
    public boolean exists(String key, String field) throws RedisException {
		try {
	    	return jedis.hexists(prefix+key, field);
			
		} catch(JedisException e) {
			throw new RedisException(e);
		}
    }

	@Override
	public String get(String key, String field) throws RedisException {
		try {
			return jedis.hget(prefix+key, field);
			
		} catch(JedisException e) {
			throw new RedisException(e);
		}
	}

	@Override
	public Map<String, String> get(String key, String... fields) throws RedisException {
		try {
			Map<String, String> result = new HashMap<String, String>();
	    	List<String> values = jedis.hmget(prefix+key, fields);
	    	for (int i=0; i<fields.length; i++) {
	    		result.put(fields[i], values.get(i));
	    	}
	    	return result;
			
		} catch(JedisException e) {
			throw new RedisException(e);
		}
	}

	@Override
	public void set(String key, Map<String, String> values) throws RedisException {
		try {
			jedis.hmset(prefix+key, values);
			
		} catch(JedisException e) {
			throw new RedisException(e);
		}
	}

	@Override
	public void set(Transaction transaction, String key, Map<String, String> values) throws RedisException {
        if (transaction == null) {
            throw new RedisUnavailableException();
        }
    	transaction.hmset(prefix+key, values);
	}

}
