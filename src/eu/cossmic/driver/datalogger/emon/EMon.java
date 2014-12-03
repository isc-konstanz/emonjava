/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.cossmic.driver.datalogger.emon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.parser.ParseException;

import eu.cossmic.datalogger.Datalogger;
import eu.cossmic.datalogger.DataloggerException;
import eu.cossmic.datalogger.LogRecordContainer;
import eu.cossmic.datalogger.Record;
import eu.cossmic.datalogger.RecordType;
import eu.cossmic.driver.device.Device;
/**
 *
 * @author Matthias, Adrian ISC
 */
public class EMon implements Datalogger {

	private final EMonInputHandler EMonInput;
	private final EMonOutHandler EMonOut;
    
    private final String emonURL;
    private final String apiKey;
    private final int driverID;
    
    private final ConcurrentLinkedQueue<LogRecordContainer> emonQueuedContainer = new ConcurrentLinkedQueue<>();
    
    public EMon(String emonURL, String apiKey, int driverID) {
        this.emonURL = emonURL;
        this.apiKey = apiKey;
        this.driverID = driverID;

    	EMonInput = new EMonInputHandler(emonURL, apiKey);
    	EMonOut = new EMonOutHandler(emonURL, apiKey);
        
        // start timer for resending messages
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                
                if (emonQueuedContainer.size() > 0) {
                    System.out.println("EMon: Trying to resend " + emonQueuedContainer.size() + " messages...");
                }
                
                for (LogRecordContainer c : emonQueuedContainer) {
                    System.out.println(c.getDevice().getAddress() + " - " + c.getRecord().getValue());
                }
                
                LogRecordContainer dataset;
                while ((dataset = emonQueuedContainer.peek()) != null) {
                    if (log(dataset)) {
                        emonQueuedContainer.remove();
                    }
                    else {
                        break;
                    }
                }
            }
        };

        // wait 10 seconds, then schedule once per minute
        Timer timer = new Timer();
        timer.schedule(task, 1 * 1000, 60 * 1000);
    }
    
    public boolean addHouseholdInputWithDefaultFeeds(String inputName) {
        int inputID = EMonOut.getInputID(0, inputName);

        if (inputID > 0) {
            System.out.println("EMon: Input " + inputName + " already exists. Ignoring");
            return false;
        }
        
        System.out.println("EMon: Creating new input processes and feeds for " + inputName + "...");

        EMonInput.writeInput(0, inputName, Record.NULL_RECORD);
        inputID = EMonOut.getInputID(0, inputName);

        long feedID;
        
        try {
            feedID = EMonInput.addFeed(inputName + "_kwh");
            EMonInput.addInputProcess(inputID, 1, String.valueOf(feedID));
            
            feedID = EMonInput.addFeed(inputName + "_power");
            EMonInput.addInputProcess(inputID, 21, String.valueOf(feedID));
            
            feedID = EMonInput.addFeed(inputName + "_kwhd");
            EMonInput.addInputProcess(inputID, 5, String.valueOf(feedID));
            
            return true;
        }
        catch (DataloggerException | IOException | ParseException ex) {
            System.err.println(ex.getMessage());
        }
        
        return false;
    }

    public boolean dispatchDataset(LogRecordContainer container) {
        int nodeID = container.getDevice().getID();
        String inputName = container.getDevice().getMode();
        if (container.getRecordType() == RecordType.STATUS) {
        	inputName = inputName.concat(RecordType.STATUS.toString());
        }
        
        int inputID = EMonOut.getInputID(nodeID, inputName);
        
        if (inputID > 0) {
            if (!container.getVerifyValue() || container.getRecord().getValue().asFloat() >= EMonOut.getInputValue(nodeID, inputName)) {
                if (EMonInput.writeInput(nodeID, inputName, container.getRecord())) {
                    return true;
                }
            }
            else {
                System.out.println(container.getRecord().getValue());
                System.out.println(EMonOut.getInputValue(nodeID, inputName));
                System.err.println("EMon: Plausibility check failed for " + nodeID + "_" + inputName);
                return true;
            }
        }
        // new input
        else if (inputID == 0) {
            System.out.println("EMon: Creating new input processes and feeds for " + nodeID + "_" + inputName + "...");
            boolean createInput = EMonInput.writeInput(nodeID, inputName, container.getRecord());
            
            if (createInput) {
                inputID = EMonOut.getInputID(nodeID, inputName);
                
                try {
                    long feedID;
                    
                    if (container.getRecordType() == RecordType.ENERGY) {
                        if (container.getDevice().getMode().equals(Device.NODE_ENERGYIN) || container.getDevice().getMode().equals(Device.NODE_ENERGYOUT)) {
                            feedID = EMonInput.addFeed(nodeID + "_" + inputName + RecordType.ENERGY.toString());
                            EMonInput.addInputProcess(inputID, 1, String.valueOf(feedID));

                            feedID = EMonInput.addFeed(nodeID + "_" + inputName + RecordType.ENERGY_DAY.toString());
                            EMonInput.addInputProcess(inputID, 5, String.valueOf(feedID));

                            feedID = EMonInput.addFeed(nodeID + "_" + inputName + RecordType.POWER.toString());
                            EMonInput.addInputProcess(inputID, 21, String.valueOf(feedID));
                        }
                        else if (container.getDevice().getMode().equals(Device.NODE_VALUE)) {
                            feedID = EMonInput.addFeed(nodeID + RecordType.READING.toString());
                            EMonInput.addInputProcess(inputID, 1, String.valueOf(feedID));
                        }
                    }
                    else if (container.getRecordType() == RecordType.STATUS) {
                        feedID = EMonInput.addFeed(nodeID + RecordType.STATUS.toString());
                        EMonInput.addInputProcess(inputID, 1, String.valueOf(feedID));
                    }
                    System.out.println("EMon: done");
                }
                catch (DataloggerException ex) {
                    System.err.println("EMon: failed: " + ex.getMessage());
                }
                catch (IOException | ParseException ex) {
                    System.err.println("EMon: Error while connecting or processing data: " + ex.getMessage());
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int registerID() throws DataloggerException {
        URL url;
        try {
            url = new URL(emonURL + "driver/reserve.json?apikey=" + apiKey + "&driverID=" + driverID);
            InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            String data = "";
            String line;
            while ((line = br.readLine()) != null) {
                data = data.concat(line);
            }
            
            return Integer.parseInt(data);
           
        } catch (IOException ex) {
            throw new DataloggerException(ex.getMessage());
        }
    }
    
    @Override
    public boolean log(LogRecordContainer dataset) {
        if (dispatchDataset(dataset)) {
            return true;
        }
        else {
            emonQueuedContainer.add(dataset);
            return false;
        }
    }

	@Override
	public List<Record> getValues(Device device, RecordType type, long startTimestamp) throws DataloggerException {
		
		return getValues(device, type, startTimestamp, System.currentTimeMillis());
	}

	@Override
	public List<Record> getValues(Device device, RecordType type, long startTimestamp, long endTimestamp) throws DataloggerException {
		String name = device.getID() + "_" + device.getMode() + type.toString();
		int datapoints = (int) (endTimestamp - startTimestamp)/(3*60*1000);
		
//		if (device.getType() == Device.DEVICE_IEC62056) {
//			datapoints = (int) ((endTimestamp - startTimestamp)/(((IEC62056Device) device).getInterval()*60*1000));
//		}
//		else if (device.getType() == Device.DEVICE_MBUS) {
//			datapoints = (int) ((endTimestamp - startTimestamp)/(((MBusDevice) device).getInterval()*60*1000));
//		}
//		else if (device.getType() == Device.DEVICE_MODBUS) {
//			datapoints = (int) ((endTimestamp - startTimestamp)/(((ModBusDevice) device).getInterval()*60*1000));
//		}
//		else if (device.getType() == Device.DEVICE_PCHARGE) {
//			datapoints = (int) ((endTimestamp - startTimestamp)/(((PChargeDevice) device).getInterval()*60*1000));
//		}

		try {
			return EMonOut.getFeedValues(name, startTimestamp, endTimestamp, datapoints);
		} catch (IOException | ParseException ex) {
		    System.err.println("EMon: Error while connecting or processing data: " + ex.getMessage());
        }
		return null;
	}

	@Override
	public Record getValue(Device device, RecordType type, long timestamp) throws DataloggerException {

		throw new DataloggerException("EMon retrieval of a single value not implemented yet.");
	}
}
