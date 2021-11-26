/* 
 * Copyright 2016-2021 ISC Konstanz
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
package org.emoncms.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Transaction implements AutoCloseable {

	private final Connection connection;

	public Transaction(Connection connection) throws SqlException {
		this.connection = connection;
		try {
			connection.setAutoCommit(false);
			
		} catch (SQLException e) {
			throw new SqlException(e);
		}
	}

	public boolean execute(String query) throws SqlException {
		try (Statement statement = connection.createStatement()) {
			return statement.execute(query);
			
		} catch (SQLException e) {
			throw new SqlException(e);
		}
	}

	public ResultSet query(String query) throws SqlException {
		try (Statement statement = connection.createStatement()) {
			return statement.executeQuery(query);
			
		} catch (SQLException e) {
			throw new SqlException(e);
		}
	}

	@Override
	public void close() throws Exception {
		connection.commit();
		connection.close();
	}

}
