/*
 * Copyright 2016-17 ISC Konstanz
 *
 * This file is part of emonjava.
 * For more information visit https://bitbucket.org/isc-konstanz/emonjava
 *
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.emoncms.data;


public enum Process {

	LOG_TO_FEED(1, ProcessArg.FEEDID), 
	SCALE(2, ProcessArg.VALUE), 
	OFFSET(3, ProcessArg.VALUE), 
	POWER_TO_KWH(4, ProcessArg.FEEDID), 
	POWER_TO_KWHD(5, ProcessArg.FEEDID), 
	TIMES_INPUT(6, ProcessArg.INPUTID), 
	INPUT_ONTIME(7, ProcessArg.FEEDID), 
	WHINC_TO_KWH(8, ProcessArg.FEEDID), 
	UPDATE_FEED_DATA(10, ProcessArg.FEEDID), 
	ADD_INPUT(11, ProcessArg.INPUTID), 
	DIVIDE_INPUT(12, ProcessArg.INPUTID), 
	ACCUMULATOR(14, ProcessArg.FEEDID), 
	RATECHANGE(15, ProcessArg.FEEDID), 
	HISTOGRAM(16, ProcessArg.FEEDID), 
	AVERAGE(17, ProcessArg.FEEDID), 
	PULSE_DIFF(20, ProcessArg.FEEDID), 
	KWH_TO_POWER(21, ProcessArg.FEEDID), 
	SUBTRACT_INPUT(22, ProcessArg.INPUTID), 
	KWH_TO_KWHD(23, ProcessArg.FEEDID), 
	ALLOWPOSITIVE(24, ProcessArg.NONE), 
	ALLOWNEGATIVE(25, ProcessArg.NONE), 
	SIGNED_TO_UNSIGNED(26, ProcessArg.NONE), 
	MAX_VALUE(27, ProcessArg.FEEDID), 
	MIN_VALUE(28, ProcessArg.FEEDID), 
	ADD_FEED(29, ProcessArg.FEEDID), 
	SUB_FEED(30, ProcessArg.FEEDID), 
	MULTIPLY_BY_FEED(31, ProcessArg.FEEDID), 
	DIVIDE_BY_FEED(32, ProcessArg.FEEDID), 
	RESET_TO_ZERO(33, ProcessArg.NONE), 
	WH_ACCUMULATOR(34, ProcessArg.FEEDID), 
	PUBLISH_TO_MQTT(35, ProcessArg.TEXT), 
	RESET_TO_NULL(36, ProcessArg.NONE), 
	RESET_TO_ORIGINAL(37, ProcessArg.NONE), 
	IF_ZERO_SKIP(42, ProcessArg.NONE), 
	IF_NOT_ZERO_SKIP(43, ProcessArg.NONE), 
	IF_NULL_SKIP(44, ProcessArg.NONE), 
	IF_NOT_NULL_SKIP(45, ProcessArg.NONE), 
	IF_GT_SKIP(46, ProcessArg.VALUE), 
	IF_GT_EQUAL_SKIP(47, ProcessArg.VALUE), 
	IF_LT_SKIP(48, ProcessArg.VALUE), 
	IF_LT_EQUAL_SKIP(49, ProcessArg.VALUE), 
	IF_EQUAL_SKIP(50, ProcessArg.VALUE), 
	IF_NOT_EQUAL_SKIP(51, ProcessArg.VALUE), 
	GOTO_PROCESS(52, ProcessArg.VALUE), 
	SOURCE_FEED_DATA_TIME(53, ProcessArg.FEEDID), 
	ADD_SOURCE_FEED(55, ProcessArg.FEEDID), 
	SUB_SOURCE_FEED(56, ProcessArg.FEEDID), 
	MULTIPLY_BY_SOURCE_FEED(57, ProcessArg.FEEDID), 
	DIVIDE_BY_SOURCE_FEED(58, ProcessArg.FEEDID), 
	RECIPROCAL_BY_FEED(59, ProcessArg.FEEDID);

	private final int id;
	private final ProcessArg arg;

	private Process(int id, ProcessArg arg) {
		this.id = id;
		this.arg = arg;
	}

	public int getId() {
		return id;
	}

	public ProcessArg getArgument() {
		return arg;
	}

	public static Process getEnum(int id) {
		switch (id) {
		case 1:
			return LOG_TO_FEED;
		case 2:
			return SCALE;
		case 3:
			return OFFSET;
		case 4:
			return POWER_TO_KWH;
		case 5:
			return POWER_TO_KWHD;
		case 6:
			return TIMES_INPUT;
		case 7:
			return INPUT_ONTIME;
		case 8:
			return WHINC_TO_KWH;
		case 10:
			return UPDATE_FEED_DATA;
		case 11:
			return ADD_INPUT;
		case 12:
			return DIVIDE_INPUT;
		case 14:
			return ACCUMULATOR;
		case 15:
			return RATECHANGE;
		case 16:
			return HISTOGRAM;
		case 17:
			return AVERAGE;
		case 20:
			return PULSE_DIFF;
		case 21:
			return KWH_TO_POWER;
		case 22:
			return SUBTRACT_INPUT;
		case 23:
			return KWH_TO_KWHD;
		case 24:
			return ALLOWPOSITIVE;
		case 25:
			return ALLOWNEGATIVE;
		case 26:
			return SIGNED_TO_UNSIGNED;
		case 27:
			return MAX_VALUE;
		case 28:
			return MIN_VALUE;
		case 29:
			return ADD_FEED;
		case 30:
			return SUB_FEED;
		case 31:
			return MULTIPLY_BY_FEED;
		case 32:
			return DIVIDE_BY_FEED;
		case 33:
			return RESET_TO_ZERO;
		case 34:
			return WH_ACCUMULATOR;
		case 35:
			return PUBLISH_TO_MQTT;
		case 36:
			return RESET_TO_NULL;
		case 37:
			return RESET_TO_ORIGINAL;
		case 42:
			return IF_ZERO_SKIP;
		case 43:
			return IF_NOT_ZERO_SKIP;
		case 44:
			return IF_NULL_SKIP;
		case 45:
			return IF_NOT_NULL_SKIP;
		case 46:
			return IF_GT_SKIP;
		case 47:
			return IF_GT_EQUAL_SKIP;
		case 48:
			return IF_LT_SKIP;
		case 49:
			return IF_LT_EQUAL_SKIP;
		case 50:
			return IF_EQUAL_SKIP;
		case 51:
			return IF_NOT_EQUAL_SKIP;
		case 52:
			return GOTO_PROCESS;
		case 53:
			return SOURCE_FEED_DATA_TIME;
		case 55:
			return ADD_SOURCE_FEED;
		case 56:
			return SUB_SOURCE_FEED;
		case 57:
			return MULTIPLY_BY_SOURCE_FEED;
		case 58:
			return DIVIDE_BY_SOURCE_FEED;
		case 59:
			return RECIPROCAL_BY_FEED;
		default:
			throw new IllegalArgumentException("Unknown process id: " + id);
		}
	}
	
	@Override
	public String toString() {
		return id + ":" + arg.getValue();
	}
}
