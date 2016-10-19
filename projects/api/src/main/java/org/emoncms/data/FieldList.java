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
