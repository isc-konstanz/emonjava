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
package org.openmuc.framework.datalogger.data;

import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.datalogger.spi.LogChannel;

public interface Channel extends LogChannel {

    @Deprecated
	@Override
	public String getDescription();

    public String getLogger();

    @Deprecated
    @Override
	public Integer getLoggingTimeOffset();

	public Integer getIntervalOffset();

    @Deprecated
    @Override
	public Integer getLoggingInterval();

	public Integer getInterval();

    public Integer getIntervalMax();

    public double getTolerance();

    public boolean isDynamic();

    public boolean isAveraging();

    @Deprecated
    @Override
	public String getLoggingSettings();

	public Settings getSettings();

	public boolean hasSetting(String key);

	public Value getSetting(String key);

	public Value getValue();

	public Long getTime();

	public Flag getFlag();

	public boolean isValid();

}
