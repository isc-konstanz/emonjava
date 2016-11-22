package org.openmuc.framework.datalogger.emoncms;

import org.emoncms.Input;

public class ChannelInputContainer {

	private final Input input;
	private final String authenticator;

	public ChannelInputContainer(Input input, String key) {
		this.input = input;
		this.authenticator = key;
	}

	public Input getInput() {
		return input;
	}

	public String getAuthenticator() {
		return authenticator;
	}
}
