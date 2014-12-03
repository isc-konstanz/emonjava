/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.cossmic.datalogger;

import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.ValueType;

import eu.cossmic.driver.device.Device;


/**
 *
 * @author Adrian ISC
 */
public class LogRecordContainer {
    private final Device device;
    
    private final ValueType valueType;
    private final RecordType recordType;
    private final Record record;
    
    private final boolean verifyValue;
    
    public LogRecordContainer(Device device, ValueType valueType, RecordType recordType, Record record, boolean verifyValue) {
        this.device = device;
        this.valueType = valueType;
        this.recordType = recordType;
        this.record = record;
        this.verifyValue = verifyValue;
    }

    public LogRecordContainer(Device device, RecordType recordType, Float value, Long timestamp) {
    	this(device, ValueType.FLOAT, recordType, new Record(new FloatValue(value), timestamp), false);
    }

    public LogRecordContainer(Device device, RecordType recordType, Float value, Long timestamp, boolean verifyValue) {
    	this(device, ValueType.FLOAT, recordType, new Record(new FloatValue(value), timestamp), verifyValue);
    }
    
    public LogRecordContainer(Device device, RecordType recordType, Boolean status, Long timestamp) {
    	this(device, ValueType.BOOLEAN, recordType, new Record(new BooleanValue(status), timestamp), false);
    }
    
    public Device getDevice() {
        return device;
    }

	public ValueType getValueType() {
		return valueType;
	}

	public RecordType getRecordType() {
		return recordType;
	}
	
    public Record getRecord() {
        return record;
    }

	public boolean getVerifyValue() {
		return verifyValue;
	}
}
