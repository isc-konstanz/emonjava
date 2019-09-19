/* 
 * Copyright 2016-19 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
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
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.emoncms.hibernate;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.EntityType;

import org.emoncms.Emoncms;
import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.EmoncmsUnavailableException;
import org.emoncms.Feed;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.metamodel.internal.EntityTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateClient implements Emoncms, HibernateCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(HibernateClient.class);

	public static final String SCALE_INTEGER_TYPE = "ScaleInteger";

	private static final String CONFIG_PATH = "hibernate.configPath";
	private static final String CONFIG_PATH_DEFAULT = "conf/";

	private static final String CONFIG = "hibernate.config.file";
	private static final String CONFIG_DEFAULT = "hibernate.cfg.xml";

	private final String driver;
	private final String address;
	private final String dialect;
	private final String user;
	private final String password;

	private final File properties;
	private Configuration config;
	private SessionFactory factory;

	private Map<String, HibernateFeed> feeds;

	protected HibernateClient(String driver, String address, String dialect, String user, String password) {
		this.driver = driver;
		this.address = address;
		this.dialect = dialect;
		
		this.user = user;
		this.password = password;
		
		String configPath = System.getProperty(CONFIG_PATH, CONFIG_PATH_DEFAULT);
		String configFile = System.getProperty(CONFIG, CONFIG_DEFAULT);
		properties = new File(configPath + configFile);
	}

	protected HibernateClient(HibernateBuilder builder) {
		this(builder.driverClass, builder.databaseUrl, builder.databaseDialect, 
				builder.user, builder.password);
	}

	public String getAddress() {
		return address;
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.SQL;
	}

	@Override
	public void close() {
		logger.info("Shutting emoncms SQL connection \"{}\" down", address);
		if (factory != null) {
			factory.close();
			factory = null;
		}
	}

	@Override
	public boolean isClosed() {
		if (factory == null || factory.isClosed()) {
			return true;
		}
		return false;
	}

	@Override
	public void open() throws EmoncmsUnavailableException {
		logger.info("Initializing emoncms SQL connection \"{}\"", address);
		initialize();
	}

	private void initialize() throws EmoncmsUnavailableException {
        config = new Configuration().configure(properties);
        config = config.setProperty("hibernate.connection.driver_class", driver);
        config = config.setProperty("hibernate.connection.url", address);
        if (user !=  null && password != null) {
        	config = config.setProperty("hibernate.connection.username", user);
        	config = config.setProperty("hibernate.connection.password", password);
        }
        config = config.setProperty("hibernate.dialect", dialect);
		
        if (feeds != null) {
    		for (HibernateFeed feed : feeds.values()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Entity of feed " + feed.getEntityName());
                }
    	        if (feed.containsUserType(SCALE_INTEGER_TYPE)) {
        			config.registerTypeContributor((typeContributions, serviceRegistry) -> {
        					typeContributions.contributeType(ScaleIntegerType.INSTANCE, SCALE_INTEGER_TYPE);
        			});
        			break;
    	        }
        		config.addInputStream(feed.getMapping());
    		}
        }
		factory = config.buildSessionFactory();
	}

	public Feed getFeed(String table) throws EmoncmsException {
		logger.debug("Requesting feed with entity: {}", table);
		
		HibernateFeed feed = feeds.get(table);
		if (feed != null) {
			if (factory == null || !feedExists(table)) {
				throw new EmoncmsException("Feed with entity: " + table + " not found!");
			}
		}
		else {
			feed = new HibernateFeed(this, table);
			feeds.put(table, feed);
		}
		return feed;
	}

	private boolean feedExists(String entityName) {
		//TODO: EntityTypeImpl is deprecated and needs to be handled better than to use internal hibernate classes
		Session session = factory.openSession();
		Iterator<EntityType<?>> it = session.getMetamodel().getEntities().iterator();
		while (it.hasNext()) {
			EntityTypeImpl<?> type = (EntityTypeImpl<?>)it.next();
			if (entityName.equals(type.getTypeName())) {
				session.close();
				return true;
			}
		}
		session.close();
		return false;
	}

	@Override
	public Map<Feed, Double> getFeedValues(List<Feed> feeds) throws EmoncmsException {
		Map<Feed, Double> map = new HashMap<Feed, Double>();
		
		for (Feed feed : feeds) {
			map.put(feed, feed.getLatestValue());
		}
		return map;
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
	public void post(DataList dataList) throws EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	@Override
	public Session getSession() throws EmoncmsUnavailableException {
		if (factory == null || factory.isClosed()) {
			throw new EmoncmsUnavailableException("Hibernate session factory not available");
		}
		return factory.openSession();
	}

}
