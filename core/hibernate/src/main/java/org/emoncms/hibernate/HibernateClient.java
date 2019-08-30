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
import java.io.InputStream;
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
import org.hibernate.type.BasicType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateClient implements Emoncms, HibernateFactoryGetter {
	private static final Logger logger = LoggerFactory.getLogger(HibernateClient.class);

	public static final String SCALE_INTEGER_TYPE = "ScaleInteger";
	
	private static final String CONFIG_PATH = "hibernate.configPath";
	private static final String DEFAULT_CONFIG_PATH = "conf/";
	private static final String HIBERNATE_CONFIG = "hibernate.config.file";
	private static final String DEFAULT_HIBERNATE_CONFIG = "hibernate.cfg.xml";

	private final String connectionDriverClass;
	private final String connectionUrl;
	private final String dialect;
	private final String user;
	private final String password;
	
	private final File hibernatePropsFile;
	private final String hibernatePropsFilePath;

	private SessionFactory factory;
	private Map<String, HibernateFeed> feedMap;

	protected BasicType userType;

	protected HibernateClient(String connectionDriverClass, String connectionUrl, String dialect, String user, String password) {
		this.connectionDriverClass = connectionDriverClass;
		this.connectionUrl = connectionUrl;
		this.dialect = dialect;
		this.user = user;
		this.password = password;
		
		String configPath = System.getProperty(CONFIG_PATH, DEFAULT_CONFIG_PATH);
		String hibernateConfigFile = System.getProperty(HIBERNATE_CONFIG, DEFAULT_HIBERNATE_CONFIG);
		hibernatePropsFilePath = configPath + hibernateConfigFile;
		hibernatePropsFile = new File(hibernatePropsFilePath);
	}

	public HibernateClient(HibernateBuilder sqlBuilder) {
		this(sqlBuilder.connectionDriverClass, sqlBuilder.connectionUrl, sqlBuilder.databaseDialect, 
				sqlBuilder.user, sqlBuilder.password);
	}

	@Override
	public void close() {
		logger.info("Shutting emoncms SQL connection \"{}\" down", connectionUrl);
		if (factory != null) {
			factory.close();
			factory = null;
		}
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.SQL;
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
		logger.info("Initializing emoncms SQL connection \"{}\"", connectionUrl);
		initialize();
	}

	public void setFeedMap(Map<String, HibernateFeed> feedMap) {
		this.feedMap = feedMap;
	}

	private void initialize() throws EmoncmsUnavailableException {
        Configuration config = new Configuration().configure(hibernatePropsFile);
        config = config.setProperty("hibernate.connection.driver_class", connectionDriverClass);
        config = config.setProperty("hibernate.connection.url", connectionUrl);
        if (user !=  null) config = config.setProperty("hibernate.connection.username", user);
        if (password != null) config = config.setProperty("hibernate.connection.password", password);
        config = config.setProperty("hibernate.dialect", dialect);
        if (feedMap == null) return;
		for (HibernateFeed feed : feedMap.values()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Entity of feed " + feed.getEntityName());
            }
	        if (feed.containsUserType(SCALE_INTEGER_TYPE)) {
	        	userType = ScaleIntegerType.INSTANCE;
	        }            
        	InputStream inputStream = feed.createMappingInputStream();
    		config.addInputStream(inputStream);
		}
		if (!isClosed()) {
			close();
		}
		
		if (userType !=  null) {
			config.registerTypeContributor( (typeContributions, serviceRegistry) -> {
					typeContributions.contributeType(userType, SCALE_INTEGER_TYPE);
			} );
		}
		
		factory = config.buildSessionFactory();
	}
	
	public BasicType getUserType() {
		return userType;
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		return factory;
	}
	
	public Feed getFeed(String entityName) throws EmoncmsException {
		logger.debug("Requesting feed with entity: {}", entityName);
		
		Feed feed = feedMap.get(entityName);
		
		if (feed != null) {
			if (factory == null || !feedExists(entityName)) {
				throw new EmoncmsException("Feed with entity: " + entityName + " not found!");
			}
		}
		else {
			feed = new HibernateFeed(this, entityName);
			feedMap.put(entityName, (HibernateFeed) feed);
		}
		return feed;		
	}

	private boolean feedExists(String entityName) {
		//TODO EntityTypeImpl is deprecated and needs to be handled better than to use internal hibernate classes
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

	public String getConnectionUrl() {
		return connectionUrl;
	}

}
