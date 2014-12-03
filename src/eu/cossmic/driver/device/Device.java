/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.cossmic.driver.device;

import java.util.Arrays;

/**
 *
 * @author Matthias ISC
 */
public class Device {
    public static int DEVICE_HM = 1;
    public static int DEVICE_FS20 = 2;
    public static int DEVICE_MBUS = 3;
    public static int DEVICE_IEC62056 = 4;
    public static int DEVICE_GPIO = 5;
    public static int DEVICE_MODBUS = 6;
    public static int DEVICE_PCHARGE = 7;
    
    public static String NODE_ENERGYIN = "energyin";
    public static String NODE_ENERGYOUT = "energyout";
    public static String NODE_VALUE = "value";
    
    protected int ID;
    protected String mode;
    
    protected String address;
    protected int type;
    //protected int direction;
    
    protected String[] deviceDetails;
    
    public Device() {
        
    }
    
    public Device(Device device) {
        this.ID = device.getID();
        this.mode = device.getMode();
        this.address = device.getAddress();
        this.type = device.getType();
        this.deviceDetails = device.getDeviceDetails();
    }
    
    public Device(int ID, String mode) {
        this.ID = ID;
        this.mode = mode;
        this.address = null;
        this.deviceDetails = null;
    }
    
    public Device(int ID, String mode, String address) {
        this.ID = ID;
        this.mode = mode;
        this.address = address;
        this.deviceDetails = null;
    }
    
    public Device(int ID, String mode, String address, int type, String[] deviceDetails) {
        this.ID = ID;
        this.mode = mode;
        this.address = address;
        this.type = type;
        this.deviceDetails = deviceDetails;
    }
    
    public void loadConfiguration(String deviceDetails) {
        try {
            String[] deviceDetailsArray = deviceDetails.split(",");

            this.type = Integer.parseInt(deviceDetailsArray[0]);
            this.address = deviceDetailsArray[1];

            if (deviceDetailsArray.length > 2) {
                this.deviceDetails = Arrays.copyOfRange(deviceDetailsArray, 2, deviceDetailsArray.length);
            }
            else {
                this.deviceDetails = null;
            }
        }
        catch (NumberFormatException ex) {
            System.err.println("Syntax error in configuration file devices.properties");
            System.exit(1);
        }
    }
    
    public int getID() {
        return ID;
    }
    
    public String getMode() {
        return mode;
    }
    
    public String getAddress() {
        return address;
    }
    
    public int getType() {
        return type;
    }
    
    /*public int getDirection() {
        return direction;
    }*/
    
    public String getDeviceInfo() {
        return type + "," + address + "," + join(getDeviceDetails(), ",");
    }
    
    protected String[] getDeviceDetails() {
        return deviceDetails;
    }
    
    /*public void setID(int ID) {
        this.ID = ID;
    }*/
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    protected void setDeviceDetails(String[] deviceDetails) {
        this.deviceDetails = deviceDetails;
    }
    
    private String join(String[] split, String separator) {
        if (split == null || split.length == 0) return "";
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < split.length; i++) {
            sb.append(split[i]);
            if (i != split.length - 1) {
                sb.append(separator);
            }
        }
        
        return sb.toString();
    }
    
    public void close() {
    }
}
