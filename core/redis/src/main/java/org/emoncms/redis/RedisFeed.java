package org.emoncms.redis;

import java.util.LinkedList;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.Feed;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisFeed implements Feed {
    private static final Logger logger = LoggerFactory.getLogger(RedisFeed.class);

    protected Integer id;

    protected RedisFeed(Integer id) throws EmoncmsException {
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
        // TODO Auto-generated method stub
    	return null;
    }

    @Override
    public Timevalue getLatestTimevalue() throws EmoncmsException {
        // TODO Auto-generated method stub
    	return null;
    }

    @Override
    public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
        throw new UnsupportedOperationException("Unsupported for type "+getType());
    }

    @Override
    public void insertData(Timevalue timevalue) throws EmoncmsException {
        // TODO Auto-generated method stub
    }

}
