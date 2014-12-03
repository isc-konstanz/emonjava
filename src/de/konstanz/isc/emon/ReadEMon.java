package de.konstanz.isc.emon;

import java.util.List;

import eu.cossmic.datalogger.DataloggerException;
import eu.cossmic.datalogger.Record;
import eu.cossmic.datalogger.RecordType;
import eu.cossmic.driver.datalogger.emon.EMon;
import eu.cossmic.driver.device.Device;

public class ReadEMon {

	public static void main(String[] args) {
		String url = "http://10.1.1.231/emoncms/";
		String apikey = "71053388aaf2de3e035e6dee105de62a";
		long start = System.currentTimeMillis() - 24*60*60*1000;


        // create EMon instance
        EMon logger = new EMon(url, apikey, 3);
        
        Device device = new Device(10, "in");
        List<Record> records = null;
		try {
			records = logger.getValues(device, RecordType.POWER, start);
		} catch (DataloggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(records);
	}
}
