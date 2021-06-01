/* 
 * Copyright 2016-21 ISC Konstanz
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
package org.emoncms.sql;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.redis.RedisClient;
import org.emoncms.redis.RedisInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlInput extends RedisInput {
	private static final Logger logger = LoggerFactory.getLogger(SqlInput.class);

    /**
     * The Feeds' current callback object, which is notified of query events
     */
    private final SqlCallbacks callbacks;

    public static SqlInput connect(SqlCallbacks callbacks, RedisClient redis, Integer id)
    		throws EmoncmsException {
        
        if (callbacks == null) {
            throw new EmoncmsUnavailableException("MySQL connection to emoncms database invalid");
        }
        if (id != null && id < 1) {
            throw new EmoncmsException("Invalid input id: "+id);
        }
        return new SqlInput(callbacks, redis, id);
    }

	protected SqlInput(SqlCallbacks callbacks, RedisClient redis, Integer id) throws EmoncmsException {
		super(redis, id);
		this.callbacks = callbacks;
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.REDIS;
    }

}
