/* 
 * Copyright 2016-2021 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
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
 */
package org.emoncms;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

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
public interface Emoncms extends Closeable {

	/**
	 * Returns the {@link Emoncms} connection type
	 * 
	 * @return the connections communication type.
	 */
	public EmoncmsType getType();

	/**
	 * Returns whether the {@link Emoncms} instance holds an open and valid connection
	 * 
	 * @return the connections closed state.
	 */
	public boolean isClosed();

	/**
	 * Open the content management systems' communication connection, such as the initiation of thread pools or
	 * verifications of configured parameters.
	 * 
	 * @throws EmoncmsUnavailableException if the configured parameters cannot describe a communication
	 */
	public void open() throws EmoncmsUnavailableException;

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
	 * Returns the list of all {@link Input} objects of the authenticated user and the specified node ID. 
	 * An input allows to configure processes and post data through e.g. {@link Input#post(Timevalue)}.
	 * 
	 * @throws UnsupportedOperationException if the connection type does not allow the operation.
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param node the Node ID, inputs should be returned for.
	 * 
	 * @return the list of inputs.
	 */
	public default List<Input> getInputList(String node)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	/**
	 * Returns the list of all {@link Input} objects of the authenticated user. 
	 * An input allows to configure processes and post data through e.g. {@link Input#post(Timevalue)}.
	 * 
	 * @throws UnsupportedOperationException if the connection type does not allow the operation.
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @return the list of inputs.
	 */
	public default List<Input> getInputList()
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	/**
	 * Returns the {@link Input} object for the specified node and name. An input allows to configure processes 
	 * and post data through e.g. {@link Input#post(Timevalue)}.
	 * 
	 * @throws UnsupportedOperationException if the connection type does not allow the operation.
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param node the Node ID of the input to get.
	 * @param name the Name of the input to get.
	 * 
	 * @return the input object.
	 */
	public default Input getInput(String node, String name)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	/**
	 * Returns the {@link Input} object for the specified input ID. An input allows to configure processes 
	 * and post data through e.g. {@link Input#post(Timevalue)}.
	 * 
	 * @throws UnsupportedOperationException if the connection type does not allow the operation.
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param id the ID of the input to get.
	 * 
	 * @return the input object.
	 */
	public default Input getInput(int id)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	/**
	 * Returns the list of all {@link Feed} objects of the authenticated user. A feed allows to retrieve 
	 * the latest data and configuration fields, as well as historical data through e.g. {@link Feed#getData(long, long, int)}.
	 * 
	 * @throws UnsupportedOperationException if the connection type does not allow the operation.
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @return the list of feeds.
	 */
	public default List<Feed> getFeedList()
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	/**
	 * Returns the {@link Feed} object for the specified feed ID. A feed allows to retrieve the latest data 
	 * and configuration fields, as well as historical data through e.g. {@link Feed#getData(long, long, int)}.
	 * 
	 * @throws UnsupportedOperationException if the connection type does not allow the operation.
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param id the ID of the feed to get.
	 * 
	 * @return the feed object.
	 */
	public default Feed getFeed(int id)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	/**
	 * Returns the latest values of a list of {@link Feed} objects.
	 *
	 * @throws UnsupportedOperationException if the connection type does not allow the operation.
	 * @throws EmoncmsException if the communication with the emoncms server fails.
	 * 
	 * @param feeds the list of feeds to get the latest values for.
	 * 
	 * @return a map of corresponding feed values.
	 */
	public default Map<Feed, Double> getFeedValues(List<Feed> feeds)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	/**
	 * Creates a new {@link Feed} for the emoncms server. A feed allows to retrieve the latest data 
	 * and configuration fields, as well as historical data through e.g. {@link Feed#getData(long, long, int)}.
	 * 
	 * @throws UnsupportedOperationException if the connection type does not allow the operation.
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
	public default int newFeed(String name, String tag, Datatype type, Engine engine, Options options)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

}
