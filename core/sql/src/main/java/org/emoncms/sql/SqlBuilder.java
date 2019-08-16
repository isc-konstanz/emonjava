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
	protected String databaseDialect = "org.hibernate.dialect.MySQL5Dialect";
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
