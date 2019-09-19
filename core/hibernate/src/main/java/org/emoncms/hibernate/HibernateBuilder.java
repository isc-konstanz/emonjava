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
package org.emoncms.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.emoncms.Emoncms;

public class HibernateBuilder {

	private static final List<HibernateClient> clients = new ArrayList<HibernateClient>();

	protected String driverClass = "com.mysql.cj.jdbc.Driver";
	protected String address = "127.0.0.1";
	protected int port = 3306;

	protected String databaseUrl;
	protected String databaseName = "openmuc"; 
	protected String databaseType = "jdbc:mysql";
	protected String databaseDialect = "org.hibernate.dialect.MariaDBDialect";

	protected String user = null;
	protected String password = null;

	private HibernateBuilder() {
	}

	private HibernateBuilder(String address) {
		this.address = address;
	}

    public static HibernateBuilder create() {
        return new HibernateBuilder();
    }

    public static HibernateBuilder create(String address) {
        return new HibernateBuilder(address);
    }

	public HibernateBuilder setDriverClass(String connectionDriverClass) {
		this.driverClass = connectionDriverClass;
		return this;
	}

	public HibernateBuilder setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}

	public HibernateBuilder setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
		return this;
	}

	public HibernateBuilder setDatabaseDialect(String databaseDialect) {
		this.databaseDialect = databaseDialect;
		return this;
	}

	public HibernateBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public HibernateBuilder setCredentials(String user, String password) {
		this.user = user;
		this.password = password;
		return this;
	}

	public Emoncms build() {
		if (!databaseType.endsWith(":")) databaseType += ":";
		
		databaseUrl = databaseType + "//" + address + ":" + port + "/" + databaseName + "?useSSL=false"; //&useLegacyDatetimeCode=false&serverTimezone=UTC";
		for (HibernateClient client : clients) {
			if (client.getAddress().equals(databaseUrl)) {
				return client;
			}
		}
		HibernateClient client = new HibernateClient(this);
		clients.add(client);
		return client;
	}

}
