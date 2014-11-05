package de.konstanz.isc.data;

public final class BooleanValue implements Value {

	final boolean value;

	public BooleanValue(boolean value) {
		this.value = value;
	}

	@Override
	public double asDouble() {
		if (value) {
			return 1;
		}
		else {
			return 0;
		}
	}

	@Override
	public int asInt() {
		if (value) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	@Override
	public boolean asBoolean() {
		return value;
	}

	@Override
	public String toString() {
		return Boolean.toString(value);
	}
}
