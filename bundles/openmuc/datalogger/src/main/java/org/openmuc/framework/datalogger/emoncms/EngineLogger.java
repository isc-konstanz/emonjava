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
package org.openmuc.framework.datalogger.emoncms;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Settings;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.DataLoggerActivator;
import org.openmuc.framework.datalogger.LoggingChannelFactory;
import org.openmuc.framework.datalogger.annotation.Configure;
import org.openmuc.framework.datalogger.annotation.DataLogger;
import org.openmuc.framework.datalogger.annotation.Read;
import org.openmuc.framework.datalogger.annotation.Write;
import org.openmuc.framework.datalogger.emoncms.EngineCollection.ChannelCollection;
import org.openmuc.framework.datalogger.emoncms.http.HttpChannel;
import org.openmuc.framework.datalogger.emoncms.http.HttpEngine;
import org.openmuc.framework.datalogger.emoncms.mqtt.MqttChannel;
import org.openmuc.framework.datalogger.emoncms.mqtt.MqttEngine;
import org.openmuc.framework.datalogger.emoncms.sql.SqlChannel;
import org.openmuc.framework.datalogger.emoncms.sql.SqlEngine;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DataLoggerService.class)
@DataLogger(id = EngineLogger.ID)
public class EngineLogger extends DataLoggerActivator implements DataLoggerService, LoggingChannelFactory {
    private final static Logger logger = LoggerFactory.getLogger(EngineLogger.class);

    public final static String ID = "emonlogger";

    private static final String DISABLE = "disable";
    private static final String DISABLED = "disabled";

    private static final String CONFIG = System.getProperty(EngineLogger.class.
            getPackage().getName().toLowerCase() + ".config", "conf" + File.separator + "emoncms.conf");

    protected static final String DEFAULT = System.getProperty(EngineLogger.class.
            getPackage().getName().toLowerCase() + ".default", "HTTP").toUpperCase();

    protected final Map<EmoncmsType, Engine<?>> engines = new LinkedHashMap<EmoncmsType, Engine<?>>();

    @Activate
    public void activate() {
        try {
            Ini config = new Ini(new File(CONFIG));
            activate(config, EmoncmsType.HTTP);
            activate(config, EmoncmsType.MQTT);
            activate(config, EmoncmsType.SQL);
            
        } catch (Exception e) {
            logger.error("Error while reading emoncms configuration: {}", e.getMessage());
        }
    }

    private void activate(Ini config, EmoncmsType type) {
        try {
            Section section;
            if (config.containsKey(type.toString())) {
                section = config.get(type.toString());
            }
            else if (config.keySet().size() == 1 && type == EmoncmsType.valueOf(DEFAULT)) {
                section = config.get("Emoncms");
            }
            else {
                logger.debug("Skipping invalid {} engine activation", type.toString());
                return;
            }
            if (Boolean.parseBoolean(section.get(DISABLE)) ||
                    Boolean.parseBoolean(section.get(DISABLED))) {
                
                logger.debug("Skipping disabled {} engine activation", type.toString());
                return;
            }
            try {
                Engine<?> engine;
                switch(type) {
                case HTTP:
                    engine = new HttpEngine();
                    break;
                case MQTT:
                    engine = new MqttEngine();
                    break;
                case SQL:
                    engine = new SqlEngine();
                    break;
                default:
                    return;
                }
                engine.activate(new Configuration(section));
                engines.put(engine.getType(), engine);
                
            } catch (EmoncmsUnavailableException e) {
                logger.warn("Unable to establish {} connection. "
                        + "Please remove or disable the configuration section if this is intentional.", type.toString());
            }
        } catch (Exception e) {
            logger.error("Error while activating engine \"{}: {}", type.toString(), e.getMessage());
        }
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating Emoncms Logger");
        for (Engine<?> engine : engines.values()) {
            try {
                if (engine.isActive()) {
                    engine.deactivate();
                }
            } catch (Exception e) {
                logger.warn("Error while deactivating engine: {}", e);
            }
        }
    }

    @Configure
    public void configure(List<EngineChannel> channels) throws IOException {
        for (ChannelCollection<?> channelCollection : new EngineCollection(engines, channels)) {
            try {
                channelCollection.configure();
                if (logger.isDebugEnabled()) {
                    logger.debug("Configured {} channels for the {} engine", 
                            channelCollection.size(), channelCollection.getEngine().getType());
                }
            } catch(IOException e) {
                logger.warn("Failed to configure channels: {}", e.getMessage());
                
            } catch(Exception e) {
                logger.warn("Error while configuring channels:", e);
            }
        }
    }

    @Write
    public void write(List<EngineChannel> channels, long timestamp) throws IOException {
        EngineCollection engines = new EngineCollection(this.engines, channels);
        for (ChannelCollection<?> collection : engines) {
            try {
                collection.write(timestamp);
                
            } catch(IOException e) {
                logger.warn("Failed to log channels: {}", e.getMessage());
                
            } catch(Exception e) {
                logger.warn("Error while logging channels:", e);
            }
        }
    }

    @Read
    public List<Record> read(EngineChannel channel, long startTime, long endTime) throws IOException {
        Engine<?> engine = engines.get(channel.getEngine());
        if (engine == null && engines.size() > 0) {
            engine = engines.values().iterator().next();
        }
        if (engine == null) {
            throw new IOException("Engine unavailable: " + channel.getEngine());
        }
        return read(engine, channel, startTime, endTime);
    }

    @SuppressWarnings("unchecked")
    private <C extends EngineChannel> List<Record> read(Engine<?> engine, C channel, long startTime, long endTime) 
            throws IOException {
        
        return ((Engine<C>) engine).read(channel, startTime, endTime);
    }

	@Override
	public EngineChannel newChannel(Settings settings) throws ArgumentSyntaxException {
		String typeStr = "http";
		if (settings.contains(EngineChannel.ENGINE)) {
			typeStr = settings.getString(EngineChannel.ENGINE);
		}
		else if (settings.contains(EngineChannel.LOGGER)) {
			typeStr = settings.getString(EngineChannel.LOGGER);
		}
		EmoncmsType type = EmoncmsType.valueOf(typeStr.toUpperCase());
        switch (type) {
        case HTTP:
            return new HttpChannel();
        case MQTT:
            return new MqttChannel();
        case SQL:
            return new SqlChannel();
        case REDIS:
        default:
            throw new ArgumentSyntaxException("Emoncms "+type.toString().toLowerCase()+" logging engine not yet implemented.");
        }
	}

}
