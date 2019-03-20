package org.emoncms;

import java.util.List;
import java.util.Map;

import org.emoncms.com.EmoncmsException;
import org.emoncms.data.Authentication;
import org.emoncms.data.Field;
import org.emoncms.data.FieldList;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;

/**
 * The <code>Input</code> interface is used to communicate with an emoncms webserver and handle input specific actions.
 * An input instance can be used to
 * <ul>
 * <li>Post data to the instanced input.</li>
 * <li>Configure logging or other data processing.</li>
 * <li>Get configuration information.</li>
 * </ul>
 * 
 * <p>
 * Inputs hold all their configuration fields in memory, but may be cleared to only hold their ID, node and name 
 * by calling {@link Input#clear()}. Input methods only need those variables to maintain their functionality 
 * and other resources may be released to save memory. 
 * Current values for all fields can be retrieved by calling {@link Input#load()}.
 */

public interface Input {

	public default int getId() {
		throw new UnsupportedOperationException();
	}

	public default String getNode() {
		throw new UnsupportedOperationException();
	}

	public default String getName() {
		throw new UnsupportedOperationException();
	}

	public default String getDescription() {
		throw new UnsupportedOperationException();
	}

	public default void setDescription(String description) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default ProcessList getProcessList() {
		throw new UnsupportedOperationException();
	}

	public default void setProcessList(ProcessList processes) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void setProcessList(String processList) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void setFields(Map<String, String> fields) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void resetProcessList() throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default Timevalue getTimevalue() {
		throw new UnsupportedOperationException();
	}

	public default void post(Timevalue timevalue) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void post(Timevalue timevalue, Authentication authentication) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void post(List<Timevalue> timevalues) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void post(List<Timevalue> timevalues, Authentication authentication) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void setField(Field field, String value) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void setFields(FieldList fields) throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void delete() throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void load() throws EmoncmsException {
		throw new UnsupportedOperationException();
	}

	public default void clear() {
		throw new UnsupportedOperationException();
	}
}
