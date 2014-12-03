package eu.cossmic.datalogger;

import java.util.List;

import eu.cossmic.driver.device.Device;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Matthias, Adrian ISC
 */
public interface Datalogger {
	
    public int registerID() throws DataloggerException;
    
    public boolean log(LogRecordContainer container);

	/**
	 * Gets all values (time series) of a device from startTime until now. It is the same as the call getValues(device,
	 * type, startTime, System.currentTimeMillies)
	 * 
	 * @param device
	 *            the device object to look for values.
	 * @param type
	 * 			  RecordType of the logged data. Examples are the readings of a meter or the status of a outlet.
	 * @param startTimestamp
	 *            Time of the first value in the time series in ms since epoche.
	 * @return A List of value objects or an empty list if no matching objects have been found.
	 */
	public List<Record> getValues(Device device, RecordType type, long startTimestamp) throws DataloggerException;

	/**
	 * Gets all values (time series) of a device from startTime until endTime.
	 * 
	 * @param device
	 *            the device object to look for values.
	 * @param type
	 * 			  RecordType of the logged data. Examples are the readings of a meter or the status of a outlet.
	 * @param startTimestamp
	 *            Time of the first value in the time series in ms since epoche.
	 * @param endTimestamp
	 *            Time of the last value in the time series in ms since epoche
	 * @return A List of value objects or an empty list if no matching objects have been found.
	 */
	public List<Record> getValues(Device device, RecordType type, long startTimestamp, long endTimestamp) throws DataloggerException;

	/**
	 * Gets a single value with a specified timestamp from the database.
	 * 
	 * @param device
	 *            the device object to look for values.
	 * @param type
	 * 			  RecordType of the logged data. Examples are the readings of a meter or the status of a outlet.
	 * @param timestamp
	 *            timestamp of the value (ms since epoche)
	 * @return a Value object if to has been found or null
	 */
	public Record getValue(Device device, RecordType type, long timestamp) throws DataloggerException;
}
