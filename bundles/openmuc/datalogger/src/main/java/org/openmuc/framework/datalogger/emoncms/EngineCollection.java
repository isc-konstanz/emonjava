/* 
 * Copyright 2016-20 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.emoncms.EmoncmsType;
import org.openmuc.framework.datalogger.emoncms.EngineCollection.ChannelCollection;

public class EngineCollection extends LinkedList<ChannelCollection<? extends EngineChannel>> {
	private static final long serialVersionUID = -3846829759240096545L;

	private final Map<EmoncmsType, Engine<?>> engines;

	EngineCollection(Map<EmoncmsType, Engine<?>> engines) {
		super();
		this.engines = engines;
	}

	EngineCollection(Map<EmoncmsType, Engine<?>> engines, List<EngineChannel> channels) throws IOException {
		this(engines);
		for (EngineChannel channel : channels) {
			add(channel);
		}
	}

	void add(EngineChannel channel) throws IOException {
		Engine<?> engine = engines.get(channel.getEngine());
		
		if (engine == null && engines.size() > 0) {
			engine = engines.values().iterator().next();
		}
		if (engine == null) {
			throw new IOException("Engine unavailable: " + channel.getEngine());
		}
		get(engine).add(channel);
	}

	@SuppressWarnings("unchecked")
	<C extends EngineChannel> ChannelCollection<C> get(Engine<?> engine) {
		for (ChannelCollection<? extends EngineChannel> channels : this) {
			if (channels.getEngine().getType() == engine.getType()) {
				return (ChannelCollection<C>) channels;
			}
		}
		ChannelCollection<C> collection = new ChannelCollection<C>((Engine<C>) engine);
		add(collection);
		return collection;
	}

	static class ChannelCollection<C extends EngineChannel> extends LinkedList<C> {
		private static final long serialVersionUID = -2418938992605046464L;

		private final Engine<C> engine;

		public ChannelCollection(Engine<C> engine) {
			super();
			this.engine = engine;
		}

		public Engine<C> getEngine() {
			return engine;
		}

		public void doConfigure() throws IOException {
			engine.onConfigure(this);
		}

		public void doWrite(long timestamp) throws IOException {
			engine.onWrite(this, timestamp);
		}
	}

}
