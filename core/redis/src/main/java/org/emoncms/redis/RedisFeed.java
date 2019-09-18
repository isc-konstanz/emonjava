package org.emoncms.redis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.Feed;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Transaction;

public class RedisFeed implements Feed {
    private static final Logger logger = LoggerFactory.getLogger(RedisFeed.class);

    protected static final String FEED_PREFIX = "feed:";

    protected final RedisCallbacks callbacks;

    protected final Integer id;

    public static RedisFeed connect(RedisCallbacks redis, Integer id) 
            throws EmoncmsException {
        if (id != null && id < 1) {
            throw new EmoncmsException("Invalid feed id: "+id);
        }
        return new RedisFeed(redis, id);
    }

    protected RedisFeed(RedisCallbacks callbacks, Integer id) throws EmoncmsException {
        this.callbacks = callbacks;
        this.id = id;
    }

    @Override
    public int getId() {
        if (id != null) {
            return id;
        }
        throw new UnsupportedOperationException("Unconfigured feedid for table " + id);
    }

    @Override
    public EmoncmsType getType() {
        return EmoncmsType.REDIS;
    }

    @Override
    public Double getLatestValue() throws EmoncmsException {
        String key = parseKey();
        logger.debug("Retrieving cached value for {}", key);
        
        if (!callbacks.exists(key, "value")) {
            throw new RedisException("No value cached yet for id:"+id);
        }
        try {
            return Double.valueOf(callbacks.get(key, "value"));
            
        } catch (NumberFormatException e) {
            throw new RedisException("Invalid cached value returned: "+e.getMessage());
        }
    }

    @Override
    public Timevalue getLatestTimevalue() throws EmoncmsException {
        String key = parseKey();
        logger.debug("Retrieving cached timevalue for {}", key);
        
        if (!callbacks.exists(key, "time")) {
            throw new RedisException("No value cached yet for id:"+id);
        }
        Map<String, String> result = callbacks.get(key, "time", "value");
        try {
            Long time = Long.valueOf(result.get("time"));
            Double value = Double.valueOf(result.get("value"));
            
            return new Timevalue(time, value);
            
        } catch (NumberFormatException e) {
            throw new RedisException("Invalid cached value returned: "+e.getMessage());
        }
    }

    @Override
    public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
        throw new UnsupportedOperationException("Unsupported for type "+getType());
    }

    @Override
    public void insertData(Timevalue timevalue) throws EmoncmsException {
        this.cacheData(timevalue.getTime(), timevalue.getValue());
    }

    public void cacheData(long timestamp, double data) throws EmoncmsException {
        String key = parseKey();
        logger.debug("Caching value {}:{}", key, data);
        
        if (!callbacks.exists(key, "time")) {
            throw new RedisException("No value cached yet for id:"+id);
        }
        Map<String, String> values = new HashMap<String, String>();
    	values.put("time", String.valueOf((int) Math.round((double) timestamp/1000.0)));
    	values.put("value", String.valueOf(data));
    	
        callbacks.set(key, values);
    }

    public void cacheData(Transaction transaction, long timestamp, double data) throws EmoncmsException {
        String key = parseKey();
        logger.debug("Caching value {}:{}", key, data);
        
        Map<String, String> values = new HashMap<String, String>();
    	values.put("time", String.valueOf((int) Math.round((double) timestamp/1000.0)));
    	values.put("value", String.valueOf(data));
    	
    	callbacks.set(transaction, key, values);
    }

    private String parseKey() throws RedisUnavailableException {
        if (callbacks == null) {
            throw new RedisUnavailableException();
        }
        if (id == null) {
            throw new RedisUnavailableException("No feedid configured");
        }
        return FEED_PREFIX+String.valueOf(id);
    }

}
