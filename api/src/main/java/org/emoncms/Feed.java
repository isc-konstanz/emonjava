/* 
 * Copyright 2016-20 ISC Konstanz
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

import java.util.LinkedList;

import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.Field;
import org.emoncms.data.FieldList;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;

/**
 * The <code>Feed</code> interface is used to communicate with an emoncms webserver and handle feed specific actions.
 * An feed instance can be used to
 * <ul>
 * <li>Retrieve the latest and historical data.</li>
 * <li>Get configuration information.</li>
 * </ul>
 */
public interface Feed {

	public int getId();

	public EmoncmsType getType();

	public default String getName() {
		return null;
	}

	public default void setName(String name)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default String getTag() throws EmoncmsException {
		return null;
	}

	public default void setTag(String tag)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default boolean isPublic() throws EmoncmsException {
		return false;
	}

	public default void setPublic(boolean visible)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default Integer getSize() throws EmoncmsException {
		return null;
	}

	public default Datatype getDatatype() throws EmoncmsException {
		return null;
	}

	public default Engine getEngine() throws EmoncmsException {
		return null;
	}

	public default ProcessList getProcessList() throws EmoncmsException {
		return null;
	}

	public default void setProcessList(ProcessList processes)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void resetProcessList()
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default String getField(Field field) throws EmoncmsException {
		return null;
	}

	public default void setField(Field field, String value)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void setFields(FieldList fields)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public Double getLatestValue() throws EmoncmsException;

	public Timevalue getLatestTimevalue() throws EmoncmsException;

	public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException;

	public default void insertData(Timevalue timevalue)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void updateData(Timevalue timevalue)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void deleteData(long time)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void deleteDataRange(long start, long end)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void delete()
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

}
