package de.konstanz.isc.data;

public final class DoubleValue implements Value {

	final double value;

	public DoubleValue(double value) {
		this.value = value;
	}

	@Override
	public double asDouble() {
		return value;
	}

	@Override
	public int asInt() {
		return (int) value;
	}
	
	@Override
	public boolean asBoolean() {
		return (value != 0);
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

}
