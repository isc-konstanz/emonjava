package org.openmuc.framework.datalogger.emoncms;

import java.util.HashMap;
import java.util.Map;

public class SettingsHelper {

	private final static String INPUT_ID = "inputid";
	private final static String DEVICE_NODE_ID = "nodeid";
	private final static String DEVICE_API_KEY = "apikey";

    private final Map<String, String> settingsMap = new HashMap<>();

    public SettingsHelper(String settings) {
        String[] settingsArray = settings.split(",");
        for (String arg : settingsArray) {
            int p = arg.indexOf(":");
            if (p != -1) {
                settingsMap.put(arg.substring(0, p).toLowerCase().trim(), arg.substring(p + 1).trim());
            }
        }
    }

    public Integer getInputId() {
        if (settingsMap.containsKey(INPUT_ID)) {
            return Integer.parseInt(settingsMap.get(INPUT_ID).trim());
        }

        return null;
    }

    public String getNode() {
        if (settingsMap.containsKey(DEVICE_NODE_ID)) {
            return settingsMap.get(DEVICE_NODE_ID);
        }

        return null;
    }
    
    public String getApiKey() {
        if (settingsMap.containsKey(DEVICE_API_KEY)) {
            return settingsMap.get(DEVICE_API_KEY);
        }

        return null;
    }
    
    public boolean isValid() {
        if (settingsMap.containsKey(INPUT_ID) && !settingsMap.get(INPUT_ID).trim().isEmpty() && 
        		settingsMap.containsKey(DEVICE_NODE_ID) && !settingsMap.get(DEVICE_NODE_ID).trim().isEmpty()) {
        	return true;
        }
        else return false;
    }
}
