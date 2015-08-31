package de.isc.emon.cms;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.isc.emon.cms.config.EmoncmsConfig;
import de.isc.emon.cms.config.EmoncmsProcessConfig;


public class AccessEmonCMS {
	private static final Logger logger = LoggerFactory.getLogger(AccessEmonCMS.class);

	public static void main(String[] args) {
		EmoncmsConfig config = null;
		
		String configFileName = System.getProperty("de.isc.emon.cms.configfile");
		if (configFileName == null) {
			configFileName = "emoncms-config.xml";
		}
		File configFile = new File(configFileName);
		
		try {
			config = configureEmoncms(configFile);
			
		} catch (FileNotFoundException e) {
			logger.info("No configuration file found." + 
					configFile.getAbsolutePath() + configFile.getName());
		} catch (EmoncmsException e) {
			logger.error("Error parsing config file: " + e.getMessage());
		}
		
		// TODO do stuff
		EmonCMS cms = new EmonCMS(config);
		try {
			cms.createInputProcess(1, "Test1", "", "");
		} catch (EmoncmsException e) {
		}
	}

	private static EmoncmsConfig configureEmoncms(File configFile) throws EmoncmsException, FileNotFoundException {
		if (configFile == null) {
			throw new NullPointerException("configFileName is null or the empty string.");
		}

		if (!configFile.exists()) {
			throw new FileNotFoundException();
		}

		DocumentBuilderFactory docBFac = DocumentBuilderFactory.newInstance();
		docBFac.setIgnoringComments(true);

		Document doc;
		try {
			doc = docBFac.newDocumentBuilder().parse(configFile);
		} catch (Exception e) {
			throw new EmoncmsException(e);
		}
		
		Node rootNode = doc.getDocumentElement();
		if (!rootNode.getNodeName().equals("configuration")) {
			throw new EmoncmsException("root node in configuration is not of type \"configuration\"");
		}
		
		NodeList configChildren =  rootNode.getChildNodes();
		for (int i = 0; i < configChildren.getLength(); i++) {
			Node childNode = configChildren.item(i);
			String childName = childNode.getNodeName();
			if (childName.equals("#text")) {
				continue;
			}
			else if (childName.equals("settings")) {
				Boolean shell = null;;
				String api = null;
				String location = null;
				Map<String, EmoncmsProcessConfig> processes = null;
				
				NodeList settingsChildren = childNode.getChildNodes();
				for (int j = 0; j < settingsChildren.getLength(); j++) {
					Node settingsChildNode = settingsChildren.item(j);
					String settingsChildName = settingsChildNode.getNodeName();
					if (settingsChildName.equals("#text")) {
						continue;
					}
					else if (settingsChildName.equals("api")) {
						api = settingsChildNode.getTextContent();
					}
					else if (settingsChildName.equals("location")) {
						shell = true;
						location = settingsChildNode.getTextContent();
					}
					else if (settingsChildName.equals("url")) {
						shell = false;
						location = settingsChildNode.getTextContent();
					}
					else if (settingsChildName.equals("processList")) {
						processes = configureProcessList(settingsChildNode);
					}
					else {
						throw new EmoncmsException("Found unknown tag in node settings:" + settingsChildName);
					}
				}
				if (shell == null || api == null || location == null) {
					throw new EmoncmsException("Emoncms configurations incomplete");
				}
				else if (processes == null) {
					processes = new HashMap<String, EmoncmsProcessConfig>();
				}
				EmoncmsConfig config = new EmoncmsConfig(shell, api, location, processes);
				
				if (!config.containsProcess("Log to feed")) {
					Map<String, String> logProcessArguments = new HashMap<String, String>();
					logProcessArguments.put("datatype", "1");
					logProcessArguments.put("engine", "2");
					config.addProcess(new EmoncmsProcessConfig("Log to feed", 1, logProcessArguments));
					
					if (logger.isTraceEnabled()) {
						logger.trace("Default \"Log to feed\" process has been configured");
					}
				}
				
				return config;
			}
			else {
				throw new EmoncmsException("Found unknown tag:" + childName);
			}
		}

		throw new EmoncmsException("Emoncms settings not configured:");
	}

    private static Map<String, EmoncmsProcessConfig> configureProcessList(Node processListNode) throws EmoncmsException {
    	Map<String, EmoncmsProcessConfig> processByKey = new HashMap<String, EmoncmsProcessConfig>();
    	
		NodeList processListChildren = processListNode.getChildNodes();
		for (int i = 0; i < processListChildren.getLength(); i++) {
			Node childNode = processListChildren.item(i);
			String childName = childNode.getNodeName();
			if (childName.equals("#text")) {
				continue;
			}
			else if (childName.equals("process")) {
				EmoncmsProcessConfig process = new EmoncmsProcessConfig(childNode);
				processByKey.put(process.getKey(), process);
				
				if (logger.isTraceEnabled()) {
					logger.trace("Process \"{}\" has been configured", process.getKey());
				}
			}
			else {
				throw new EmoncmsException("Found unknown tag in node processList:" + childName);
			}
		}
		
		return processByKey;
    }
}
