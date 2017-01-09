package org.openmuc.framework.datalogger.emoncms;

import java.util.ArrayList;

import org.emoncms.data.Namevalue;

public class DeviceValuesCollection extends ArrayList<Namevalue> {
	private static final long serialVersionUID = 1720223912529518324L;

	private final String node;
	private final String authenticator;

	private final long timestamp;

	public DeviceValuesCollection(String node, String key, long timestamp) {
		this.node = node;
		this.authenticator = key;
		this.timestamp = timestamp;
	}

	public String getNode() {
		return node;
	}

	public String getAuthenticator() {
		return authenticator;
	}

	public long getTimestamp() {
		return timestamp;
	}
}