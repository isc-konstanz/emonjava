package de.konstanz.isc.data;


public final class IntValue implements Value {

	final int value;

	public IntValue(int value) {
		this.value = value;
	}

	@Override
	public double asDouble() {
		return (double) value;
	}

	@Override
	public int asInt() {
		return value;
	}

	@Override
	public boolean asBoolean() {
		return (value != 0);
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

}
