/* 
 * Copyright 2016-20 ISC Konstanz
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
import java.util.Map;

public class HibernateTimeSeries {

	protected static final String VALUE_COLUMN = "value"; 
	protected static final String TIME_COLUMN = "timestamp"; 
	protected static final String FLAG_COLUMN = "code"; 
	
	protected static final String CONFIG_PATH = "hibernate.configPath";
	protected static final String DEFAULT_CONFIG_PATH = "conf/";
	protected static final String MAPPING_TEMPLATE_FILE = "hibernate.record.template";
	protected static final String DEFAULT_MAPPING_TEMPLATE = "hibernate.record.hbm.xml";

	protected static String MAPPING_TEMPLATE = null;

	protected final String id;
	protected final String type;
	
	public HibernateTimeSeries(String id, String type) {
		this.id = id;
		this.type = type;

		if (MAPPING_TEMPLATE == null) {
			loadMappingTemplate();
		}
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
	
	public String getId() {
		return id;
	}
	
	public InputStream createMappingInputStream() {
		String mapping = MAPPING_TEMPLATE.replace("entity-name=\"entity\"", "entity-name=\""+id+"\"");
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
		return new ByteArrayInputStream(StandardCharsets.UTF_16.encode(mapping).array());		
		
	}
	
	protected Map<String, Object> buildMap(long timestamp, Object value, byte code) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(TIME_COLUMN, timestamp);
        switch (type) {
		case "BOOLEAN":
			map.put(VALUE_COLUMN, (Boolean) value);
			break;
		case "BYTE":
			map.put(VALUE_COLUMN, (Byte) value);
			break;
		case "SHORT":
			map.put(VALUE_COLUMN, (Short) value);
			break;
		case "INTEGER":
			map.put(VALUE_COLUMN, (Integer) value);
			break;
		case "LONG":
			map.put(VALUE_COLUMN, (Long) value);
			break;
		case "FLOAT":
			map.put(VALUE_COLUMN, (Float) value);
			break;
		case "DOUBLE":
			map.put(VALUE_COLUMN, (Double) value);
			break;
		default:
			map.put(VALUE_COLUMN, String.valueOf(value));
			break;
        }
        map.put(FLAG_COLUMN, code);
		return map;
	}
	
 }
