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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlClient implements Emoncms, SqlCallbacks {
    private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

    private final String driver;
    private final String url;
    private final String user;
    private final String password;

    private final RedisClient redis;

    private ComboPooledDataSource source = null;

    protected SqlClient(RedisClient redis, String driver, String url, String user, String password) {
        this.redis = redis;
        this.driver = driver;
        
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public RedisClient getCache() {
        return redis;
    }

    public boolean hasCache() {
    	return redis != null && !redis.isClosed();
    }

    @Override
    public EmoncmsType getType() {
        return EmoncmsType.SQL;
    }

    @Override
    public boolean isClosed() {
        return source == null;
    }

    @Override
    public void close() throws IOException {
        source.close();
        source = null;
    }

    @Override
    public void open() throws EmoncmsUnavailableException {
        logger.info("Initializing emoncms SQL connection \"{}\"", url);
        try {
            if (redis != null && 
        		redis.isClosed()) {
            	
            	redis.open();
            }
            if (source != null) {
                source.close();
            }
            source = new ComboPooledDataSource();
            source.setDriverClass(driver);
            source.setJdbcUrl(url);
            source.setUser(user);
            source.setPassword(password);
            
        } catch (PropertyVetoException e) {
            throw new EmoncmsUnavailableException(e);
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
    public void post(org.emoncms.data.DataList data) throws EmoncmsException {
        throw new UnsupportedOperationException("Unsupported for type "+getType());
    }

    @Override
    public Connection getConnection() throws SqlException {
        try {
            return source.getConnection(user, password);
            
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    @Override
    public org.emoncms.sql.Transaction getTransaction() throws SqlException {
        return new Transaction(getConnection());
    }

    public redis.clients.jedis.Transaction cacheTransaction() {
        if (redis == null) {
            return null;
        }
        return redis.getTransaction();
    }

//  private static final String INSERT = "INSERT INTO (?) (time, data) VALUES((?),(?))";
//
//  public void insert(Long time, List<Namevalue> namevalues) throws SQLException {
//      logger.debug("Inserting values for {} tables {}", namevalues.size(), namevalues.toString());
//      
//      Connection connection = source.getConnection(user, password);
//      PreparedStatement statement = null;
//      try {
//          statement = connection.prepareStatement(INSERT);
//          for (Namevalue  nameVal : namevalues) {
//              statement.setString(1, nameVal.getName());
//              statement.setLong(2, time);
//              statement.setDouble(3, nameVal.getValue());
//              statement.executeUpdate();
//          }
//      }
//      finally {
//          if (statement != null) try {
//              statement.close(); 
//          }
//          catch (SQLException ignore) {}
//      }
//  }
//
//  public void insert(DataList dataList) throws SQLException {
//      logger.debug("Inserting bulk of {} data sets", dataList.size());
//      
//      Connection connection = source.getConnection(user, password);
//      PreparedStatement statement = null;
//      try {
//          statement = connection.prepareStatement(INSERT);
//          Iterator<Data> it = dataList.iterator();
//          while (it.hasNext()) {
//              Data data = it.next();
//              List<Namevalue> namevals = data.getNamevalues();
//              for (Namevalue  nameVal : namevals) {
//                  statement.setString(1, nameVal.getName());
//                  statement.setLong(2, data.getTime());
//                  statement.setDouble(3, nameVal.getValue());
//                  statement.executeUpdate();
//              }
//          }
//      }
//      finally {
//          if (statement != null) try {
//              statement.close();
//          }
//          catch (SQLException ignore) {}
//          try {
//              connection.close();
//          }
//          catch (SQLException ignore) {}
//      }
//  }

}
