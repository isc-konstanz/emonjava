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
 * An emoncms instance can be used to
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

	/**
	 * Post a {@link Timevalue} to a defined {@link Input}, identified by its node and name and authenticated by a device API key.
	 * <p>
	 * If the timevalues timestamp equals null, the CMS will use the current timestamp, to further process the input. 
	 * The HTTP post requests may be authenticated with a devicekey, enabling to write values to a defined 
	 * devices' node ID. This can be useful, to avoid the storage of an API key with more permissions.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param node the Node ID of the input, a timevalue should be posted to.
	 * @param name the Name of the input, a timevalue should be posted to.
	 * @param timevalue the timevalue to post.
	 * @param devicekey the device API Key, to authenticate the request.
	 */
	public void post(String node, String name, Timevalue timevalue, String devicekey) throws EmoncmsException;

	/**
	 * Post a {@link Timevalue} to a defined {@link Input}, identified by its node and name.
	 * <p>
	 * If the timevalues timestamp equals null, the CMS will use the current timestamp, to further process the input.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param node the Node ID of the input, a timevalue should be posted to.
	 * @param name the Name of the input, a timevalue should be posted to.
	 * @param timevalue the timevalue to post.
	 */
	public void post(String node, String name, Timevalue timevalue) throws EmoncmsException;

	/**
	 * Post a list of {@link Namevalue}s to a defined {@link Input}, identified by their node and name and authenticated by a device API key.
	 * <p>
	 * If the timevalues timestamp equals null, the CMS will use the current timestamp, to further process the input. 
	 * The HTTP post requests may be authenticated with a devicekey, enabling to write values to a defined 
	 * devices' node ID. This can be useful, to avoid the storage of an API key with more permissions.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param node the common Node ID of the inputs, a value should be posted to.
	 * @param time the timestamp, the posted values should be processed with.
	 * @param namevalues the list of namevalues that will be posted.
	 * @param devicekey the device API Key, to authenticate the request.
	 */
	public void post(String node, Long time, List<Namevalue> namevalues, String devicekey) throws EmoncmsException;

	/**
	 * Post a list of {@link Namevalue}s to a defined {@link Input}, identified by their node.
	 * <p>
	 * If the time equals null, the CMS will use the current timestamp, to further process the inputs.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param node the common Node ID of the inputs, a value should be posted to.
	 * @param time the timestamp, the posted values should be processed with.
	 * @param namevalues the list of namevalues that will be posted.
	 */
	public void post(String node, Long time, List<Namevalue> namevalues) throws EmoncmsException;


	/**
	 * Post data to a set of {@link Input}s.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param data the data to post to the CMS.
	 */
	public void post(DataList data) throws EmoncmsException;


	/**
	 * Post data to a set of {@link Input}s, authenticated by a device API key.
	 * <p>
	 * The HTTP post requests may be authenticated with a devicekey, enabling to write values to a defined 
	 * devices' node ID. This can be useful, to avoid the storage of an API key with more permissions.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param data the data to post to the CMS.
	 * @param devicekey the device API Key, to authenticate the request.
	 */
	public void post(DataList data, String devicekey) throws EmoncmsException;

	/**
	 * Returns the list of all {@link Input} objects of the authenticated user and the specified node ID. 
	 * An input allows to configure processes and post data through e.g. {@link Input#post(Timevalue)}. 
	 * <p>
	 * Inputs hold all their configuration fields in memory, but may be cleared to only hold their ID, node and name 
	 * by calling {@link Input#clear()}. Input methods only need those variables to maintain their functionality 
	 * and other resources may be released to save memory.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param node the Node ID, inputs should be returned for.
	 * 
	 * @return the list of inputs.
	 */
	public List<Input> getInputList(String node) throws EmoncmsException;

	/**
	 * Returns the list of all {@link Input} objects of the authenticated user. 
	 * An input allows to configure processes and post data through e.g. {@link Input#post(Timevalue)}.
	 * <p>
	 * Inputs hold all their configuration fields in memory, but may be cleared to only hold their ID, node and name 
	 * by calling {@link Input#clear()}. Input methods only need those variables to maintain their functionality 
	 * and other resources may be released to save memory.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @return the list of inputs.
	 */
	public List<Input> getInputList() throws EmoncmsException;

	/**
	 * Returns the {@link Input} object for the specified node and name. An input allows to configure processes 
	 * and post data through e.g. {@link Input#post(Timevalue)}.
	 * <p>
	 * Inputs hold all their configuration fields in memory, but may be cleared to only hold their ID, node and name 
	 * by calling {@link Input#clear()}. Input methods only need those variables to maintain their functionality 
	 * and other resources may be released to save memory.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param node the Node ID of the input to get.
	 * @param name the Name of the input to get.
	 * 
	 * @return the input object.
	 */
	public Input getInput(String node, String name) throws EmoncmsException;

	/**
	 * Returns the {@link Input} object for the specified input ID. An input allows to configure processes 
	 * and post data through e.g. {@link Input#post(Timevalue)}.
	 * <p>
	 * Inputs hold all their configuration fields in memory, but may be cleared to only hold their ID, node and name 
	 * by calling {@link Input#clear()}. Input methods only need those variables to maintain their functionality 
	 * and other resources may be released to save memory.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param id the ID of the input to get.
	 * 
	 * @return the input object.
	 */
	public Input getInput(int id) throws EmoncmsException;

	/**
	 * Returns the list of all {@link Feed} objects of the authenticated user. A feed allows to retrieve 
	 * the latest data and configuration fields, as well as historical data through e.g. {@link Feed#getData(long, long, int)}.
	 * <p>
	 * Feeds hold all their configuration fields in memory, but may be cleared to only hold their ID, by calling {@link Feed#clear()}.
	 * Feed methods only need the ID to maintain their functionality and other resources may be released to save memory.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @return the list of feeds.
	 */
	public List<Feed> getFeedList() throws EmoncmsException;

	/**
	 * Returns the {@link Feed} object for the specified feed ID. A feed allows to retrieve the latest data 
	 * and configuration fields, as well as historical data through e.g. {@link Feed#getData(long, long, int)}.
	 * <p>
	 * Feeds hold all their configuration fields in memory, but may be cleared to only hold their ID, by calling {@link Feed#clear()}.
	 * Feed methods only need the ID to maintain their functionality and other resources may be released to save memory.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param id the ID of the feed to get.
	 * 
	 * @return the feed object.
	 */
	public Feed getFeed(int id) throws EmoncmsException;

	/**
	 * Returns the latest values of a list of {@link Feed} objects.
	 *
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param feeds the list of feeds to get the latest values for.
	 * 
	 * @return a map of corresponding feed values.
	 */
	public Map<Feed, Double> getFeedValues(List<Feed> feeds) throws EmoncmsException;

	/**
	 * Creates a new {@link Feed} for the emoncms server. A feed allows to retrieve the latest data 
	 * and configuration fields, as well as historical data through e.g. {@link Feed#getData(long, long, int)}.
	 * 
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param name the name of the feed to create.
	 * @param tag an optional descriptor that will be used to group feeds in the emoncms web view. The tag may be left null.
	 * @param type the {@link Datatype} of the feed.
	 * @param engine the {@link Engine} of the feed.
	 * @param options the options of the feed engine. Depending of the engine, this may be left null.
	 * 
	 * @return the ID of the newly created feed.
	 */
	public int newFeed(String name, String tag, Datatype type, Engine engine, Options options) throws EmoncmsException;

}
