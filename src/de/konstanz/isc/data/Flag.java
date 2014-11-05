package de.konstanz.isc.data;


public enum Flag {

	VALID,
	DEFAULT_VALUE,
	NO_VALUE_RECEIVED_YET,
	NO_VALUES_LOGGED,
	CHANNEL_NOT_FOUND,
	LOGGER_ERROR,
	SERVER_ERROR,
	NO_CONNECTION_TO_SERVER,
	NO_RESPONSE_FROM_DRIVER,
	NO_DRIVER_DEFINED,
	PORT_NOT_FOUND, 
	VALUE_OUT_OF_BOUNDARIES,
	UNKNOWN_FLAG,
	UNKNOWN_ERROR,
	ANALOG_PIN_WIRE_ERROR,
	ONE_WIRE_DRIVER_ERROR;
	
	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
	
	public static Flag getFlag(String flag) {
		try {
			return valueOf(flag.toUpperCase().replace(" ", "_"));
		} catch (IllegalArgumentException e) {
		}
		return UNKNOWN_FLAG;
	}
}
