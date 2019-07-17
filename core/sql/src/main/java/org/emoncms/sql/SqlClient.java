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
import org.emoncms.data.Data;
import org.emoncms.data.DataList;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Timevalue;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlClient implements Emoncms {
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
	private List<SqlTimeSeries> timeSeriesArray;
	private Map<String, SqlTimeSeries> idTimeSeriesMap;


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
		if (factory != null && factory.isClosed()) {
			return true;
		}
		return false;
	}

	@Override
	public void open() throws EmoncmsUnavailableException {
		logger.info("Initializing emoncms SQL connection \"{}\"", connectionUrl);
		initialize();
	}

	public void setTimeSeriesArray(List<SqlTimeSeries> timeSeriesArray) {
		this.timeSeriesArray = timeSeriesArray;
	}

	private void initialize() {
        Configuration config = new Configuration().configure(hibernatePropsFile);
        config = config.setProperty("connection.driver_class", connectionDriverClass);
        config = config.setProperty("connection.url", connectionUrl);
        config = config.setProperty("connection.username", user);
        config = config.setProperty("connection.password", password);
        idTimeSeriesMap = new HashMap<String, SqlTimeSeries>(timeSeriesArray.size());
        if (timeSeriesArray == null) return;
		for (SqlTimeSeries timeSeries : timeSeriesArray) {
            if (logger.isTraceEnabled()) {
                logger.trace("timeSeries.getId() " + timeSeries.getId());
            }
            
        	InputStream inputStream = timeSeries.createMappingInputStream();
        	idTimeSeriesMap.put(timeSeries.getId(), timeSeries);
    		config.addInputStream(inputStream);
		}
		if (!isClosed()) {
			close();
		}
		factory = config.buildSessionFactory();
	}

	@SuppressWarnings("null")
	@Override
	public void post(String node, String name, Timevalue timevalue) throws EmoncmsException {
		SqlTimeSeries ts = idTimeSeriesMap.get(name);
		if (ts == null) {
			ts = new SqlTimeSeries(name, "DOUBLE");
			timeSeriesArray.add(ts);
			open();
		}
		
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
        
    	session.save(id, ts.buildMap(timevalue.getTime(), timevalue.getValue(), (Byte) null));
		
		t.commit();
		session.close();
	}

	@Override
	public void post(String node, Long time, List<Namevalue> namevalues) throws EmoncmsException {
		boolean hasNewTimeSeries = false;
		for (Namevalue nameValue: namevalues) {
			if (!idTimeSeriesMap.containsKey(nameValue.getName())) {
				timeSeriesArray.add(new SqlTimeSeries(nameValue.getName(), "DOUBLE"));
				hasNewTimeSeries = true;
			}
		}
		if (hasNewTimeSeries) open();

		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
        
		for (Namevalue nameValue: namevalues) {
			SqlTimeSeries ts = idTimeSeriesMap.get(nameValue.getName());
			session.save(id, ts.buildMap(time, nameValue.getValue(), (Byte) null));
		}

		t.commit();
		session.close();
	}

	@Override
	public void post(DataList dataList) throws EmoncmsException {
		logger.debug("Requesting to bulk post {} data sets", dataList.size());
		
		dataList.sort();
		
		boolean hasNewTimeSeries = false;
		for (Data data : dataList) {
			for (Namevalue nameValue: data.getNamevalues()) {
				if (!idTimeSeriesMap.containsKey(nameValue.getName())) {
					timeSeriesArray.add(new SqlTimeSeries(nameValue.getName(), "DOUBLE"));
					hasNewTimeSeries = true;
				}
			}
		}
		if (hasNewTimeSeries) open();
		
		for (Data data : dataList) {
			post(data.getNode(), data.getTime(), data.getNamevalues());
		}
	}

	public String getConnectionUrl() {
		return connectionUrl;
	}

}
