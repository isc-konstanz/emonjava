package de.konstanz.isc.data;


/**
 * A Record may represent a reading or a database entry. Record is immutable. It contains a value, a timestamp, and a
 * flag.
 */
public final class Record {

	private final Long timestamp;
	private final Flag flag;
	private final Value value;

	public Record(Value value, Long timestamp, Flag flag) {
		this.value = value;
		this.timestamp = timestamp;
		this.flag = flag;
	}

	/**
	 * Creates a valid record.
	 * 
	 * @param value
	 * @param timestamp
	 */
	public Record(Value value, Long timestamp) {
		this(value, timestamp, Flag.VALID);
	}
	
	/**
	 * Creates an invalid record with the given flag. The flag may not indicate valid.
	 * 
	 * @param flag the flag of the invalid record.
	 */
	public Record(Long timestamp, Flag flag) {
		this(null, timestamp, flag);
		if (flag == Flag.VALID) {
			throw new IllegalArgumentException("flag must indicate an error");
		}
	}
	
	public Record(Flag flag) {
		this(null, flag);
	}

	public Value getValue() {
		return value;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Flag getFlag() {
		return flag;
	}

	@Override
	public String toString() {
		return "value: " + value + "; timestamp: " + timestamp + "; flag: " + flag;
	}
}
