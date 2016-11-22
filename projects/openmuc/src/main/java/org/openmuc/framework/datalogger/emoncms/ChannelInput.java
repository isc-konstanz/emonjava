package org.openmuc.framework.datalogger.emoncms;

import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.data.Timevalue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;

public class ChannelInput {

	private final Input input;
	private final String authenticator;

	public ChannelInput(Input input, String key) {
		this.input = input;
		this.authenticator = key;
	}

	public Input getInput() {
		return input;
	}

	public String getAuthenticator() {
		return authenticator;
	}
	
	public boolean post(Record record) throws EmoncmsException, TypeConversionException {

		if (record != null && record.getValue() != null) {
			Timevalue timevalue = new Timevalue(record.getTimestamp(), record.getValue().asDouble());
			if (authenticator != null) {
				input.post(authenticator, timevalue);
			}
			else {
				input.post(timevalue);
			}
			return true;
		}
		else return false;
	}
}