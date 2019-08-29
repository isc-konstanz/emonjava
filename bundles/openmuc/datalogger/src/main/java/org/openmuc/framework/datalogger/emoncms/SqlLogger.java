package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsSyntaxException;
import org.emoncms.EmoncmsType;
import org.emoncms.data.Timevalue;
import org.emoncms.sql.SqlBuilder;
import org.emoncms.sql.SqlClient;
import org.emoncms.sql.SqlException;
import org.emoncms.sql.SqlFeed;
import org.emoncms.sql.Transaction;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;
import org.openmuc.framework.datalogger.dynamic.DynamicLoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlLogger implements DynamicLoggerService {
    private final static Logger logger = LoggerFactory.getLogger(SqlLogger.class);

    protected final static String FEED_ID = "feedid";

    protected final static String DRIVER = "driver";
    protected final static String TYPE = "type";
    protected final static String ADDRESS = "address";
    protected final static String ADDRESS_DEFAULT = "127.0.0.1";
    protected final static String PORT = "port";

    protected final static String DATABASE_NAME = "database";
    protected final static String DATABASE_USER = "user";
    protected final static String DATABASE_PASSWORD = "password";

    protected final static String PREFIX = "prefix";
    protected final static String GENERIC = "generic";

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
        
        String address = config.getString(ADDRESS, ADDRESS_DEFAULT);
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
                        try {
                            String channelId = channel.getId();
                            Integer feedId = null;
                            if (channel.hasSetting(FEED_ID)) {
                                feedId = channel.getSetting(FEED_ID).asInt();
                            }
                            
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
                            feeds.put(channelId, SqlFeed.connect(client, transaction, feedId, tableName, valueType, false));
                            
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
        feeds.get(channel.getId()).insertData(new Timevalue(time, channel.getValue().asDouble()));
    }

    @Override
    public void doLog(List<Channel> channels, long timestamp) throws IOException {
        try (Transaction transaction = client.getTransaction()) {
            for (Channel channel : channels) {
                doLog(transaction, channel, timestamp);
            }
        } catch (Exception e) {
            throw new SqlException(e);
        }
    }

    private void doLog(Transaction transaction, Channel channel, long timestamp) throws IOException {
        if (!isValid(channel)) {
            return;
        }
        Long time = channel.getTime();
        if (time == null) {
            time = timestamp;
        }
        feeds.get(channel.getId()).insertData(transaction, timestamp, channel.getValue().asDouble());
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
        // TODO Auto-generated method stub
        return null;
    }
}
