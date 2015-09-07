package de.isc.emon.cms;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.isc.emon.cms.config.EmoncmsConfig;


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
			config = loadConfigFile(configFile);
			
		} catch (FileNotFoundException e) {
			logger.info("No configuration file found." + 
					configFile.getAbsolutePath() + configFile.getName());
		} catch (EmoncmsException e) {
			logger.error("Error parsing config file: " + e.getMessage());
		}
		
		//TODO do stuff with cms
		EmonCMS cms = new EmonCMS(config);
//		try {
//			String inputName = "test";
//			cms.postInputData(inputName, 1, (double) 0, null);
//			List<EmoncmsResponse> inputs = cms.listInputs();
//			int inputId = Integer.valueOf(inputs.get(0).get("id"));
//			EmoncmsResponse feed = cms.createFeed(inputName + "_feed", "REALTIME", "PHPTIMESERIES", null);
//			EmoncmsResponse feed = cms.createFeed(inputName + "_feed_kWh", "REALTIME", "PHPTIMESERIES", null);
//			List<EmoncmsResponse> feeds = cms.listFeeds();
//			int feedIdLog = Integer.valueOf(cms.getFeedId(inputName + "_feed").get("id"));
//			int feedIdkWh = Integer.valueOf(cms.getFeedId(inputName + "_feed_kWh").get("id"));
//			cms.addInputProcess(inputId, "log_to_feed", String.valueOf(feedIdLog));
//			long time = System.currentTimeMillis();
//			cms.postInputData(inputName, 1, (double) 100, time);
//			EmoncmsResponse value = cms.getFeedValue(feedIdLog);
//			List<EmoncmsResponse> values = cms.getFeedData(feedIdLog, time - 1000*60*15, time, 15);
//			cms.getFeedField(feedIdLog, "engine");
//			cms.setFeedField(feedIdLog, "engine", "2");
//			cms.addInputProcess(inputId, "kwh_to_power", String.valueOf(feedIdkWh));
//			cms.moveInputProcess(inputId, "log_to_feed", 1);
//			cms.deleteInputProcess(inputId, "kwh_to_power");
//			cms.resetInputProcess(inputId);
//			cms.deleteFeed(feedIdLog);
//			cms.deleteFeed(feedIdkWh);
//			cms.deleteInput(inputId);
//		} catch (EmoncmsException e) {
//			logger.error("Error while posting emoncms request: " + e.getMessage());
//		}
	}

	private static EmoncmsConfig loadConfigFile(File configFile) throws EmoncmsException, FileNotFoundException {
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
		
		EmoncmsConfig config = configureSettings(rootNode);
		configureEmoncms(rootNode, config);
		
		return config;
	}

    private static EmoncmsConfig configureSettings(Node rootNode) throws EmoncmsException {
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
				String address = null;
				
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
						address = settingsChildNode.getTextContent();
					}
					else if (settingsChildName.equals("url")) {
						shell = false;
						address = settingsChildNode.getTextContent();
					}
					else throw new EmoncmsException("Found unknown tag in settings:" + settingsChildName);
				}
				if (shell == null || api == null || address == null) {
					throw new EmoncmsException("Emoncms configurations incomplete");
				}
				
				return new EmoncmsConfig(shell, api, address);
			}
		}
		throw new EmoncmsException("Emoncms settings not configured");
    }

    private static void configureEmoncms(Node rootNode, EmoncmsConfig config) throws EmoncmsException {
		NodeList configChildren =  rootNode.getChildNodes();
		for (int i = 0; i < configChildren.getLength(); i++) {
			Node childNode = configChildren.item(i);
			String childName = childNode.getNodeName();
			if (childName.equals("#text")) {
				continue;
			}
			else if (childName.equals("process")) {
				config.addProcess(getNodeKey(childNode), getValueId(childNode));
			}
			else if (childName.equals("datatype")) {
				config.addDataType(getNodeKey(childNode), getValueId(childNode));
			}
			else if (childName.equals("engine")) {
				config.addEngine(getNodeKey(childNode), getValueId(childNode));
			}
		}

		if (!config.containsProcess("log_to_feed")) {
			config.addProcess("log_to_feed", 1);
			
			if (logger.isTraceEnabled()) {
				logger.trace("Default process \"log_to_feed\" has been configured");
			}
		}
		if (!config.containsDataType("REALTIME")) {
			config.addDataType("REALTIME", 1);
			
			if (logger.isTraceEnabled()) {
				logger.trace("Default data type \"REALTIME\" has been configured");
			}
		}
		if (!config.containsEngine("PHPFINA")) {
			config.addEngine("PHPFINA", 1);

			if (logger.isTraceEnabled()) {
				logger.trace("Default engine \"PHPFINA\" has been configured");
			}
		}
		if (!config.containsEngine("PHPTIMESERIES")) {
			config.addEngine("PHPTIMESERIES", 2);

			if (logger.isTraceEnabled()) {
				logger.trace("Default engine \"PHPTIMESERIES\" has been configured");
			}
		}
    }
    
    private static String getNodeKey(Node node) throws EmoncmsException {
		NamedNodeMap attributes = node.getAttributes();
		Node nameAttribute = attributes.getNamedItem("key");
		if (nameAttribute == null) {
			throw new EmoncmsException("The node has no key attribute");
		}
		String key = nameAttribute.getTextContent();
		if (key.isEmpty()) {
			throw new EmoncmsException("The key attribute may not be empty");
		}
		
		return key;
    }
    
    private static int getValueId(Node node) throws EmoncmsException {
    	Integer id = null;
		NodeList nodeChildren = node.getChildNodes();
		try {
			for (int j = 0; j < nodeChildren.getLength(); j++) {
				Node childNode = nodeChildren.item(j);
				String childName = childNode.getNodeName();

				if (childName.equals("#text")) {
					continue;
				}
				else if(childName.equals("id")) {
					id = Integer.valueOf(childNode.getTextContent());
				}
				else throw new EmoncmsException("Found unknown tag:" + childName);
			}
		} catch (IllegalArgumentException e) {
			throw new EmoncmsException(e);
		}
		
		if (id == null) {
			throw new EmoncmsException("The node has no id defined");
		}
		
		return id;
    }
}
