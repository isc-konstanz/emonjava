package org.emoncms.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.data.Timevalue;
import org.emoncms.redis.RedisClient;
import org.emoncms.redis.RedisFeed;
import org.emoncms.redis.RedisUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlFeed extends RedisFeed {
    private static final Logger logger = LoggerFactory.getLogger(SqlFeed.class);

    public static int TYPE_LENGTH_DEFAULT = 10;
    public static String TYPE_DEFAULT = "FLOAT";
    public static String TYPE_NOT_NULL = " NOT NULL";
    public static String[] TYPES = new String[] {
            "FLOAT",
            "REAL",
            "BIGINT",
            "INT",
            "SMALLINT",
            "TINYINT",
            "BIT",
            "VARBINARY",
            "VARCHAR"
    };

    private static String QUERY_CREATE = "CREATE TABLE IF NOT EXISTS %s ("
            + "time INT UNSIGNED NOT NULL, "
            + "data %s, "
            + "PRIMARY KEY (time)"
            + ") ENGINE=MYISAM";
    private static String QUERY_SELECT = "SELECT * FROM %s WHERE time >= %s AND time <= %s";
    private static String QUERY_INSERT = "INSERT INTO %s (time,data) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE data=VALUES(data)";

    private static String COLUMN_TIME = "time";
    private static String COLUMN_DATA = "data";

    protected String table;
    protected Integer id;

    /**
     * The Feeds' current callback object, which is notified of query events
     */
    private final SqlCallbacks callbacks;

    public static SqlFeed create(SqlCallbacks callbacks, RedisClient redis, Transaction transaction, Integer id, 
            String table, String type, boolean empty) throws EmoncmsException {
        
        SqlFeed feed = connect(callbacks, redis, id, table);
        if (type == null) {
            type = TYPE_DEFAULT;
        }
        else if (!Arrays.asList(TYPES).contains(type) && 
                !type.startsWith("VARCHAR(") && !type.startsWith("VARBINARY(")) {
            throw new EmoncmsException("Value type not allowed: "+type);
        }
        if (!empty) {
            type += TYPE_NOT_NULL;
        }
        String query = String.format(QUERY_CREATE, table, type);
        logger.debug("Query  {}", query);
        
        transaction.execute(query);
        return feed;
    }

    public static SqlFeed create(SqlCallbacks callbacks, RedisClient redis, Integer id, 
            String table, String type, boolean empty) throws EmoncmsException {
        
        try (Transaction transaction = callbacks.getTransaction()) {
            return create(callbacks, redis, transaction, id, table, type, false);
            
        } catch (Exception e) {
            throw new SqlException(e);
        }
    }

    public static SqlFeed create(SqlCallbacks callbacks, RedisClient redis, Integer id, 
            String table, String type) throws EmoncmsException {
        return create(callbacks, redis, id, table, type, false);
    }

    public static SqlFeed create(SqlCallbacks callbacks, RedisClient redis, Integer id) 
            throws EmoncmsException {
        return create(callbacks, redis, id, null, null, false);
    }

    public static SqlFeed connect(SqlCallbacks callbacks, RedisClient redis, Integer id, 
            String table) throws EmoncmsException {
        
        if (callbacks == null) {
            throw new EmoncmsUnavailableException("MySQL connection to emoncms database invalid");
        }
        if (id != null && id < 1) {
            throw new EmoncmsException("Invalid feed id: "+id);
        }
        return new SqlFeed(callbacks, redis, id, table);
    }

    protected SqlFeed(SqlCallbacks callbacks, RedisClient redis, Integer id, String table) throws EmoncmsException {
        super(redis, id);
        this.table = table;
        this.callbacks = callbacks;
    }

    @Override
    public EmoncmsType getType() {
        return EmoncmsType.SQL;
    }

    @Override
    public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
        String query = String.format(QUERY_SELECT, table, start, end);
        logger.debug("Query {}", query);
        
        LinkedList<Timevalue> timevalues = new LinkedList<Timevalue>();
        try (Connection connection = callbacks.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet result = statement.executeQuery(query)) {
                    while (result.next()) {
                        long time = result.getLong(COLUMN_TIME)*1000;
                        double value = result.getInt(COLUMN_DATA);
                        
                        timevalues.add(new Timevalue(time, value));
                    }
                }
            }
        } catch (SQLException e) {
            throw new SqlException(e);
        }
        return timevalues;
    }

    @Override
    public void insertData(Timevalue timevalue) throws EmoncmsException {
        try (Transaction transaction = callbacks.getTransaction()) {
            insertData(transaction, timevalue.getTime(), timevalue.getValue());
            
        } catch (Exception e) {
            throw new SqlException(e);
        }
        try {
            super.insertData(timevalue);
        }
        catch (RedisUnavailableException ignore) {}
    }

    public void insertData(Transaction transaction, long timestamp, double data) throws EmoncmsException {
        int time = (int) Math.round((double) timestamp/1000.0);
        String query = String.format(QUERY_INSERT, table, time, data);
        logger.debug("Query {}", query);
        
        transaction.execute(query);
    }

}
