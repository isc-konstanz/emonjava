package org.emoncms;

import java.util.List;
import java.util.Map;

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
 */
public interface Input {

	public default Integer getId() throws EmoncmsException {
		return null;
	}

	public EmoncmsType getType();

	public String getNode() throws EmoncmsException;

	public String getName() throws EmoncmsException;

	public default String getDescription() throws EmoncmsException {
		return null;
	}

	public default void setDescription(String description)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default ProcessList getProcessList() throws EmoncmsException {
		return null;
	}

	public default void setProcessList(ProcessList processes)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void setProcessList(String processList)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void setFields(Map<String, String> fields)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void resetProcessList()
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void setField(Field field, String value)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default void setFields(FieldList fields)
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

	public default Timevalue getTimevalue() throws EmoncmsException {
		return null;
	}

	public void post(Timevalue timevalue) throws EmoncmsException;

	public void post(List<Timevalue> timevalues) throws EmoncmsException;

	public default void delete()
			throws UnsupportedOperationException, EmoncmsException {
		throw new UnsupportedOperationException("Unsupported for type "+getType());
	}

}
