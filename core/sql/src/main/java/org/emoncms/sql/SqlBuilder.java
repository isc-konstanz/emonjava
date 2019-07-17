package org.emoncms.sql;

import java.util.ArrayList;
import java.util.List;

import org.emoncms.Emoncms;

public class SqlBuilder {

	private static final List<SqlClient> sqlSingletons = new ArrayList<SqlClient>();

	protected String id = null;
	protected String connectionDriverClass = "com.mysql.jdbc.Driver";
	protected String connectionUrl = "jdbc:mysql://127.0.0.1:3306/openmuc?useSSL=false";

	protected String user = null;
	protected String password = null;

	private SqlBuilder() {
	}

	private SqlBuilder(String connectionUrl) {
		if (!connectionUrl.startsWith("jdbc:mysql://")) {
			connectionUrl = "jdbc:mysql://".concat(connectionUrl);
		}
		this.connectionUrl = connectionUrl;
	}

    public static SqlBuilder create() {
        return new SqlBuilder();
    }

    public static SqlBuilder create(String address) {
        return new SqlBuilder(address);
    }

	public SqlBuilder setConnectionDriverClass(String connectionDriverClass) {
		this.connectionDriverClass = connectionDriverClass;
		return this;
	}

	public SqlBuilder setCredentials(String user, String password) {
		this.user = user;
		this.password = password;
		return this;
	}

	public Emoncms build() {
		SqlClient sqlClient = null;
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
