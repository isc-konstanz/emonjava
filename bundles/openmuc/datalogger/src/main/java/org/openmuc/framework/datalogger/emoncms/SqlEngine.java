package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsType;
import org.emoncms.data.Timevalue;
import org.emoncms.redis.RedisBuilder;
import org.emoncms.redis.RedisClient;
import org.emoncms.redis.RedisUnavailableException;
import org.emoncms.sql.SqlBuilder;
import org.emoncms.sql.SqlClient;
import org.emoncms.sql.SqlException;
import org.emoncms.sql.SqlFeed;
import org.emoncms.sql.Transaction;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;
import org.openmuc.framework.datalogger.data.Settings;
import org.openmuc.framework.datalogger.engine.DataLoggerEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlEngine implements DataLoggerEngine {
    private final static Logger logger = LoggerFactory.getLogger(SqlEngine.class);

    protected final static String FEED_ID = "feedid";

    protected final static String DRIVER = "driver";
    protected final static String TYPE = "type";
    protected final static String HOST = "address";
    protected final static String HOST_DEFAULT = "127.0.0.1";
    protected final static String PORT = "port";

    protected final static String DATABASE_NAME = "database";
    protected final static String DATABASE_USER = "user";
    protected final static String DATABASE_PASSWORD = "password";

    protected final static String REDIS_ENABLED = "redis.enabled";
    protected final static String REDIS_HOST = "redis.host";
    protected final static String REDIS_PORT = "redis.port";
    protected final static String REDIS_AUTH = "redis.auth";
    protected final static String REDIS_PREFIX = "redis.prefix";

    protected final static String GENERIC = "generic";
    protected final static String PREFIX = "prefix";

    private boolean isGeneric = true;
    private String prefix = "feed_";

    private SqlClient client;

    private final Map<String, SqlFeed> feeds = new HashMap<String, SqlFeed>();

    @Override
    public String getId() {
        return EmoncmsType.SQL.name();
    }

    @Override
    public boolean isActive() {
        return client != null && !client.isClosed();
    }

    @Override
    public void onActivate(Configuration config) throws IOException {
        logger.info("Activating Emoncms SQL Logger");
        
        if (config.contains(GENERIC)) {
            isGeneric = config.getBoolean(GENERIC);
        }
        if (config.contains(PREFIX)) {
            prefix = config.getString(PREFIX);
            if (prefix.equals("False") || prefix.equals("false")) {
                prefix = "";
            }
        }
        
        String address = config.getString(HOST, HOST_DEFAULT);
        SqlBuilder builder = SqlBuilder.create(address);
        if (config.contains(PORT)) {
            builder.setPort(config.getInteger(PORT));
        }
        if (config.contains(DRIVER)) {
            builder.setDriver(config.getString(DRIVER));
        }
        if (config.contains(TYPE)) {
            builder.setDatabaseType(config.getString(TYPE));
        }
        if (config.contains(DATABASE_NAME)) {
            builder.setDatabaseName(config.getString(DATABASE_NAME));
        }
        if (config.contains(DATABASE_USER) && config.contains(DATABASE_PASSWORD)) {
            builder.setCredentials(
                    config.getString(DATABASE_USER), 
                    config.getString(DATABASE_PASSWORD));
        }
        if (config.getBoolean(REDIS_ENABLED, false)) {
        	RedisBuilder redis = RedisBuilder.create();
            if (config.contains(REDIS_HOST)) {
                redis.setHost(config.getString(REDIS_HOST));
            }
            if (config.contains(REDIS_PORT)) {
                redis.setPort(config.getInteger(REDIS_PORT));
            }
            if (config.contains(REDIS_AUTH)) {
                redis.setAuthentication(config.getString(REDIS_AUTH));
            }
            if (config.contains(REDIS_PREFIX)) {
                redis.setPrefix(config.getString(REDIS_PREFIX));
            }
            builder.setCache((RedisClient) redis.build());
        }
        client = (SqlClient) builder.build();
        client.open();
    }

    @Override
    public void onConfigure(List<Channel> channels) throws IOException {
        feeds.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Transaction transaction = client.getTransaction()) {
                    for (Channel channel : channels) {
                        Settings settings = channel.getSettings();
                        try {
                            String channelId = channel.getId();
                            Integer feedId = settings.getInteger(FEED_ID);
                            
                            String valueType;
                            switch (channel.getValueType()) {
                            case STRING:
                                Integer maxStrLength =  channel.getValueTypeLength();
                                if (maxStrLength == null) {
                                    maxStrLength = SqlFeed.TYPE_LENGTH_DEFAULT;
                                }
                                valueType = "VARCHAR(" + maxStrLength + ")";
                            case BYTE_ARRAY:
                                Integer maxBytesLength = channel.getValueTypeLength();
                                if (maxBytesLength == null) {
                                    maxBytesLength = SqlFeed.TYPE_LENGTH_DEFAULT;
                                }
                                valueType = "VARBINARY(" + maxBytesLength + ")";
                            case BYTE:
                                valueType = "TINYINT";
                            case BOOLEAN:
                                valueType = "BIT";
                            case SHORT:
                                valueType = "SMALLINT";
                            case LONG:
                                valueType = "BIGINT";
                            case INTEGER:
                                valueType = "INT";
                            case FLOAT:
                                valueType = "REAL";
                            default:
                                valueType = "FLOAT";
                            }
                            String tableName = parseTable(feedId, channelId);
                            SqlFeed feed = SqlFeed.create(client, client.getCache(), transaction, 
                                    feedId, tableName, valueType, false);
                            
                            feeds.put(channelId, feed);
                            
                        } catch (EmoncmsSyntaxException e) {
                            logger.warn("Error preparing record to be logged to Channel \"{}\": {}", 
                                    channel.getId(), e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error while configuring SQL channels: {}", e);
                }
            }
        }).start();
    }

    @Override
    public void doLog(Channel channel, long timestamp) throws IOException {
        if (!isValid(channel)) {
            return;
        }
        Long time = channel.getTime();
        if (time == null) {
            time = timestamp;
        }
        SqlFeed sqlFeed = feeds.get(channel.getId());
        sqlFeed.insertData(new Timevalue(time, channel.getValue().asDouble()));
    }

    @Override
    public void doLog(List<Channel> channels, long timestamp) throws IOException {
        try (redis.clients.jedis.Transaction redis = client.cacheTransaction();
                org.emoncms.sql.Transaction sql = client.getTransaction()) {
            
            for (Channel channel : channels) {
                if (!isValid(channel)) {
                    return;
                }
                Long time = channel.getTime();
                if (time == null) {
                    time = timestamp;
                }
                Double value = channel.getValue().asDouble();
                try {
                    SqlFeed sqlFeed = feeds.get(channel.getId());
                    sqlFeed.insertData(sql, timestamp, value);
                    sqlFeed.cacheData(redis, timestamp, value);
                }
                catch (RedisUnavailableException ignore) {}
            }
            if (redis != null) {
                redis.exec();
            }
        } catch (Exception e) {
            throw new SqlException(e);
        }
    }

    private boolean isValid(Channel channel) throws EmoncmsSyntaxException {
        if (!channel.isValid()) {
            logger.trace("Skipped logging an invalid or empty value for channel \"{}\": {}",
                    channel.getId(), channel.getFlag());
            
            return false;
        }
        logger.trace("Preparing record to log for channel {}", channel);
        return true;
    }

    @Override
    public List<Record> getRecords(Channel channel, long startTime, long endTime) throws IOException {
        List<Record> records = new LinkedList<Record>();
        List<Timevalue> values = getFeed(channel).getData(startTime, endTime, 1);
        for (Timevalue timevalue : values) {
            Value value = new DoubleValue(timevalue.getValue());
            Record record = new Record(value, timevalue.getTime());
            records.add(record);
        }
        return records;
    }

    private SqlFeed getFeed(Channel channel) throws EmoncmsException {
        SqlFeed feed = feeds.get(channel.getId());
        if (feed == null) {
            Settings settings = channel.getSettings();
            String channelId = channel.getId();
            Integer feedId = settings.getInteger(FEED_ID);
            
            feed = SqlFeed.connect(client, client.getCache(), feedId, parseTable(feedId, channelId));
            feeds.put(channelId, feed);
        }
        return feed;
    }

    private String parseTable(Integer feedId, String channelId) throws EmoncmsSyntaxException {
        String tableName = prefix;
        if (isGeneric) {
            if (feedId == null) {
                throw new EmoncmsSyntaxException("Feed id needs to be configured for generic configurations");
            }
            tableName += feedId;
        }
        else {
            tableName += channelId;
        }
        return tableName;
    }

}
