/* 
 * Copyright 2016-19 ISC Konstanz
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
package org.openmuc.framework.datalogger.engine;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Settings;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelHandler implements Channel {
	private final static Logger logger = LoggerFactory.getLogger(ChannelHandler.class);

	protected final String id;
	protected final String unit;
	protected final ValueType type;
	protected final Integer length;

	protected final Settings settings;

	protected Record record = null;

	protected ChannelHandler(LogChannel channel, Settings settings) {
		this.id = channel.getId();
		this.unit = channel.getUnit();
		this.type = channel.getValueType();
		this.length = channel.getValueTypeLength();
		this.settings = settings;
	}

	@Override
	public String getId() {
		return id;
	}

    @Deprecated
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getUnit() {
		return unit;
	}

	@Override
	public ValueType getValueType() {
		return type;
	}

	@Override
	public Integer getValueTypeLength() {
		return length;
	}

	@Override
    public String getEngine() {
		return settings.getEngine();
    }

    @Deprecated
    @Override
	public Integer getLoggingTimeOffset() {
		return settings.getIntervalOffset();
	}

    @Override
	public Integer getIntervalOffset() {
		return settings.getIntervalOffset();
	}

    @Deprecated
    @Override
	public Integer getLoggingInterval() {
		return settings.geInterval();
	}

    @Override
	public Integer getInterval() {
		return settings.geInterval();
	}

    @Override
    public Integer getIntervalMax() {
    	return settings.getIntervalMax();
    }

    @Override
    public double getTolerance() {
    	return settings.getTolerance();
    }

    @Override
    public boolean isDynamic() {
    	return settings.isDynamic();
    }

    @Override
    public boolean isAveraging() {
    	return settings.isAveraging();
    }

    @Deprecated
    @Override
	public String getLoggingSettings() {
		return settings.toString();
	}

    @Override
	public Settings getSettings() {
		return settings;
	}

    @Override
	public boolean hasSetting(String key) {
		return settings.contains(key);
	}

    @Override
	public Value getSetting(String key) {
		return settings.get(key);
	}

    @Override
	public Value getValue() {
		if (record == null) {
			return null;
		}
		return record.getValue();
	}

    @Override
	public Long getTime() {
		if (record == null) {
			return null;
		}
		return record.getTimestamp();
	}

    @Override
	public Flag getFlag() {
		if (record == null) {
			return null;
		}
		return record.getFlag();
	}

	protected Record getRecord() {
		return record;
	}

    @Override
	public boolean isValid() {
		if (record != null && record.getFlag() == Flag.VALID && record.getValue() != null) {
			return true;
		}
		return false;
	}

	protected boolean isUpdate(Record update) {
		if (record == null) {
			return true;
		}
		if (record.getFlag() != update.getFlag()) {
			return true;
		}
		else if (Flag.VALID != update.getFlag()) {
			logger.trace("Skipped logging value for unchanged flag: {}", update.getFlag());
			return false;
		}
		if (record.getTimestamp() >= update.getTimestamp()) {
			logger.trace("Skipped logging value with invalid timestamp: {}", update.getTimestamp());
			return false;
		}
		return true;
	}

	public boolean update(Record update) {
		if (isUpdate(update)) {
			record = update;
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return id+" ("+type.toString()+"): "+record.toString();
	}

}
