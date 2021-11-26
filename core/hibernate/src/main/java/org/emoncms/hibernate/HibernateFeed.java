/* 
 * Copyright 2016-2021 ISC Konstanz
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
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.BasicType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateFeed implements Feed {
	private static final Logger logger = LoggerFactory.getLogger(HibernateFeed.class);

	protected static final String VALUE_COLUMN = "value"; 
	protected static final String TIME_COLUMN = "timestamp"; 

	protected static final String CONFIG_PATH = "hibernate.configPath";
	protected static final String CONFIG_PATH_DEFAULT = "conf/";
	protected static final String MAPPING_TEMPLATE = "hibernate.record.template";
	protected static final String MAPPING_TEMPLATE_DEFAULT = "hibernate.record.hbm.xml";

	protected static String template = null;

	protected String name;
	protected String type;

	protected boolean initialized = false;

	protected HibernateCallbacks callbacks;
	
	public HibernateFeed(HibernateCallbacks callbacks, String table) {
		this.callbacks = callbacks;
		this.name = table;
		
		if (template == null) {
			loadMappingTemplate();
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean isInitialized) {
		this.initialized = isInitialized;
	}

	protected synchronized void  loadMappingTemplate() {
		String configPath = System.getProperty(CONFIG_PATH, CONFIG_PATH_DEFAULT);
		String templateFile = System.getProperty(MAPPING_TEMPLATE, MAPPING_TEMPLATE_DEFAULT);
		while (template == null) {
			try {
				template = new String(Files.readAllBytes(Paths.get(configPath + templateFile)));
			} 
			catch (IOException e) {
				logger.error(e.getMessage());
				//throw new RuntimeException(e);
			}
		}
	}

	public void setValueType(String type) {
		this.type = type;
	}

	public boolean containsUserType(String type) {
		return template.contains(type); 
	}

	public String getEntityName() {
		return name;
	}
	
	@Override
	public int getId() {
		throw new UnsupportedOperationException("SqlFeed has no id");
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
	public void deleteData(long time) throws EmoncmsException {
		logger.debug("Requesting to delete value at time: {} for feed with entity: {}", time, name);
		
		if (!initialized) return;
		Session session = callbacks.getSession();
		Transaction transaction = session.beginTransaction();
		
		String hql = "delete " + name + " where " + TIME_COLUMN + " = :time";
		Query<?> query = session.createQuery(hql).setParameter("time", time);
		int deletedRecs = query.executeUpdate();
		if (deletedRecs == 0) {
			logger.debug("No value found at time: {} for feed with entity: {}", time, name);
		}
		
		transaction.commit();
		session.close();		
	}

	@Override
	public void deleteDataRange(long start, long end) throws EmoncmsException {
		logger.debug("Requesting to delete values from {} to {} for feed with entity: {}", start, end, name);

		if (!initialized) return;
		Session session = callbacks.getSession();
		Transaction transaction = session.beginTransaction();
		
		String hql = "delete " + name + " where " + TIME_COLUMN + " <= :start and " +
							TIME_COLUMN + " >= :end";
		Query<?> query = session.createQuery(hql).setParameter("start", start).setParameter("end", end);
		int deletedRecs = query.executeUpdate();
		if (deletedRecs == 0) {
			logger.debug("No values found from {} to {} for feed with entity: {}", start, end, name);
		}
		
		transaction.commit();
		session.close();		
	}
	
	@Override
	public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
		logger.debug("Requesting to fetch data from {} to {} for feed with entity: {}", start, end, name);

		if (!initialized) return new LinkedList<Timevalue>();
		Session session = callbacks.getSession();
		Transaction transaction = session.beginTransaction();
		
		//TODO implement interval to get only data with start-i*interval 
		Query<?> query = session.createQuery("from " + name + 
				" where " + TIME_COLUMN + " <= :start and " +
							TIME_COLUMN + " >= :end");
		BasicType userType = null;
		if (containsUserType(HibernateClient.SCALE_INTEGER_TYPE)) {
			userType = ScaleIntegerType.INSTANCE;
			query.setParameter("start", start, userType)
				 .setParameter("end", end, userType);
		}
		else {
			query.setParameter("start", start)
				 .setParameter("end", end);
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map> list = (List<Map>) query.list();
		LinkedList<Timevalue> timeValuesList = new LinkedList<Timevalue>();
		for (@SuppressWarnings("rawtypes") Map map : list) {
			Long time = Long.valueOf(map.get(TIME_COLUMN).toString());
			double value = getValue(map.get(VALUE_COLUMN));
			Timevalue timeValue = new Timevalue(time, value);
			timeValuesList.add(timeValue);
		}
		
		transaction.commit();
		session.close();		
		
		return timeValuesList;
	}

	@Override
	public Timevalue getLatestTimevalue() throws EmoncmsException {
		logger.debug("Requesting to get latest timevalue for feed with entity: {}", name);

		if (!initialized) return null;
		Session session = callbacks.getSession();
		Transaction transaction = session.beginTransaction();
		
		Query<?> query = session.createQuery("from " + name + " order by " + TIME_COLUMN);
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
		transaction.commit();
		session.close();		

		return timeValue;
	}

	protected Double getValue(Object val) {
		Double value = null;
		if (val instanceof String) {
			value = Double.parseDouble((String) val);
		}
		else if (val instanceof Boolean) {
			value = ((Boolean) val) ? 1.0 : 0.0;
		}
		else {
			value = (Double) val;
		}
		return value;
	}

	@Override
	public Double getLatestValue() throws EmoncmsException {
		logger.debug("Requesting to get latest value for feed with entity: {}", name);
		
		if (!initialized) return null;
		Session session = callbacks.getSession();
		Transaction transaction = session.beginTransaction();
		
		Query<?> query = session.createQuery("from " + name + " order by " + TIME_COLUMN);
		query.setMaxResults(1);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map> list = (List<Map>) query.list();
		Double value = null;
		if (! list.isEmpty()) {
			@SuppressWarnings("rawtypes")
			Map firstMap = list.get(0);
			value = getValue(firstMap.get(VALUE_COLUMN));
		}
		transaction.commit();
		session.close();
		
		return value;
	}

	@Override
	public void insertData(Timevalue timevalue) throws EmoncmsException {
		logger.debug("Requesting to insert value: {}, time: {} for feed with entity: {}",
				timevalue.getValue(), timevalue.getTime(), name);

		setData(timevalue.getTime(), timevalue.getValue());
	}

	@Override
	public void updateData(Timevalue timevalue) throws EmoncmsException {
		logger.debug("Requesting to update value: {} at time: {} for feed with entity: {}",
				timevalue.getValue(), timevalue.getTime(), name);

		setData(timevalue.getTime(), timevalue.getValue());
	}

	protected void setData(Long time, double value) throws EmoncmsException {
		if (!initialized) return;
		Session session = callbacks.getSession();
		Transaction transaction = session.beginTransaction();
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(TIME_COLUMN, time);
		switch (type) {
			case "BOOLEAN":
				boolean b = (value == 0.0?false:true);
				map.put(VALUE_COLUMN, b);
				break;
			case "BYTE":
				map.put(VALUE_COLUMN, ((Double)value).byteValue());
				break;
			case "DOUBLE":
				map.put(VALUE_COLUMN, value);
				break;
			case "FLOAT":
				map.put(VALUE_COLUMN, ((Double)value).floatValue());
				break;
			case "INTEGER":
				map.put(VALUE_COLUMN, ((Double)value).intValue());
				break;
			case "LONG":
				map.put(VALUE_COLUMN, ((Double)value).longValue());
				break;
			case "SHORT":
				map.put(VALUE_COLUMN, ((Double)value).shortValue());
				break;
			case "STRING":
				map.put(VALUE_COLUMN, ((Double)value).toString());
				break;
			default:
				map.put(VALUE_COLUMN, ((Double)value).toString());
				break;
		}
		session.saveOrUpdate(name, map);
		
		transaction.commit();
		session.close();		
	}

	protected InputStream getMapping() {
		String mapping = template.replace("entity-name=\"entity\"", "entity-name=\""+name+"\"");
		if (type != null) {
			switch (type) {
			case "BOOLEAN":
				mapping = mapping.replace("java.lang.Object", "java.lang.Boolean");
				break;
			case "BYTE":
				mapping = mapping.replace("java.lang.Object", "java.lang.Byte");
				break;
			case "SHORT":
				mapping = mapping.replace("java.lang.Object", "java.lang.Short");
				break;
			case "INTEGER":
				mapping = mapping.replace("java.lang.Object", "java.lang.Integer");
				break;
			case "LONG":
				mapping = mapping.replace("java.lang.Object", "java.lang.Long");
				break;
			case "FLOAT":
				mapping = mapping.replace("java.lang.Object", "java.lang.Float");
				break;
			case "DOUBLE":
				mapping = mapping.replace("java.lang.Object", "java.lang.Double");
				break;
			default:
				mapping = mapping.replace("java.lang.Object", "java.lang.String");
				break;
			}
		}
		else {
			// Use hard coded float if type is not specified.
			mapping = mapping.replace("java.lang.Object", "java.lang.Float");
			type = "FLOAT";
		}
		return new ByteArrayInputStream(StandardCharsets.UTF_16.encode(mapping).array());		
	}

}
