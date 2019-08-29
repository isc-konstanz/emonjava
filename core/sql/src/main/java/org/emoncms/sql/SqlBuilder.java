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
package org.emoncms.sql;

import org.emoncms.Emoncms;

/**
 * Builds and opens a {@link SqlClient} instance as an {@link Emoncms} implementation.
 * 
 */
public class SqlBuilder {

	private String address = "127.0.0.1";
	private int port = 3306;

	private String driver = "com.mysql.cj.jdbc.Driver";
	private String type = "jdbc:mysql";
	private String name = "emoncms";

	private String user = "root";
	private String password = "";

	private SqlBuilder() {
	}

	private SqlBuilder(String address) {
		this.address = address;
	}

    public static SqlBuilder create() {
        return new SqlBuilder();
    }

    public static SqlBuilder create(String address) {
        return new SqlBuilder(address);
    }

	public SqlBuilder setAddress(String address) {
		this.address = address;
		return this;
	}

	public SqlBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public SqlBuilder setDriver(String driver) {
		this.driver = driver;
		return this;
	}

	public SqlBuilder setDatabaseType(String type) {
		this.type = type;
		return this;
	}

	public SqlBuilder setDatabaseName(String name) {
		this.name = name;
		return this;
	}

	public SqlBuilder setCredentials(String user, String password) {
		this.user = user;
		this.password = password;
		return this;
	}

	public Emoncms build() {
		return new SqlClient(driver, type+"://"+address+":"+port+"/"+name+"?autoReconnect=true&useSSL=false", user, password);
	}

}
