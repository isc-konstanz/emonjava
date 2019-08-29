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
import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class RedisClient implements Emoncms {
    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

    private Jedis jedis;

    public RedisClient() {
    }

    @Override
    public EmoncmsType getType() {
        return EmoncmsType.REDIS;
    }

    @Override
    public boolean isClosed() {
        return jedis.isConnected();
    }

    @Override
    public void close() throws IOException {
    	jedis.close();
    }

    @Override
    public void open() throws EmoncmsUnavailableException {
        logger.info("Initializing emoncms Redis connection \"{}\"", "");

    	jedis = new Jedis();
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
    public void post(org.emoncms.data.DataList data) throws EmoncmsException {
        throw new UnsupportedOperationException("Unsupported for type "+getType());
    }

}
