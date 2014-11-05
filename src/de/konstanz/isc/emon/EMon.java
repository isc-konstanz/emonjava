package de.konstanz.isc.emon;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;


public class EMon {
    
    private final String emonURL;
    private final String apiKey;
    private final int driverID;

	private EMonOutHandler EMonOut;
	private EMonInHandler EMonIn;
	
    public EMon(String emonURL, String apiKey, int driverID) {
        this.emonURL = emonURL;
        this.apiKey = apiKey;
        this.driverID = driverID;
        
        EMonOut = new EMonOutHandler(emonURL, apiKey);
    }
	
	public Map<Long, Double> getValues(int id, long start, long end) {

		return null;
	}
	public static void main(String[] args) throws Exception {
		String ip = "10.1.1.231";
		String apikey = "71053388aaf2de3e035e6dee105de62a";
		int feed = 2;
		long end = System.currentTimeMillis();
		long start = end - 24*60*60*1000;
		int datapoints = (int) ((end - start)/(3*60*1000));

		URL url = new URL("http://" + ip + "/emoncms/feed/data.json?"
				+ "apikey=" + apikey
				+ "&id=" + feed
				+ "&start=" + start
				+ "&end=" + end
				+ "&dp=" + datapoints);
		
		InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        
        JSONParser parser = new JSONParser();
        JSONArray entries = (JSONArray) parser.parse(isr);
        
        Iterator i = entries.iterator();
        while (i.hasNext()) {
//            JSONObject input = (JSONObject) i.next();
        	String entry = i.next().toString();
        	List<String> values = Arrays.asList(entry.replace("[", "").replace("]", "").split(","));
        	
        	long timestamp = Long.parseLong(values.get(0));
        	double value = Double.parseDouble(values.get(1));
        	
            System.out.println(i.next().toString());
        }
	}
}