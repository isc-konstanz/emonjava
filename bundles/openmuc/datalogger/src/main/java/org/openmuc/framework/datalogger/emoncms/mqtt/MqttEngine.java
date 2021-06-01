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
package org.openmuc.framework.datalogger.emoncms.mqtt;

import java.io.IOException;
import java.util.List;

import org.emoncms.EmoncmsType;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.emoncms.mqtt.MqttBuilder;
import org.emoncms.mqtt.MqttClient;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.emoncms.Configuration;
import org.openmuc.framework.datalogger.emoncms.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttEngine implements Engine<MqttChannel> {
    private final static Logger logger = LoggerFactory.getLogger(MqttEngine.class);

    private final static String ADDRESS = "address";
    private final static String ADDRESS_DEFAULT = "tcp://localhost";
    private final static String PORT = "port";

    private final static String USER = "user";
    private final static String PASSWORD = "password";

    private MqttClient client;

    @Override
    public EmoncmsType getType() {
        return EmoncmsType.MQTT;
    }

    @Override
    public boolean isActive() {
        return client != null && !client.isClosed();
    }

    @Override
    public void activate(Configuration config) throws IOException {
        logger.info("Activating Emoncms MQTT Logger");
        
        String address = config.getString(ADDRESS, ADDRESS_DEFAULT);
        MqttBuilder builder = MqttBuilder.create(address);
        if (config.contains(PORT)) {
            builder.setPort(config.getInteger(PORT));
        }
        if (config.contains(USER) && config.contains(PASSWORD)) {
            builder.setCredentials(config.getString(USER), config.getString(PASSWORD));
        }
        client = (MqttClient) builder.build();
        client.open();
    }

    @Override
    public void deactivate() {
        client.close();
    }

    @Override
    public void write(List<MqttChannel> channels, long timestamp) throws IOException {
        if (channels.size() == 1) {
            write(channels.get(0), timestamp);
            return;
        }
        DataList data = new DataList();
        for (MqttChannel channel : channels) {
            if (!channel.isValid()) {
                continue;
            }
            String node = channel.getNode();
            Long time = channel.getRecord().getTimestamp();
            if (time == null) {
                time = timestamp;
            }
            data.add(time, node, new Namevalue(channel.getId(), channel.getRecord().getValue().asDouble()));
        }
        client.post(data);
    }

    public void write(MqttChannel channel, long timestamp) throws IOException {
        if (!channel.isValid()) {
            return;
        }
        String node = channel.getNode();
        Long time = channel.getRecord().getTimestamp();
        if (time == null) {
            time = timestamp;
        }
        client.post(node, channel.getId(), new Timevalue(time, channel.getRecord().getValue().asDouble()));
    }

    @Override
    public List<Record> read(MqttChannel channel, long startTime, long endTime) throws IOException {
        throw new UnsupportedOperationException();
    }

}
