package de.isc.emon.cms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.config.EmoncmsConfig;
import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.EmoncmsResponse;
import de.isc.emon.cms.connection.http.EmoncmsHTTPConnection;
import de.isc.emon.cms.connection.php.EmoncmsPHPConnection;


public class EmonCMS {
	private static final Logger logger = LoggerFactory.getLogger(EmonCMS.class);

	private final EmoncmsConnection connection;
	private final EmoncmsConfig config;
	
    
    public EmonCMS(EmoncmsConfig config) {
    	this.config = config;
    	if (config.useShell()) {
        	this.connection = new EmoncmsPHPConnection(config.getAddress(), config.getAPIKey());
    	}
    	else {
        	this.connection = new EmoncmsHTTPConnection(config.getAddress(), config.getAPIKey());
    	}
    	logger.info("Registered EmonCMS {}", connection.getId());
    }

	public void postInputData(String inputName, Integer node, Double value, Long time) 
			throws EmoncmsException {
		String request = "input/post.json?";
		
		if (node != null) {
			request = request.concat("&node=" + node);
		}
		request = request.concat("&json={\"" + inputName + "\":" + value + "}");
		if (time != null) {
			request = request.concat("&time=" + time/1000);
		}
		
		connection.postRequest(request);
	}

	public void postInputBulkData(String inputName, String data, String optField, String optValue) 
			throws EmoncmsException {
		String request = "input/post.json?";
		
		request = request.concat("&data=[" + data + "]");
		if (optField != null && optValue != null) {
			request = request.concat("&" + optField + "=" + optValue);
		}

		connection.postRequest(request);
	}

	public EmoncmsResponse deleteInput(int inputId) throws EmoncmsException {
		String request = "input/delete.json?";
		
		request = request.concat("&inputid=" + inputId);
		
		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to delete input " + inputId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public List<EmoncmsResponse> listInputs() throws EmoncmsException {
		String request = "input/list.json?";

		List<EmoncmsResponse> response = new ArrayList<EmoncmsResponse>();
		try {
			JSONArray inputs = (JSONArray) connection.getJSONResponse(request);
			if (inputs != null) {
	    		Iterator<?> i = inputs.iterator();
	            while (i.hasNext()) {
	                JSONObject input = (JSONObject) i.next();
	                
	                EmoncmsResponse r = new EmoncmsResponse();
	                r.put("id", String.valueOf(input.get("id")));
	                r.put("nodeid", String.valueOf(input.get("nodeid")));
	                r.put("name", String.valueOf(input.get("name")));
	                r.put("description", String.valueOf(input.get("description")));
	                r.put("processList", String.valueOf(input.get("processList")));
	                r.put("time", String.valueOf(input.get("time")));
	                r.put("value", String.valueOf(input.get("value")));
	                
	                response.add(r);
	            }
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to retrieve input list");
			
		} catch (IOException | ParseException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
		
		return response;
	}

	public EmoncmsResponse addInputProcess(int inputId, String processKey, String argument) 
			throws EmoncmsException {
		String request = "input/process/add.json?";
		
		request = request.concat("&inputid=" + inputId);
		request = request.concat("&processid=" + config.getProcessId(processKey));
		if (argument != null) {
			request = request.concat("&arg=" + argument);
		}

		try {
			JSONObject process = (JSONObject) connection.getJSONResponse(request);
			if (process != null && (boolean) process.get("success")) {

				return new EmoncmsResponse("result", String.valueOf(process.get("message")));
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to add input process" + ": " + process.get("message"));
			
		} catch (IOException | ParseException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse listInputProcessList(int inputId) throws EmoncmsException {
		String request = "input/process/list.json?";
		
		request = request.concat("&inputid=" + inputId);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to list the processes of input " + inputId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse deleteInputProcess(int inputId, String processKey)
			throws EmoncmsException {
		String request = "input/process/delete.json?";

		request = request.concat("&inputid=" + inputId);
		request = request.concat("&processid=" + config.getProcessId(processKey));

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to delete the process of input " + inputId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse moveInputProcess(int inputId, String processKey, int moveBy)
			throws EmoncmsException {
		String request = "input/process/move.json?";
		
		request = request.concat("&inputid=" + inputId);
		request = request.concat("&processid=" + config.getProcessId(processKey));
		request = request.concat("&moveby=" + moveBy);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to move the process of input " + inputId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse resetInputProcess(int inputId) throws EmoncmsException {
		String request = "input/process/reset.json?";
		
		request = request.concat("&inputid=" + inputId);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to reset the processes of input " + inputId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse createFeed(String feedName, String datatypeKey, String engineKey, String options)
			throws EmoncmsException {
		String request = "feed/create.json?";
		request = request.concat("&name=\"" + feedName + "\"");
		if (datatypeKey != null && !datatypeKey.isEmpty()) {
			request = request.concat("&datatype=" + config.getDataTypeId(datatypeKey));
		}
		request = request.concat("&engine=" + config.getEngineId(engineKey));
		if (options != null && !options.isEmpty()) {
			request = request.concat("&options=" + options);
		}

		try {
			JSONObject feed = (JSONObject) connection.getJSONResponse(request);
			if (feed != null && (boolean) feed.get("success")) {
				EmoncmsResponse response = new EmoncmsResponse();
                response.put("id", String.valueOf(feed.get("feedid")));
                response.put("result", String.valueOf(feed.get("result")));
        		
        		return response;
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to create feed" + ": " + feed.get("message"));
			
		} catch (IOException | ParseException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}
	
	public EmoncmsResponse createFeed(String feedName, String engineKey)
			throws EmoncmsException {
		return createFeed(feedName, null, engineKey, null);
	}

	public EmoncmsResponse deleteFeed(int feedId) throws EmoncmsException {
		String request = "feed/delete.json?";

		request = request.concat("&id=" + feedId);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to delete feed " + feedId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public List<EmoncmsResponse> listFeeds() throws EmoncmsException {
		String request = "feed/list.json?";

		List<EmoncmsResponse> response = new ArrayList<EmoncmsResponse>();
		try {
			JSONArray inputs = (JSONArray) connection.getJSONResponse(request);
			if (inputs != null) {
	    		Iterator<?> i = inputs.iterator();
	            while (i.hasNext()) {
	                JSONObject input = (JSONObject) i.next();
	                
	                EmoncmsResponse r = new EmoncmsResponse();
	                r.put("id", String.valueOf(input.get("id")));
	                r.put("userid", String.valueOf(input.get("userid")));
	                r.put("name", String.valueOf(input.get("name")));
	                r.put("tag", String.valueOf(input.get("tag")));
	                r.put("public", String.valueOf(input.get("public")));
	                r.put("size", String.valueOf(input.get("size")));
	                r.put("engine", String.valueOf(input.get("engine")));
	                r.put("time", String.valueOf(input.get("time")));
	                r.put("value", String.valueOf(input.get("value")));
	                
	                response.add(r);
	            }
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to retrieve feed list");
			
		} catch (IOException | ParseException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
		
		return response;
	}

	public List<EmoncmsResponse> listFeedsByUser(int userId) throws EmoncmsException {
		String request = "feed/list.json?";

		request = request.concat("&userid=" + userId);

		List<EmoncmsResponse> response = new ArrayList<EmoncmsResponse>();
		try {
			JSONArray inputs = (JSONArray) connection.getJSONResponse(request);
			if (inputs != null) {
	    		Iterator<?> i = inputs.iterator();
	            while (i.hasNext()) {
	                JSONObject input = (JSONObject) i.next();
	                
	                EmoncmsResponse r = new EmoncmsResponse();
	                r.put("id", String.valueOf(input.get("id")));
	                r.put("userid", String.valueOf(input.get("userid")));
	                r.put("name", String.valueOf(input.get("name")));
	                r.put("tag", String.valueOf(input.get("tag")));
	                r.put("public", String.valueOf(input.get("public")));
	                r.put("size", String.valueOf(input.get("size")));
	                r.put("engine", String.valueOf(input.get("engine")));
	                r.put("time", String.valueOf(input.get("time")));
	                r.put("value", String.valueOf(input.get("value")));
	                
	                response.add(r);
	            }
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to retrieve feed list for user " + userId);
			
		} catch (IOException | ParseException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
		
		return response;
	}

	public EmoncmsResponse getFeedId(String feedName) throws EmoncmsException {
		String request = "feed/getid.json?";
		
		request = request.concat("&name=" + feedName);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("id", response.replace("\"", ""));
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to retrieve id of feed \"" + feedName + "\": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse getFeedValue(int feedId) throws EmoncmsException {
		String request = "feed/value.json?";

		request = request.concat("&id=" + feedId);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("\"\"") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("value", response.replace("\"", ""));
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to retrieve last value of feed " + feedId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public List<EmoncmsResponse> getFeedData(int feedId, long start, long end, int datapoints)
			throws EmoncmsException {
		String request = "feed/data.json?";

		request = request.concat("&id=" + feedId);
		request = request.concat("&start=" + start);
		request = request.concat("&end=" + end);
		request = request.concat("&dp=" + datapoints);

		List<EmoncmsResponse> values = new ArrayList<EmoncmsResponse>();
		try {
			String response = connection.getResponse(request);
			if (!response.contains("false") && !response.equals("<br />")) {
				response.replace("[[", "").replace("]]", "");
				String[] valuesArr = response.split("],[");
				for (int i=1; i < valuesArr.length; i++) {
					String[] valueArr = valuesArr[i].split(",");
					
	                EmoncmsResponse r = new EmoncmsResponse();
	                r.put("time", String.valueOf(valueArr[0]));
	                r.put("value", String.valueOf(valueArr[1]));
	                
	                values.add(r);
				}
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to retrieve data of feed " + feedId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
		
		return values;
	}

	public EmoncmsResponse getFeedField(int feedId, String field) throws EmoncmsException {
		String request = "feed/get.json?";

		request = request.concat("&id=" + feedId);
		request = request.concat("&field=" + field);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("\"\"") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("value", response.replace("\"", ""));
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to retrieve field \"" + field + "\" of feed " + feedId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}
	
	public EmoncmsResponse setFeedField(int feedId, String field, String value) throws EmoncmsException {
		String request = "feed/set.json?";
		
		request = request.concat("&id=" + feedId);
		request = request.concat("&fields={\"" + field + "\":\"" + value + "\"}");

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to set field \"" + field + "\" of feed " + feedId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse updateFeed(int feedId, long time, double value)
			throws EmoncmsException {
		String request = "feed/update.json?";

		request = request.concat("&id=" + feedId);
		request = request.concat("&value=" + value);
		request = request.concat("&time=" + time);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("value", response.replace("\"", ""));
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to update feed " + feedId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse insertFeedData(int feedId, long time, double value)
			throws EmoncmsException {
		String request = "feed/insert.json?";
	
		request = request.concat("&id=" + feedId);
		request = request.concat("&value=" + value);
		request = request.concat("&time=" + time);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to insert data to feed " + feedId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}

	public EmoncmsResponse deleteFeedDatapoint(int feedId, long time)
			throws EmoncmsException {
		String request = "feed/deletedatapoint.json?";
	
		request = request.concat("&id=" + feedId);
		request = request.concat("&feedtime=" + time);

		try {
			String response = connection.getResponse(request);
			if (!response.equals("false") && !response.equals("<br />")) {
				
				return new EmoncmsResponse("result", response);
			}
			else throw new EmoncmsException("Emoncms " + connection.getId() + " was unable to delete datapoint of feed " + feedId + ": " + response);
			
		} catch (IOException e) {
			throw new EmoncmsException("Exception while requesting \"" + request + "\": " + e.getMessage());
		}
	}
}
