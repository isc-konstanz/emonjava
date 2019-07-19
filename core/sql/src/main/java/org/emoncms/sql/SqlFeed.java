package org.emoncms.sql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsException;
import org.emoncms.EmoncmsType;
import org.emoncms.Feed;
import org.emoncms.data.Timevalue;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlFeed implements Feed {
	private static final Logger logger = LoggerFactory.getLogger(SqlFeed.class);

	protected static final String CONFIG_PATH = "hibernate.configPath";
	protected static final String DEFAULT_CONFIG_PATH = "conf/";
	protected static final String MAPPING_TEMPLATE_FILE = "hibernate.record.template";
	protected static final String DEFAULT_MAPPING_TEMPLATE = "hibernate.record.hbm.xml";
	protected static final String PREFIX_FEED = "feed_";

	protected static final String VALUE_COLUMN = "data"; 
	protected static final String TIME_COLUMN = "time"; 

	protected static String MAPPING_TEMPLATE = null;

	protected final int id;
	protected String entityName;
	protected final String dataType;
	protected SessionFactory factory;
	
	public SqlFeed(int id, String dataType) {
		this.id = id;
		this.entityName = PREFIX_FEED + id;
		this.dataType = dataType;

		if (MAPPING_TEMPLATE == null) {
			loadMappingTemplate();
		}
	}
	
	public SqlFeed(SessionFactory factory, int id, String dataType) {
		this(id, dataType);
		this.factory = factory;
	}
	
	protected void loadMappingTemplate() {
		String configPath = System.getProperty(CONFIG_PATH, DEFAULT_CONFIG_PATH);
		String mappingTemplateFile = System.getProperty(MAPPING_TEMPLATE_FILE, DEFAULT_MAPPING_TEMPLATE);
		String templateFileStr = configPath + mappingTemplateFile;
		try {
			MAPPING_TEMPLATE = new String(Files.readAllBytes(Paths.get(templateFileStr)));
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public InputStream createMappingInputStream() {
		String mapping = MAPPING_TEMPLATE.replace("entity-name=\"entity\"", "entity-name=\""+id+"\"");
		switch (dataType) {
		case "Boolean":
			mapping = mapping.replace("java.lang.Object", "java.lang.Boolean");
			break;
		case "Byte":
			mapping = mapping.replace("java.lang.Object", "java.lang.Byte");
			break;
		case "Double":
			mapping = mapping.replace("java.lang.Object", "java.lang.Double");
			break;
		case "Float":
			mapping = mapping.replace("java.lang.Object", "java.lang.Float");
			break;
		case "Integer":
			mapping = mapping.replace("java.lang.Object", "java.lang.Integer");
			break;
		case "Long":
			mapping = mapping.replace("java.lang.Object", "java.lang.Long");
			break;
		case "Short":
			mapping = mapping.replace("java.lang.Object", "java.lang.Short");
			break;
		case "String":
			mapping = mapping.replace("java.lang.Object", "java.lang.String");
			break;
		default:
			mapping = mapping.replace("java.lang.Object", "java.lang.String");
			break;
		}
		return new ByteArrayInputStream(StandardCharsets.UTF_16.encode(mapping).array());		
		
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	public void setSessionFactory(SessionFactory factory) {
		this.factory = factory;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	@Override
	public EmoncmsType getType() {
		return EmoncmsType.SQL;
	}
	
	/* (non-Javadoc)
	 * @see org.emoncms.Feed#delete()
	 * Drop Table from DataBase
	 */
	@Override
	public void delete() {
		//TODO Should drop table but also look for records in feeds description table 
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	@Override
	public void deleteData(long time) {
		logger.debug("Requesting to delete value at time: {} for feed with id: {}", time, id);

		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		
		String hql = "delete " + entityName + " where " + TIME_COLUMN + " = :time";
		Query<?> query = session.createQuery(hql).setParameter("time", time);
		int deletedRecs = query.executeUpdate();
		if (deletedRecs == 0) {
			logger.debug("No value found at time: {} for feed with id: {}", time, id);
		}
		
		t.commit();
		session.close();		
	}

	@Override
	public void deleteDataRange(long start, long end) throws EmoncmsException {
		logger.debug("Requesting to delete values from {} to {} for feed with id: {}", start, end, id);

		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		
		String hql = "delete " + entityName + " where " + TIME_COLUMN + " <= :start and " +
							TIME_COLUMN + " >= :end";
		Query<?> query = session.createQuery(hql).setParameter("start", start).setParameter("end", end);
		int deletedRecs = query.executeUpdate();
		if (deletedRecs == 0) {
			logger.debug("No values found from {} to {} for feed with id: {}", start, end, id);
		}
		
		t.commit();
		session.close();		
	}
	
	@Override
	public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
		logger.debug("Requesting to fetch data from {} to {} for feed with id: {}", start, end, id);

		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		
		//TODO implement interval to get only data with start-i*interval 
		Query<?> query = session.createQuery("from " + entityName + 
				" where " + TIME_COLUMN + " <= " + start + " and " +
							TIME_COLUMN + " >= " + end);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map> list = (List<Map>) query.list();
		LinkedList<Timevalue> timeValuesList = new LinkedList<Timevalue>();
		for (@SuppressWarnings("rawtypes") Map map : list) {
			Long time = (long)map.get(TIME_COLUMN);
			double value = getValue(map.get(VALUE_COLUMN));
			Timevalue timeValue = new Timevalue(time, value);
			timeValuesList.add(timeValue);
		}
		
		t.commit();
		session.close();		
		
		return timeValuesList;
	}

	@Override
	public Timevalue getLatestTimevalue() throws EmoncmsException {
		logger.debug("Requesting to get latest timevalue for feed with id: {}", id);

		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		
		Query<?> query = session.createQuery("from " + entityName + " order by " + TIME_COLUMN);
		query.setMaxResults(1);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map> list = (List<Map>) query.list();
		Timevalue timeValue = null;
		if (! list.isEmpty()) {
			@SuppressWarnings("rawtypes")
			Map firstMap = list.get(0);
			Long time = (long)firstMap.get(TIME_COLUMN);
			double value = getValue(firstMap.get(VALUE_COLUMN));
			timeValue = new Timevalue(time, value);
		}
		t.commit();
		session.close();		

		return timeValue;
	}
	
	protected Double getValue(Object val) {
		Double value = null;
		if (val instanceof Boolean) value = new Double(((Boolean)val) == false ? 0 : 1);
		else if (val instanceof Byte) value = new Double((Byte)val);
		else if (val instanceof Double) value = new Double((Double)val);
		else if (val instanceof Float) value = new Double((Float)val);
		else if (val instanceof Integer) value = new Double((Integer)val);
		else if (val instanceof Long) value = new Double((Long)val);
		else if (val instanceof Short) value = new Double((Short)val);
		else if (val instanceof String) value = new Double((String)val);
		return value;
	}

	@Override
	public Double getLatestValue() throws EmoncmsException {
		logger.debug("Requesting to get latest value for feed with id: {}", id);
		
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		
		Query<?> query = session.createQuery("from " + entityName + " order by " + TIME_COLUMN);
		query.setMaxResults(1);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map> list = (List<Map>) query.list();
		Double value = null;
		if (! list.isEmpty()) {
			@SuppressWarnings("rawtypes")
			Map firstMap = list.get(0);
			value = getValue(firstMap.get(VALUE_COLUMN));
		}
		t.commit();
		session.close();
		
		return value;
	}

	@Override
	public void insertData(Timevalue timevalue) {
		logger.debug("Requesting to insert value: {}, time: {} for feed with id: {}",
				timevalue.getValue(), timevalue.getTime(), id);

		setData(timevalue.getTime(), timevalue.getValue());
	}

	@Override
	public void updateData(Timevalue timevalue) throws EmoncmsException {
		logger.debug("Requesting to update value: {} at time: {} for feed with id: {}",
				timevalue.getValue(), timevalue.getTime(), id);

		setData(timevalue.getTime(), timevalue.getValue());
	}

	protected void setData(Long time, double value) {
		Session session = factory.openSession();
		Transaction t = session.beginTransaction();
		
    	// Build Map
        Map<String, Object> map = buildMap(time, value);
    	session.save(entityName, map);
		
		t.commit();
		session.close();		
	}

	protected Map<String, Object> buildMap(Long time, double value) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(TIME_COLUMN, time);
		map.put(TIME_COLUMN, new Double(value));
		return map;
	}
}
