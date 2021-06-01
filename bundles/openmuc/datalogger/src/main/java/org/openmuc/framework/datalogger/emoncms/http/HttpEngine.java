/* 
 * Copyright 2016-21 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.framework.datalogger.emoncms.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.Feed;
import org.emoncms.data.Authentication;
import org.emoncms.data.Data;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.http.HttpBuilder;
import org.emoncms.http.HttpConnection;
import org.emoncms.http.HttpFeed;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.emoncms.Configuration;
import org.openmuc.framework.datalogger.emoncms.Engine;
import org.openmuc.framework.datalogger.emoncms.http.data.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpEngine implements Engine<HttpChannel> {
    private final static Logger logger = LoggerFactory.getLogger(HttpEngine.class);

    private final static String ADDRESS = "address";
    private final static String ADDRESS_DEFAULT = "http://localhost/emoncms/";

    private final static String AUTHORIZATION = "authorization";
    private final static String AUTHENTICATION = "authentication";

    private final static String MAX_THREADS = "maxThreads";

    private HttpConnection connection;

    @Override
    public EmoncmsType getType() {
        return EmoncmsType.HTTP;
    }

    @Override
    public boolean isActive() {
        return connection != null && !connection.isClosed();
    }

    @Override
    public void activate(Configuration config) throws IOException {
        logger.info("Activating Emoncms HTTP Logger");
        
        String address = config.getString(ADDRESS, ADDRESS_DEFAULT);
        HttpBuilder builder = HttpBuilder.create(address);
        if (config.contains(MAX_THREADS)) {
            builder.setMaxThreads(config.getInteger(MAX_THREADS));
        }
        if (config.contains(AUTHORIZATION) && config.contains(AUTHENTICATION)) {
            builder.setCredentials(config.getString(AUTHORIZATION), config.getString(AUTHENTICATION));
        }
        connection = (HttpConnection) builder.build();
        connection.open();
    }

    @Override
    public void deactivate() {
        connection.close();
    }

    @Override
    public void write(List<HttpChannel> channels, long timestamp) throws IOException {
        if (channels.size() == 1) {
            write(channels.get(0), timestamp);
            return;
        }
        List<DataContainer> containers = new ArrayList<DataContainer>();
        for (HttpChannel channel : channels) {
            if (!channel.isValid()) {
                continue;
            }
            String node = channel.getNode();
            Long time = channel.getRecord().getTimestamp();
            if (time == null) {
                time = timestamp;
            }
            
            Authentication authentication = channel.getAuthentication();
            DataContainer container = null;
            for (DataContainer c : containers) {
                if (c.equals(authentication)) {
                    container = c;
                    break;
                }
            }
            if (container == null) {
                // No input collection for that device exists yet, so it needs to be created
                container = new DataContainer(authentication);
                containers.add(container);
            }
            container.add(time, node, new Namevalue(channel.getId(), channel.getRecord().getValue().asDouble()));
        }
        for (DataContainer container : containers) {
            logger.debug("Logging {} values with authentication \"{}\"", container.size(), container.getAuthenticator());
            
            Authentication authenticator = container.getAuthenticator();
            if (container.size() == 1) {
                Data data = container.get(0);
                
                // Data time is already in seconds, but needs to be in milliseconds for post()
                long time = data.getTime()*1000;
                if (authenticator.isDefault()) {
                    connection.post(data.getNode(), time, data.getNamevalues());
                }
                else {
                    connection.post(data.getNode(), time, data.getNamevalues(), authenticator);
                }
            }
            else {
                if (authenticator.isDefault()) {
                    connection.post(container);
                }
                else {
                    connection.post(container, authenticator);
                }
            }
        }
    }

    public void write(HttpChannel channel, long timestamp) throws IOException {
        if (!channel.isValid()) {
            return;
        }
        String node = channel.getNode();
        Long time = channel.getRecord().getTimestamp();
        if (time == null) {
            time = timestamp;
        }
        connection.post(node, channel.getId(), new Timevalue(time, channel.getRecord().getValue().asDouble()));
    }

    @Override
    public List<Record> read(HttpChannel channel, long startTime, long endTime) throws IOException {
        if (!channel.hasFeed()) {
            throw new EmoncmsException("Unable to retrieve values for channel without configured feed: " + channel.getId());
        }
        Feed feed = HttpFeed.connect(connection, channel.getFeed());
        List<Record> records = new LinkedList<Record>();
        List<Timevalue> data = feed.getData(startTime, endTime, channel.getLoggingInterval());
        for (Timevalue timevalue : data) {
            records.add(new Record(new DoubleValue(timevalue.getValue()), timevalue.getTime()));
        }
        return records;
    }

}
