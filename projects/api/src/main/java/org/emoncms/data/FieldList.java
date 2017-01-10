/*
 * Copyright 2016-17 ISC Konstanz
 *
 * This file is part of emonjava.
 * For more information visit https://bitbucket.org/isc-konstanz/emonjava
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
 *
 */
package org.emoncms.data;

import java.util.HashMap;
import java.util.Map;


public class FieldList extends HashMap<Field, String> {
	private static final long serialVersionUID = -3326186957769047676L;

	Map<String, String> str = new HashMap<String, String>();

	public FieldList(Field field, String value) {
		super(1);
		put(field, value);
	}

	@Override
	public String put(Field field, String value) {
		str.put(field.getValue(), value);
		return super.put(field, value);
	}

	@Override
	public void putAll(Map<? extends Field, ? extends String> map) {
		Map<String, String> fields = new HashMap<String, String>(map.size());
		for (Map.Entry<? extends Field, ? extends String> field : map.entrySet()) {
			fields.put(field.getKey().getValue(), field.getValue());
		}
		str.putAll(fields);
		super.putAll(map);
	}

	public Map<String, String> getValues() {
		return str;
	}
}
