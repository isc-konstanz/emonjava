package de.isc.emon.cms.connection.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.connection.EmoncmsConnection;


public class EmoncmsHTTPConnection implements EmoncmsConnection {
	private static final Logger logger = LoggerFactory.getLogger(EmoncmsHTTPConnection.class);
	
	private final String URL;
	private final String API;

//    private final Map<Integer, Sample> queuedSamplesByInputId = Collections.synchronizedMap(new LinkedHashMap<Integer, Sample>());
    
    
    public EmoncmsHTTPConnection(String url, String api) {
    	this.URL = (url.endsWith("/")) ? url : url.concat("/").concat("emoncms/");
    	this.API = api;
    }

	@Override
	public String postInputData(String inputName, double value) 
			throws IOException {
		String request = URL + "input/post.json?";
		request = request.concat("&apikey=" + API);
		
		request = request.concat("&json={\"" + inputName + "\":" + value + "}");
		
		return parseResponse(request);
	}

	@Override
	public String postInputData(String inputName, int node, double value) 
			throws IOException {
		String request = URL + "input/post.json?";
		request = request.concat("&apikey=" + API);

		request = request.concat("&node=" + node);
		request = request.concat("&json={\"" + inputName + "\":" + value + "}");
		
		return parseResponse(request);
	}

	@Override
	public String postInputData(String inputName, long time, double value) 
			throws IOException {
		String request = URL + "input/post.json?";
		request = request.concat("&apikey=" + API);

		request = request.concat("&json={\"" + inputName + "\":" + value + "}");
		request = request.concat("&time=" + time);
		
		return parseResponse(request);
	}

	@Override
	public String postInputData(String inputName, int node, long time, double value) 
			throws IOException {
		String request = URL + "input/post.json?";
		request = request.concat("&apikey=" + API);

		request = request.concat("&node=" + node);
		request = request.concat("&json={\"" + inputName + "\":" + value + "}");
		request = request.concat("&time=" + time);
		
		return parseResponse(request);
	}

	@Override
	public JSONObject deleteInput(int inputId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray listInputs() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject addInputProcess(int inputId, int processId,
			String arguments) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray listInputProcessList(int inputId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject deleteInputProcess(int inputId, int processId)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject moveInputProcess(int inputId, int processId, int moveBy)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject resetInputProcess(int inputId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject createFeed(String feedName, int engine, String options)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject deleteFeed(int feedId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray listFeeds() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray listFeedsByUser(int userId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject gedFeedId(String feedName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getFeedValue(int feedId) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray getFeedData(int feedId, long start, long end,
			int datapoints) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject renameFeed(int feedId, String newName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject updateFeed(int feedId, long time, double value)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject instertFeedData(int feedId, long time, double value)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject deleteFeedDatapoint(int feedId, long time)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
    
	private Object parseJSONResponse(String strURL) throws IOException {
		Object result = null;
		
        URL url = new URL(strURL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            InputStreamReader isr = new InputStreamReader(is);

            JSONParser parser = new JSONParser();
            result = parser.parse(isr);

        } catch (ParseException e) {
        	logger.debug("Error while parsing JSON response of: {}", strURL);
        }
        finally {
            urlConnection.disconnect();
        }
        return result;
	}
    
	private String parseResponse(String strURL) throws IOException {
		String result = null;
		
        URL url = new URL(strURL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        InputStream is = new BufferedInputStream(urlConnection.getInputStream());
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        
        result = br.readLine();

        urlConnection.disconnect();
        return result;
	}
}
