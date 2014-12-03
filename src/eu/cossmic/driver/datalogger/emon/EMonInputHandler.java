package eu.cossmic.driver.datalogger.emon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import eu.cossmic.datalogger.DataloggerException;
import eu.cossmic.datalogger.Record;

public class EMonInputHandler {
    private final String emonURL;
    private final String apiKey;

    public EMonInputHandler(String emonURL, String apiKey) {
        this.emonURL = emonURL;
        this.apiKey = apiKey;
    }
    
    public long addFeed(String feedName) throws DataloggerException, IOException, ParseException {
        JSONObject result = EMonUtil.getJSONObjectByURL(emonURL, "feed/create.json?name=" + feedName + "&datatype=1&engine=2", apiKey);

        if ((boolean) result.get("success") == true) {
            return (long) result.get("feedid");
        }
        else {
            throw new DataloggerException((String) result.get("message"));
        }
    }
    
    public void addInputProcess(int inputID, int processID, String arg) throws DataloggerException {
        try {
            JSONObject result = EMonUtil.getJSONObjectByURL(emonURL, "input/process/add.json?inputid=" + inputID + "&processid=" + processID + "&arg=" + arg, apiKey);
            
            if (!(boolean) result.get("success")) {
                throw new DataloggerException((String) result.get("message"));
            }

        } catch (IOException | ParseException ex) {
            System.err.println("EMon: Exception while connecting or parsing data: " + ex.getMessage());
        }
    }
    
    public boolean writeInput(int nodeID, String name, Record record) {
        URL url;
        try {
        	float value = Float.valueOf(0);
        	long timestamp = 0L;
        	if (record != Record.NULL_RECORD) {
            	value = record.getValue().asFloat();
            	timestamp = record.getTimestamp();
        	}
        	
            url = new URL(emonURL + "input/post.json?apikey=" + apiKey + "&node=" + nodeID + "&json={" + name + ":" + value + "}" + (timestamp > 0 ? "&time=" + timestamp : ""));
            
            InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            String response = br.readLine();
            
            if (response.equals("ok")) return true;

        } catch (IOException ex) {
            System.err.println("EMon: Exception while connecting or parsing data: " + ex.getMessage());
        }
        
        return false;
    }
}
