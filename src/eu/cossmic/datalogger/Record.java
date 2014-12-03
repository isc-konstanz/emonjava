package eu.cossmic.datalogger;

import org.openmuc.framework.data.Value;

/**
 * A Record may represent a reading or a database entry. Record is immutable. It contains a value and a timestamp.
 * If a record represents a measurement reading then the timestamp is supplied by the communication device that
 * retrieved the data. If the data record is a processed value (e.g. a mean value that was stored in the database) then
 * the timestamp is supplied by the datalogger.
 */
public final class Record {

	public static final Record NULL_RECORD = new Record(null, 0L);

	private final Long timestamp;
	private final Value value;

	public Record(Value value, Long timestamp) {
		this.value = value;
		this.timestamp = timestamp;
	}

	public Value getValue() {
		return value;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "value: " + value + "; timestamp: " + timestamp;
	}

}
