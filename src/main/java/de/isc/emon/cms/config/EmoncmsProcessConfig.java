package de.isc.emon.cms.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.isc.emon.cms.EmoncmsException;


public class EmoncmsProcessConfig {

	private final String key;
	private Integer id;

	private final Map<String, String> args;

	
	public EmoncmsProcessConfig(Node processConfigNode) throws EmoncmsException {
		NamedNodeMap attributes = processConfigNode.getAttributes();
		Node nameAttribute = attributes.getNamedItem("key");
		if (nameAttribute == null) {
			throw new EmoncmsException("Process has no key attribute");
		}
		key = nameAttribute.getTextContent();
		if (key.isEmpty()) {
			throw new EmoncmsException("The process key may not be empty");
		}
		
		args = new LinkedHashMap<String, String>();
		NodeList connectionChildren = processConfigNode.getChildNodes();
		try {
			for (int j = 0; j < connectionChildren.getLength(); j++) {
				Node childNode = connectionChildren.item(j);
				String childName = childNode.getNodeName();

				if (childName.equals("#text")) {
					continue;
				}
				else if(childName.equals("processId")) {
					id = Integer.valueOf(childNode.getTextContent());
				}
				else {
					args.put(childName, childNode.getTextContent());
				}
			}
		} catch (IllegalArgumentException e) {
			throw new EmoncmsException(e);
		}
		
		if (id == null) {
			throw new EmoncmsException("Process \"" + key + "\" has no id defined");
		}
	}
	
	public EmoncmsProcessConfig(String key, Integer id, Map<String, String> args) throws EmoncmsException {
		if (key == null || key.isEmpty()) {
			throw new EmoncmsException("Process has no key attribute");
		}
		this.key = key;
		
		if (id == null) {
			throw new EmoncmsException("Process \"" + key + "\" has no id defined");
		}
		this.id = id;
		
		this.args = args;
	}
	
	public String getKey() {
		return key;
	}
	
	public int getId() {
		return id;
	}
	
	public Map<String, String> getArguments() {
		return args;
	}
}
