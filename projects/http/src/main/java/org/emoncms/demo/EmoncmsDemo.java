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
package org.emoncms.demo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.emoncms.Emoncms;
import org.emoncms.Feed;
import org.emoncms.HttpEmoncmsFactory;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.data.DataList;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Options;
import org.emoncms.data.Process;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmoncmsDemo {
	private static final Logger logger = LoggerFactory.getLogger(EmoncmsDemo.class);

    private static final String URL = "YOUR_ADDRESS";
    private static final String API_KEY = "YOUR_API_KEY";


	public static void main(String[] args) {
		
		Emoncms cms = HttpEmoncmsFactory.newAuthenticatedHttpEmoncmsConnection(URL, API_KEY);
		try {
			cms.start();
            
            // Post some initial values to 3 inputs of 2 nodes
            String node1 = "Test";
            String input1Name = "test";
            cms.post(node1, input1Name, new Timevalue(0));
            String input2Name = "foo";
            cms.post(node1, input2Name, new Timevalue(0));

            String node2 = "Foo";
            String input3Name = "foo";
            cms.post(node2, input3Name, new Timevalue(0));
            
            List<Input> inputs = cms.getInputList();
            logger.info("Input list size: {}", inputs.size());
            
            // Post bulk data to 3 inputs of 2 nodes named automatically numerically
            DataList data = new DataList();
            data.add(1, new Timevalue(1));
            data.add(1, new Timevalue(1));
            data.add(2, new Timevalue(1));
            cms.post(data);
            
            // Post bulk data for 2 inputs, 1min in the future
            List<Namevalue> values = new ArrayList<Namevalue>();
            values.add(new Namevalue(input1Name, 1));
            values.add(new Namevalue(input2Name, 1));
            cms.post(node1, System.currentTimeMillis()+60*1000, values);

            inputs = cms.getInputList(node1);
            logger.info("Inputs of node \"{}\" size: {}", node1, inputs.size());

            // Load 2 feeds and create and configure them, if not existing
            List<Feed> feeds = cms.loadFeedList();
            Feed feed1 = null;
            Feed feed2 = null;
            for (Feed feed : feeds) {
                if (feed.getName().equals(input1Name)) {
                    feed1 = feed.clear();
                    logger.info("Feed \"{}\" found with id: {}", input1Name, feed1.getId());
                    
                }
                if (feed.getName().equals(input2Name)) {
                    feed2 = feed.clear();
                    logger.info("Feed \"{}\" found with id: {}", input2Name, feed2.getId());
                }
            }
            
            if (feed1 == null) {
                Options options = new Options();
                options.setInterval(5);
                int feed1Id = cms.newFeed(input1Name, node1, Datatype.REALTIME, Engine.PHPFINA, options);
                logger.info("Feed created with id: {}", feed1Id);
                
                feed1 = cms.getFeed(feed1Id);
            }
            
            if (feed2 == null) {
                Options options = new Options();
                options.setInterval(60);
                int feed2Id = cms.newFeed(input2Name, node1, Datatype.REALTIME, Engine.PHPFINA, options);
                logger.info("Feed created with id: {}", feed2Id);

                feed2 = cms.getFeed(feed2Id);
            }

            // Set processList for 2 inputs to the newly created feeds
            Input input1 = cms.getInput(node1, input1Name);
            try {
                input1.setDescription("Test input");
                
                ProcessList processList1 = new ProcessList();
                processList1.add(Process.OFFSET, 10);
                processList1.add(Process.LOG_TO_FEED, feed1.getId());
                input1.setProcessList(processList1);
            } catch (EmoncmsException e) {
                logger.warn("Input \"{}\" configuration already updated", input1.getName());
            }
            
            // Get the processList of input 1
            ProcessList processList1 = input1.getProcessList();
            logger.info("Process list of input \"{}\":  {}", input1.getName(), processList1);
            
            Input input2 = cms.getInput(node1, input2Name);
            try {
                ProcessList processList2 = new ProcessList(Process.LOG_TO_FEED, feed2.getId());
                input2.setProcessList(processList2);
                
            } catch (EmoncmsException e) {
                logger.warn("Input \"{}\" configuration already updated", input2.getName());
            }
            
            // Remove all inputs without processes
            cms.cleanInputList();
            
            // Clean and remove input 2
            input2.resetProcessList();
            input2.delete();
            
            // Post several values and check different methods of retrieving them
            long start = System.currentTimeMillis() - 1000;
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(5*1000);
                } catch (InterruptedException e) {
                }
                
                input1.post(new Timevalue(i));
            }
            long end = System.currentTimeMillis() + 1000;
            
            logger.info("Last value for feed \"{}\": {}", feed1.getName(), feed1.getLatestValue());
            logger.info("Last timevalue for feed \"{}\": {}", feed1.getName(), feed1.getLatestTimevalue());
            List<Feed> fetchfeeds = new ArrayList<Feed>();
            fetchfeeds.add(feed1);
            fetchfeeds.add(feed2);
            Map<Feed, Double> fetchedValues = cms.getFeedValues(fetchfeeds);
            for (Map.Entry<Feed, Double> feedvalue : fetchedValues.entrySet()) {
                logger.info("Fetched values for feed {}: {}", feedvalue.getKey().getId(), feedvalue.getValue());
            }

            LinkedList<Timevalue> timevalues = feed1.getData(start, end, 5);
            logger.info("Fetched {} values for feed {}:", timevalues.size(), feed1.getId());
            for (Timevalue timevalue : timevalues) {
                logger.info(timevalue.toString());
            }
            
            // Remove feed 2
            feed2.delete();
            
            // Update and insert some data points
            start -= 5*1000;
            feed1.insertData(new Timevalue(timevalues.getFirst().getTime() - 5*1000, 21));
            feed1.updateData(new Timevalue(timevalues.getLast().getTime(), 22));

            timevalues = feed1.getData(start, end, 5);
            logger.info("Fetched {} values for feed {}:", timevalues.size(), feed1.getId());
            for (Timevalue timevalue : timevalues) {
                logger.info(timevalue.toString());
            }

            // Clean up
            feed1.delete();
            input1.delete();

			
		} catch (EmoncmsException e) {
			logger.error(e.getMessage());
		} finally {
			cms.stop();
		}
	}
}
