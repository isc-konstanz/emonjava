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
package org.emoncms;

import java.util.List;
import java.util.Map;

import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.data.DataList;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Namevalue;
import org.emoncms.data.Options;
import org.emoncms.data.Timevalue;


/**
 * The <code>Emoncms</code> class is used to communicate with an emoncms webserver. The Energy monitoring 
 * Content Management System is an open-source web-app for processing, logging and visualising energy, 
 * temperature and other environmental data as part of the OpenEnergyMonitor project.
 * A emoncms instance can be used to
 * <ul>
 * <li>Post data to inputs and logging feeds.</li>
 * <li>Retrieved stored or processed feed data values.</li>
 * <li>Configure data processing or other settings.</li>
 * <li>Get configuration information.</li>
 * </ul>
 */
public interface Emoncms {
	

	/**
	 * Start the content management systems' communication, such as the initiation of thread pools or
	 * verifications of configured parameters.
	 * 
	 * @throws EmoncmsUnavailableException if the configured parameters cannot describe a communication
	 */
	public void start() throws EmoncmsUnavailableException;

	/**
	 * Shuts the content management systems' communication down and cleans up resources.
	 */
	public void stop();
	
	public void post(String node, String name, Timevalue timevalue) throws EmoncmsException;
	
	public void post(String node, Long time, List<Namevalue> namevalues) throws EmoncmsException;
	
	public void post(DataList data) throws EmoncmsException;
	
	public List<Input> getInputList(String node) throws EmoncmsException;

	public List<Input> getInputList() throws EmoncmsException;
	
	public List<Input> loadInputList() throws EmoncmsException;
	
	public void cleanInputList() throws EmoncmsException;
	
	public Input getInput(String node, String name) throws EmoncmsException;
	
	public Input loadInput(int id) throws EmoncmsException;
	
	/**
	 * Returns the list of all {@link Feed} objects of the authenticated user. A feed allows to retrieve 
	 * the latest data and configuration values, as well as historical data through e.g. {@link Feed#getData(long, long, int)}.
	 * <p>
	 * Feeds hold only their ID in memory and retrieve other values separately for each function call.
	 * A list of feeds, holding all current data in memory may be retrieved calling {@link #loadFeedList()}.
	 * 
	 * @return the list of feeds.
	 */
	public List<Feed> getFeedList() throws EmoncmsException;

	/**
	 * Returns the list of all {@link HttpFeedData} objects of the authenticated user. A feed holds the current 
	 * data and configuration values.
	 * <p>
	 * Feed data objects hold all feed fields, such as engine configuration parameters, in memory. To retrieve
	 * updated values or historical data, functions will be passed to an underlying {@link Feed} instance and need
	 * to be retrieved separately for each function call.
	 *
	 * @return the list of feeds, containing all data fields.
	 */
	public List<Feed> loadFeedList() throws EmoncmsException;
	
	public Feed getFeed(int id) throws EmoncmsException;
	
	public Feed loadFeed(int id) throws EmoncmsException;
	
	public Map<Feed, Double> getFeedValues(List<Feed> feeds) throws EmoncmsException;
	
	public int newFeed(String name, String tag, Datatype type, Engine engine, Options options) throws EmoncmsException;

}
