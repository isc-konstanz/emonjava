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

import org.emoncms.Emoncms;

/**
 * Builds and opens a {@link RedisClient} instance as an {@link Emoncms} implementation.
 * 
 */
public class RedisBuilder {

    private String host = "localhost";
    private int port = 6379;

    private String password = null;
    private String prefix = "emoncms";

    private RedisBuilder() {
    }

    private RedisBuilder(String host) {
        this.host = host;
    }

    public static RedisBuilder create() {
        return new RedisBuilder();
    }

    public static RedisBuilder create(String address) {
        return new RedisBuilder(address);
    }

    public RedisBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public RedisBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public RedisBuilder setAuthentication(String password) {
        this.password = password;
        return this;
    }

    public RedisBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Emoncms build() {
        return new RedisClient(host, port, password, prefix);
    }

}
