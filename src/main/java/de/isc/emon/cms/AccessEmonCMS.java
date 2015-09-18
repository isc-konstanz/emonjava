package de.isc.emon.cms;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.http.EmoncmsHTTPConnection;
import de.isc.emon.cms.connection.php.EmoncmsPHPConnection;
import de.isc.emon.cms.data.Input;
import de.isc.emon.cms.data.Process;


public class AccessEmonCMS {
	private static final Logger logger = LoggerFactory.getLogger(AccessEmonCMS.class);

	
	public static void main(String[] args) {
		EmoncmsConnection connection = null;
		
		String configFileName = System.getProperty("de.isc.emon.cms.configfile");
		if (configFileName == null) {
			configFileName = "emoncms-config.xml";
		}
		File configFile = new File(configFileName);
		
		try {
			connection = configureEmoncms(configFile);
			
		} catch (FileNotFoundException e) {
			logger.info("No configuration file found." + 
					configFile.getAbsolutePath() + configFile.getName());
		} catch (EmoncmsException e) {
			logger.error("Error parsing config file: " + e.getMessage());
		}
		
		//TODO do stuff with cms
		EmonCMS cms = new EmonCMS(connection);
		try {
			List<Input> inputs = cms.getInputList(1);
			int inputId = inputs.get(0).getId();
			
			LinkedList<Process> newProcesses = new LinkedList<Process>();
			Process processAdd = new Process("offset", String.valueOf(10));
			newProcesses.add(processAdd);
			cms.setInputProcessList(inputId, newProcesses);
			
			LinkedList<Process> processes = cms.getInputProcessList(inputId);
			logger.info(processes.toString());
			
//			cms.resetInputProcessList(inputId);
			
		} catch (EmoncmsException e) {
			logger.error("Error while posting emoncms request: " + e.getMessage());
		}
	}

	private static EmoncmsConnection configureEmoncms(File configFile) throws EmoncmsException, FileNotFoundException {
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
				String apiKey = null;
				String address = null;
				
				NodeList settingsChildren = childNode.getChildNodes();
				for (int j = 0; j < settingsChildren.getLength(); j++) {
					Node settingsChildNode = settingsChildren.item(j);
					String settingsChildName = settingsChildNode.getNodeName();
					if (settingsChildName.equals("#text")) {
						continue;
					}
					else if (settingsChildName.equals("api")) {
						apiKey = settingsChildNode.getTextContent();
					}
					else if (settingsChildName.equals("directory")) {
						shell = true;
						address = settingsChildNode.getTextContent();
					}
					else if (settingsChildName.equals("url")) {
						shell = false;
						address = settingsChildNode.getTextContent();
					}
					else throw new EmoncmsException("Found unknown tag in settings:" + settingsChildName);
				}
				if (shell == null || apiKey == null || address == null) {
					throw new EmoncmsException("Emoncms configurations incomplete");
				}
				
				if (!address.endsWith("/")) {
					address = address.concat("/");
				}
		    	if (shell) {
					return new EmoncmsPHPConnection(address, apiKey);
		    	}
		    	else {
					return new EmoncmsHTTPConnection(address, apiKey);
		    	}
			}
		}
		throw new EmoncmsException("Emoncms settings not configured");
	}
}
