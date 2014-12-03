package eu.cossmic.driver.datalogger.emon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EMonUtil {
	    
    public static JSONObject getJSONObjectByURL(String emonURL, String args, String apiKey) throws IOException, ParseException {
        URL url = new URL(emonURL + args + "&apikey=" + apiKey);
        
        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(isr);
    }
    
    public static JSONArray getJSONArrayByURL(String emonURL, String args, String apiKey) throws IOException, ParseException {
        URL url = new URL(emonURL + args + "&apikey=" + apiKey);
        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        
        JSONParser parser = new JSONParser();
        
        return (JSONArray) parser.parse(isr);
    }
}
