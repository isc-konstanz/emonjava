package eu.cossmic.driver.datalogger.emon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.Value;

import eu.cossmic.datalogger.DataloggerException;
import eu.cossmic.datalogger.Record;

public class EMonOutHandler {
	private final String emonURL;
	private final String apiKey;
	
	public EMonOutHandler(String emonURL, String apiKey) {
		this.emonURL = emonURL;
		this.apiKey = apiKey;
	}
    
    public int getInputID(int nodeID, String name) {
        try {
            JSONArray inputs = EMonUtil.getJSONArrayByURL(emonURL, "input/list.json?", apiKey);
            
            Iterator<?> i = inputs.iterator();
            while (i.hasNext()) {
                JSONObject input = (JSONObject) i.next();
                
                if (Integer.parseInt((String) input.get("nodeid")) == nodeID && input.get("name").equals(name)) {
                    return Integer.parseInt((String) input.get("id"));
                }
            }
            
            return 0;
           
        } catch (IOException | ParseException ex) {
            System.err.println("EMon: Exception while connecting or parsing data: " + ex.getMessage());
        }
        
        return -1;
    }
    
    public float getInputValue(int nodeID, String name) {
        try {
            JSONArray inputs = EMonUtil.getJSONArrayByURL(emonURL, "input/list.json?", apiKey);
            
            Iterator<?> i = inputs.iterator();
            while (i.hasNext()) {
                JSONObject input = (JSONObject) i.next();
                
                if (Integer.parseInt((String) input.get("nodeid")) == nodeID && input.get("name").equals(name)) {
                    return ((input.get("value") instanceof Boolean && (boolean) input.get("value") == false) || input.get("value") == null) ? 0 : Float.parseFloat((String) input.get("value"));
                }
            }
            
            return 0;
           
        } catch (IOException | ParseException ex) {
            System.err.println("EMon: Exception while connecting or parsing data: " + ex.getMessage());
        }
        
        return -1;
    }
	
	public Map<Integer, String> getFeedList() {
		Map<Integer, String> feeds = new HashMap<Integer, String>();
        try {
            JSONArray entries = EMonUtil.getJSONArrayByURL(emonURL, "feed/list.json?", apiKey);
            
            Iterator<?> e = entries.iterator();
            while (e.hasNext()) {
                JSONObject entry = (JSONObject) e.next();
                
                feeds.put(Integer.parseInt((String) entry.get("id")), (String) entry.get("name"));
            }
           
        } catch (IOException | ParseException ex) {
            System.err.println("EMon: Exception while connecting or parsing data: " + ex.getMessage());
        }
        if (!feeds.isEmpty()) {
        	return feeds;
        }
        
        return null;
	}
	
	public Integer getFeedID(String name) throws DataloggerException{
		// TODO implement http://10.1.1.231/emoncms/feed/getid.json?name=""
		Map<Integer, String> feeds = getFeedList();

		for (Map.Entry<Integer, String> feed : feeds.entrySet()) {
			if (feed.getValue().equals(name)) {
				return feed.getKey();
			}
		}
		
		throw new DataloggerException("No EMon feed found by the name: " + name);
	}

	public List<Record> getFeedValues(String name, long from, long until, int datapoints) throws DataloggerException, IOException, ParseException {
		
		return getFeedValues(getFeedID(name), from, until, datapoints);
	}
	
	public List<Record> getFeedValues(int id, long from, long until, int datapoints) throws IOException, ParseException {
		String url = "feed/data.json?" + "&id=" + id + "&start=" + from + "&end=" + until + "&dp=" + datapoints;
        JSONArray entries = EMonUtil.getJSONArrayByURL(emonURL, url, apiKey);
        
        List<Record> records = new ArrayList<Record>();
        Iterator<?> e = entries.iterator();
        while (e.hasNext()) {
        	String entry = e.next().toString();
        	List<String> values = Arrays.asList(entry.replace("[", "").replace("]", "").split(","));
        	
        	long timestamp = Long.parseLong(values.get(0));
        	Value value = new FloatValue(Float.parseFloat(values.get(1)));
        	records.add(new Record(value, timestamp));
        }
		return records;
	}
}
