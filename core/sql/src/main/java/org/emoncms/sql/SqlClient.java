package org.emoncms.sql;

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

public class SqlClient implements Emoncms, SqlFactoryGetter {
	private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);

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

	private SessionFactory factory;
	private Map<String, SqlFeed> feedMap;

	protected BasicType userType;

	protected SqlClient(String connectionDriverClass, String connectionUrl, String dialect, String user, String password) {
		this.connectionDriverClass = connectionDriverClass;
		this.connectionUrl = connectionUrl;
		this.dialect = dialect;
		this.user = user;
		this.password = password;
		
		String configPath = System.getProperty(CONFIG_PATH, DEFAULT_CONFIG_PATH);
		String hibernateConfigFile = System.getProperty(HIBERNATE_CONFIG, DEFAULT_HIBERNATE_CONFIG);
		String hibernatePropsFilePath = configPath + hibernateConfigFile;
		hibernatePropsFile = new File(hibernatePropsFilePath);
	}

	public SqlClient(SqlBuilder sqlBuilder) {
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

	public void setFeedMap(Map<String, SqlFeed> feedMap) {
		this.feedMap = feedMap;
	}

	private void initialize() {
        Configuration config = new Configuration().configure(hibernatePropsFile);
        config = config.setProperty("hibernate.connection.driver_class", connectionDriverClass);
        config = config.setProperty("hibernate.connection.url", connectionUrl);
        if (user !=  null) config = config.setProperty("hibernate.connection.username", user);
        if (password != null) config = config.setProperty("hibernate.connection.password", password);
        config = config.setProperty("hibernate.dialect", dialect);
        if (feedMap == null) return;
		for (SqlFeed feed : feedMap.values()) {
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
			feed = new SqlFeed(this, entityName);
			feedMap.put(entityName, (SqlFeed) feed);
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
