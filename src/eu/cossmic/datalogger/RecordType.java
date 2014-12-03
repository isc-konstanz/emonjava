package eu.cossmic.datalogger;


public enum RecordType {
	READING, ENERGY, ENERGY_DAY, POWER, STATUS;

	@Override
	public String toString() {
		switch (this) {
		case READING:
			return "_value";
		case ENERGY:
			return "_kwh";
		case ENERGY_DAY:
			return "_kwhd";
		case POWER:
			return "_power";
		case STATUS:
			return "_status";
		default:
			return "ERROR";
		}
	}
}
