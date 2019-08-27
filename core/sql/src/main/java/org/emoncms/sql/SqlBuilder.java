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

import java.util.ArrayList;
import java.util.List;

import org.emoncms.Emoncms;

public class SqlBuilder {

	private static final List<SqlClient> sqlSingletons = new ArrayList<SqlClient>();

	protected String connectionDriverClass = "com.mysql.jdbc.Driver";
	protected String address = "127.0.0.1";
	protected int port = 3306;
	protected String databaseName = "openmuc"; 
	protected String databaseType = "jdbc:mysql";
	protected String databaseDialect = "org.hibernate.dialect.MariaDBDialect";
	protected String connectionUrl;

	protected String user = null;
	protected String password = null;

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

	public SqlBuilder setDatabaseDialect(String databaseDialect) {
		this.databaseDialect = databaseDialect;
		return this;
	}

	public SqlBuilder setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}

	public SqlBuilder setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
		return this;
	}

	public SqlBuilder setConnectionDriverClass(String connectionDriverClass) {
		this.connectionDriverClass = connectionDriverClass;
		return this;
	}

	public SqlBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public SqlBuilder setCredentials(String user, String password) {
		this.user = user;
		this.password = password;
		return this;
	}

	public Emoncms build() {
		SqlClient sqlClient = null;
		if (!databaseType.endsWith(":")) databaseType += ":";
		
		connectionUrl = databaseType + "//" + address + ":" + port + "/" + 
					databaseName + "?useSSL=false";

		for (SqlClient emoncms : sqlSingletons) {
					
			if (emoncms.getConnectionUrl().equals(connectionUrl)) {
				sqlClient = emoncms;
				break;
			}
		}
		if (sqlClient == null) {
			sqlClient = new SqlClient(this);
			sqlSingletons.add(sqlClient);
		}
		return sqlClient;
	}

}
