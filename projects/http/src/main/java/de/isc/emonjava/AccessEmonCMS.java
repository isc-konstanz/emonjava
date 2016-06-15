/*
 * Copyright 2016 ISC Konstanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.isc.emonjava;

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

import de.isc.emonjava.com.EmoncmsCommunication;
import de.isc.emonjava.com.http.EmoncmsHTTP;
import de.isc.emonjava.data.Feed;
import de.isc.emonjava.data.Input;
import de.isc.emonjava.data.Process;
import de.isc.emonjava.data.Value;


public class AccessEmonCMS {
	private static final Logger logger = LoggerFactory.getLogger(AccessEmonCMS.class);

	
	public static void main(String[] args) {
		EmoncmsCommunication connection = null;
		
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
			String inputName = "test";
//			cms.postInputData(inputName, 1, new Value(1));
			List<Input> inputs = cms.getInputList();
			int inputId = 0;
			for (Input i : inputs) {
				if (i.getName().equals(inputName)) inputId = i.getId();
			}
			
			String feedName = inputName + "_log";
//			DataType datatype = new DataType("realtime");
//			Engine engine = new Engine("phpfina");
//			Field options = new Field("interval", "60");
//			int feedId = cms.createFeed(feedName, "device_name", datatype, engine, options);
//			Field field = new Field("tag", "device_test");
//			cms.setFeedField(feedId, field);
			List<Feed> feeds = cms.getFeedList();
			int feedId = 0;
			for (Feed f : feeds) {
				if (f.getName().equals(feedName)) feedId = f.getId();
			}
			
			LinkedList<Process> newProcesses = new LinkedList<Process>();
			Process processAdd = new Process("offset", String.valueOf(100));
			newProcesses.add(processAdd);
			Process processLog = new Process("log_to_feed", String.valueOf(feedId));
			newProcesses.add(processLog);
			cms.setInputProcessList(inputId, newProcesses);
			
//			LinkedList<Process> processes = cms.getInputProcessList(inputId);
//			cms.resetInputProcessList(inputId);
			
			int i = 0;
			while(true) {
				cms.writeInputData(inputName, 1, new Value(i));
				i++;
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
			
		} catch (EmoncmsException e) {
			logger.error("Error while posting emoncms request: " + e.getMessage());
		}
	}

	private static EmoncmsCommunication configureEmoncms(File configFile) throws EmoncmsException, FileNotFoundException {
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
					else if (settingsChildName.equals("address")) {
						address = settingsChildNode.getTextContent();
					}
					else throw new EmoncmsException("Found unknown tag in settings:" + settingsChildName);
				}
				if (apiKey == null || address == null) {
					throw new EmoncmsException("Emoncms configurations incomplete");
				}
				
				if (!address.endsWith("/")) {
					address = address.concat("/");
				}
				
				return new EmoncmsHTTP(address, apiKey);
			}
		}
		throw new EmoncmsException("Emoncms settings not configured");
	}
}
