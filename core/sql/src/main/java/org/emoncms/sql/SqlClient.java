package org.emoncms.sql;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.metamodel.internal.EntityTypeImpl;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlClient implements Emoncms, SqlFactoryGetter {
	private static final Logger logger = LoggerFactory.getLogger(SqlClient.class);


	private static final String CONFIG_PATH = "hibernate.configPath";
	private static final String DEFAULT_CONFIG_PATH = "conf/";
	private static final String HIBERNATE_CONFIG = "hibernate.config.file";
	private static final String DEFAULT_HIBERNATE_CONFIG = "hibernate.cfg.xml";

	private final String id;
	private final String connectionDriverClass;
	private final String connectionUrl;
	private final String user;
	private final String password;
	
	private final File hibernatePropsFile;

	private SessionFactory factory;
	private Map<Integer, SqlFeed> feedMap;


	protected SqlClient(String id, String connectionDriverClass, String connectionUrl, String user, String password) {
		this.id = id;
		
		this.connectionDriverClass = connectionDriverClass;
		this.connectionUrl = connectionUrl;
		this.user = user;
		this.password = password;
		
		String configPath = System.getProperty(CONFIG_PATH, DEFAULT_CONFIG_PATH);
		String hibernateConfigFile = System.getProperty(HIBERNATE_CONFIG, DEFAULT_HIBERNATE_CONFIG);
		String hibernatePropsFilePath = configPath + hibernateConfigFile;
		hibernatePropsFile = new File(hibernatePropsFilePath);
	}

	public SqlClient(SqlBuilder sqlBuilder) {
		this(sqlBuilder.id, sqlBuilder.connectionDriverClass, sqlBuilder.connectionUrl, sqlBuilder.user, sqlBuilder.password);
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

	public void setFeedMap(Map<Integer, SqlFeed> feedMap) {
		this.feedMap = feedMap;
	}

	private void initialize() {
        Configuration config = new Configuration().configure(hibernatePropsFile);
        config = config.setProperty("connection.driver_class", connectionDriverClass);
        config = config.setProperty("connection.url", connectionUrl);
        config = config.setProperty("connection.username", user);
        config = config.setProperty("connection.password", password);
        if (feedMap == null) return;
		for (SqlFeed feed : feedMap.values()) {
            if (logger.isTraceEnabled()) {
                logger.trace("timeSeries.getId() " + feed.getId());
            }
        	InputStream inputStream = feed.createMappingInputStream();
    		config.addInputStream(inputStream);
		}
		if (!isClosed()) {
			close();
		}
		factory = config.buildSessionFactory();
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		return factory;
	}
	
	@Override
	public Feed getFeed(int id) throws EmoncmsException {
		logger.debug("Requesting feed with id: {}", id);
		
		Feed feed = feedMap.get(id);
		
		if (feed != null) {
			if (factory == null || !feedExists(id)) {
				throw new EmoncmsException("Feed " + id + " not found!");
			}
		}
		else {
			feed = new SqlFeed(this, id, "Double");
			feedMap.put(id, (SqlFeed) feed);
		}
		return feed;
	}
	
	private boolean feedExists(int id) {
		//TODO find better way than query table
		
		Session session = factory.openSession();
		EntityTypeImpl<?> type = (EntityTypeImpl<?>) session.getMetamodel().getEntities().toArray()[0];
		if (("feed_" + id).equals(type.getTypeName())) {
			session.close();
			return true;
		}
		else {
			session.close();
			return false;
		}
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
