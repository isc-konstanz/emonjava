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
package org.openmuc.framework.datalogger.dynamic;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;

public class LogChannel implements org.openmuc.framework.datalogger.spi.LogChannel {

	protected final String id;
	protected final String desc;
	protected final String unit;
	protected final ValueType type;
	protected final Integer length;

	protected final LogSettings settings;

	protected Record record = null;

	protected LogChannel(org.openmuc.framework.datalogger.spi.LogChannel channel, LogSettings settings) {
		this.id = channel.getId();
		this.desc = channel.getDescription();
		this.unit = channel.getUnit();
		this.type = channel.getValueType();
		this.length = channel.getValueTypeLength();
		this.settings = settings;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return desc;
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

    public boolean isAveraging() {
    	return settings.isAveraging();
    }

    protected boolean isDynamic() {
    	return settings.isDynamic();
    }

    protected double getLoggingTolerance() {
    	return settings.getTolerance();
    }

    protected Integer getLoggingMaxInterval() {
    	return settings.getMaxInterval();
    }

	@Override
	public Integer getLoggingInterval() {
		return settings.geInterval();
	}

	@Override
	public Integer getLoggingTimeOffset() {
		return settings.getIntervalOffset();
	}

	@Override
	public String getLoggingSettings() {
		return settings.toString();
	}

	public LogSettings getSettings() {
		return settings;
	}

	public boolean hasSetting(String key) {
		return settings.contains(key);
	}

	public Value getSetting(String key) {
		return settings.get(key);
	}

	public Value getValue() {
		if (record == null) {
			return null;
		}
		return record.getValue();
	}

	public Long getTime() {
		if (record == null) {
			return null;
		}
		return record.getTimestamp();
	}

	public Flag getFlag() {
		if (record == null) {
			return null;
		}
		return record.getFlag();
	}

	public Record getRecord() {
		return record;
	}

	public boolean isValid() {
		if (record != null && record.getFlag() == Flag.VALID && record.getValue() != null) {
			return true;
		}
		return false;
	}

}
