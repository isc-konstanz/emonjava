package org.openmuc.framework.datalogger.dynamic;

import org.openmuc.framework.datalogger.data.Settings;
import org.openmuc.framework.datalogger.spi.LogChannel;

public class TestChannelHandler {

	public static ChannelHandler createChannelHandler(LogChannel channel, Settings settings) {
		ChannelHandler handler = new ChannelHandler(channel, settings);
		return handler;
	}
}
