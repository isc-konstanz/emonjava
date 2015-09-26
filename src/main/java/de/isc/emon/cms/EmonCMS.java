package de.isc.emon.cms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.EmoncmsResponse;
import de.isc.emon.cms.connection.RequestMethod;
import de.isc.emon.cms.connection.RequestParameter;
import de.isc.emon.cms.data.DataType;
import de.isc.emon.cms.data.Engine;
import de.isc.emon.cms.data.Field;
import de.isc.emon.cms.data.Process;
import de.isc.emon.cms.data.Feed;
import de.isc.emon.cms.data.Input;
import de.isc.emon.cms.data.Value;


public class EmonCMS {
	private static final Logger logger = LoggerFactory.getLogger(EmonCMS.class);

	private final EmoncmsConnection connection;
	
    
    public EmonCMS(EmoncmsConnection connection) {
    	this.connection = connection;
    	
    	logger.info("Registered EmonCMS {}", connection.getId());
    }

	public void writeInputData(String inputName, Integer node, Value value) 
			throws EmoncmsException {
		String request = "input/post.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		if (node != null) {
			parameters.add(new RequestParameter("node", node.toString()));
		}
		parameters.add(new RequestParameter("json", "{" + inputName + ":" + value.getValue() + "}"));
		if (value.getTime() != null) {
			parameters.add(new RequestParameter("time", String.valueOf(value.getTime()/1000)));
		}
		
		connection.writeRequest(request, parameters);
	}
	
	public boolean postInputData(String inputName, Integer node, Value value) 
			throws EmoncmsException {
		String request = "input/post.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		if (node != null) {
			parameters.add(new RequestParameter("node", String.valueOf(node)));
		}
		parameters.add(new RequestParameter("json", "{" + inputName + ":" + value.getValue() + "}"));
		if (value.getTime() != null) {
			parameters.add(new RequestParameter("time", String.valueOf(value.getTime()/1000)));
		}
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (response.getMessage().equals("ok")) {
			return true;
		}
		
		logger.debug("Unable to post to input \"{}\": {}", inputName, response);
		return false;
	}

	public boolean setInputField(int inputId, Field field) throws EmoncmsException {
		String request = "input/set.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("inputid", String.valueOf(inputId)));
		parameters.add(new RequestParameter("fields", field.toString()));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (response.getMessage().equals("null")) {
			return true;
		}
		logger.debug("Unable to update field \"{}\" of input {}: {}", field.getKey(), inputId, response);
		return false;
	}

	public List<Input> getInputList() throws EmoncmsException {
		String request = "input/list.json?";
		
		List<Input> inputs = new ArrayList<Input>();

		EmoncmsResponse response = connection.sendRequest(request, null);
		if (!response.getMessage().equals("[]")) {
			JSONArray jsonArr = (JSONArray) response.parseJSON();
			Iterator<?> i = jsonArr.iterator();
	        while (i.hasNext()) {
	        	JSONObject json = (JSONObject) i.next();
	        	Input input = new Input(json);

	        	inputs.add(input);
	        }
		}
		
		return inputs;
	}

	public List<Input> getInputList(int nodeId) throws EmoncmsException {
		String request = "input/get_inputs.json?";
		
		List<Input> inputs = new ArrayList<Input>();

		EmoncmsResponse response = connection.sendRequest(request, null);
		if (!response.getMessage().equals("[]")) {
			JSONObject json = (JSONObject) ((JSONObject) response.parseJSON()).get(String.valueOf(nodeId));
			for (Object key : json.keySet()) {
				JSONObject j = (JSONObject) json.get(key);
				int id = Integer.valueOf((String) j.get("id"));
				String name = (String) key;
				String processList = (String) j.get("processList");
	        	Input input = new Input(id, nodeId, name, processList);
	        	
	        	inputs.add(input);
			}
		}
		
		return inputs;
	}

	public void cleanInputList() throws EmoncmsException {
		String request = "input/clean.json?";

		EmoncmsResponse response = connection.sendRequest(request, null);
		if (!response.getMessage().isEmpty()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Input list cleaned: {}", response.getMessage());
			}
		}
		else if (logger.isTraceEnabled()) {
			logger.trace("Input list already clean. Inputs without processes to clean were not found");
		}
	}

	public boolean deleteInput(int inputId) throws EmoncmsException {
		String request = "input/delete.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("inputid", String.valueOf(inputId)));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (response.getMessage().equals("null")) {
			return true;
		}
		logger.debug("Unable to delete input {}: {}", inputId, response);
		return false;
	}

	public boolean setInputProcess(int inputId, Process process) 
			throws EmoncmsException {
		String request = "input/process/set.json?";

		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("inputid", String.valueOf(inputId)));
		parameters.add(new RequestParameter(RequestMethod.POST, "processlist", process.toString()));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if ((boolean) json.get("success")) {
			return true;
		}
		logger.debug("Unable to set process {} for input {}: ", process.getId(), inputId, json.get("message"));
		return false;
	}

	public boolean setInputProcessList(int inputId, LinkedList<Process> processes) 
			throws EmoncmsException {
		String request = "input/process/set.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("inputid", String.valueOf(inputId)));

		StringBuilder processList = new StringBuilder();
		for(Process p : processes) {
			processList.append(p.toString());
			if (p != processes.getLast()) {
				processList.append(",");
			}
		}
		parameters.add(new RequestParameter(RequestMethod.POST, "processlist", processList.toString()));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if ((boolean) json.get("success")) {
			return true;
		}
		logger.debug("Unable to set process list for input {}: ", inputId, json.get("message"));
		return false;
	}

	public LinkedList<Process> getInputProcessList(int inputId) throws EmoncmsException {
		String request = "input/process/get.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("inputid", String.valueOf(inputId)));
		
		LinkedList<Process> processes = new LinkedList<Process>();
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (!response.getMessage().isEmpty()) {
			String[] processesArr = response.getMessage().split(",");
			for (int i = 0; i < processesArr.length; i++) {
				String[] processArr = processesArr[i].split(":");
				Process p = new Process(Integer.valueOf(processArr[0]), processArr[1]);
				processes.add(p);
			}
		}
		return processes;
	}
	
	public boolean resetInputProcessList(int inputId) throws EmoncmsException {
		String request = "input/process/reset.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("inputid", String.valueOf(inputId)));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if ((boolean) json.get("success")) {
			return true;
		}
		logger.debug("Unable to reset the processes of input {}: {}", inputId, json.get("message"));
		return false;
	}

	public Integer createFeed(String feedName, String feedTag, DataType datatype, Engine engine, String options)
			throws EmoncmsException {
		String request = "feed/create.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("name", feedName));
		if (feedTag != null && !feedTag.isEmpty()) {
			parameters.add(new RequestParameter("tag", feedTag));
		}
		parameters.add(new RequestParameter("datatype", String.valueOf(datatype.getId())));
		parameters.add(new RequestParameter("engine", String.valueOf(engine.getId())));
		
		if (options != null && !options.isEmpty()) {
			request = request.concat("&options=" + options);
			parameters.add(new RequestParameter("options", options));
		}

		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if ((boolean) json.get("success")) {
    		
    		return Integer.valueOf(String.valueOf(json.get("feedid")));
		}
		logger.debug("Unable to create feed \"{}\": {}", feedName, json.get("message"));
		return null;
	}

	public Integer createFeed(String feedName, String feedTag, DataType datatype, Engine engine, Field options)
			throws EmoncmsException {
		return createFeed(feedName, feedTag, datatype, engine, options.toString());
	}

	public boolean deleteFeed(int feedId) throws EmoncmsException {
		String request = "feed/delete.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (response.getMessage().equals("null")) {
			return true;
		}
		logger.debug("Unable to delete feed {}: {}", feedId, response);
		return false;
	}

	public List<Feed> getFeedList() throws EmoncmsException {
		String request = "feed/list.json?";
				
		List<Feed> feeds = new ArrayList<Feed>();

		EmoncmsResponse response = connection.sendRequest(request, null);
		if (!response.getMessage().equals("[]")) {
			JSONArray jsonArr = (JSONArray) response.parseJSON();
			Iterator<?> i = jsonArr.iterator();
	        while (i.hasNext()) {
	        	JSONObject json = (JSONObject) i.next();
	        	Feed feed = new Feed(json);
	        	
	        	feeds.add(feed);
	        }
		}
		
		return feeds;
	}

	public List<Feed> listFeedsByUser(int userId) throws EmoncmsException {
		String request = "feed/list.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("userid", String.valueOf(userId)));
		
		List<Feed> feeds = new ArrayList<Feed>();

		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (!response.getMessage().equals("[]")) {
			JSONArray jsonArr = (JSONArray) response.parseJSON();
			Iterator<?> i = jsonArr.iterator();
	        while (i.hasNext()) {
	        	JSONObject json = (JSONObject) i.next();
	        	Feed feed = new Feed(json);
	        	
	        	feeds.add(feed);
	        }
		}
		
		return feeds;
	}

	public Feed getFeed(int feedId) throws EmoncmsException {
		String request = "feed/aget.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if (!json.containsKey("success") || (boolean) json.get("success")) {
    		
			Feed feed = new Feed(json);
    		return feed;
		}
		logger.debug("Unable to retrieve feed {}: {}", feedId, json.get("message"));
		return null;
	}

	public String getFeedField(int feedId, String field) throws EmoncmsException {
		String request = "feed/get.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		parameters.add(new RequestParameter("field", field));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (!response.getMessage().contains("false")) {
			return response.getMessage();
		}
		logger.debug("Unable to retrieve field \"{}\" of feed {}: {}", field, feedId, response);
		return null;
	}
	
	public boolean setFeedField(int feedId, Field field) throws EmoncmsException {
		String request = "feed/set.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		parameters.add(new RequestParameter("fields", field.toString()));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (response.getMessage().equals("null")) {
			return true;
		}
		logger.debug("Unable to update field \"{}\" of feed {}: {}", field.getKey(), feedId, response);
		return false;
	}
	
	public Value getFeedValue(int feedId) throws EmoncmsException {
		String request = "feed/value.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (!response.getMessage().contains("false")) {
			Value value = new Value(Double.valueOf(response.getMessage()));
			return value;
		}
		logger.debug("Unable to retrieve last value of feed {}: {}", feedId, response);
		return null;
	}
	
	public Value getFeedTimeValue(int feedId) throws EmoncmsException {
		String request = "feed/timevalue.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if (!json.containsKey("success") || (boolean) json.get("success")) {
			
			Value value = new Value(json);
			return value;
		}
		logger.debug("Unable to retrieve last time and value of feed {}: {}", feedId, response);
		return null;
	}

	public LinkedList<Value> getFeedData(int feedId, long start, long end, int interval)
			throws EmoncmsException {
		String request = "feed/data.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		parameters.add(new RequestParameter("start", String.valueOf(start)));
		parameters.add(new RequestParameter("end", String.valueOf(end)));
		parameters.add(new RequestParameter("interval", String.valueOf(interval)));
		
		LinkedList<Value> values = new LinkedList<Value>();
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (response.getMessage().startsWith("[") && !response.getMessage().equals("[]")) {
			String responseStr = response.getMessage().replace("[[", "").replace("]]", "");
			if (responseStr.contains("],[")) {
				String[] valuesArr = responseStr.split("],[");
				for (int i=1; i < valuesArr.length; i++) {
					String[] valueArr = valuesArr[i].split(",");
	                Value v = new Value(Double.valueOf(valueArr[1]), Long.valueOf(valueArr[0]));
	                
	                values.add(v);
				}
			}
			else {
				String[] valueArr = responseStr.split(",");
                Value v = new Value(Double.valueOf(valueArr[1]), Long.valueOf(valueArr[0]));
                
                values.add(v);
			}
		}
		
		return values;
	}

	public boolean updateFeed(int feedId, Value value)
			throws EmoncmsException {
		String request = "feed/update.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		parameters.add(new RequestParameter("value", String.valueOf(value.getValue())));
		parameters.add(new RequestParameter("time", String.valueOf(value.getTime())));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (!response.getMessage().contains("false")) {
			return true;
		}
		logger.debug("Unable to update feed {}: {}", feedId, response);
		return false;
	}

	public boolean insertFeedData(int feedId, Value value)
			throws EmoncmsException {
		String request = "feed/insert.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		parameters.add(new RequestParameter("value", String.valueOf(value.getValue())));
		parameters.add(new RequestParameter("time", String.valueOf(value.getTime())));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (!response.getMessage().contains("false")) {
			return true;
		}
		logger.debug("Unable to insert data to feed {}: {}", feedId, response);
		return false;
	}

	public boolean deleteFeedDatapoint(int feedId, long time)
			throws EmoncmsException {
		String request = "feed/deletedatapoint.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		parameters.add(new RequestParameter("feedtime", String.valueOf(time)));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (!response.getMessage().contains("false")) {
			return true;
		}
		logger.debug("Unable to delete datapoint to feed {}: {}", feedId, response);
		return false;
	}

	public boolean setFeedProcess(int feedId, Process process) 
			throws EmoncmsException {
		String request = "feed/process/set.json?";

		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		parameters.add(new RequestParameter(RequestMethod.POST, "processlist", process.toString()));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if ((boolean) json.get("success")) {
			return true;
		}
		logger.debug("Unable to set process {} for feed {}: ", process.getId(), feedId, json.get("message"));
		return false;
	}

	public boolean setFeedProcessList(int feedId, LinkedList<Process> processes) 
			throws EmoncmsException {
		String request = "feed/process/set.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		
		StringBuilder processList = new StringBuilder();
		for(Process p : processes) {
			processList.append(p.toString());
			if (p != processes.getLast()) {
				processList.append(",");
			}
		}
		parameters.add(new RequestParameter(RequestMethod.POST, "processlist", processList.toString()));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if ((boolean) json.get("success")) {
			return true;
		}
		logger.debug("Unable to set process list for feed {}: ", feedId, json.get("message"));
		return false;
	}

	public LinkedList<Process> getFeedProcessList(int feedId) throws EmoncmsException {
		String request = "feed/process/get.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		
		LinkedList<Process> processes = new LinkedList<Process>();
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		if (!response.getMessage().isEmpty()) {
			String[] processesArr = response.getMessage().split(",");
			for (int i = 0; i < processesArr.length; i++) {
				String[] processArr = processesArr[i].split(":");
				Process p = new Process(Integer.valueOf(processArr[0]), processArr[1]);
				processes.add(p);
			}
		}
		return processes;
	}
	
	public boolean resetFeedProcessList(int feedId) throws EmoncmsException {
		String request = "feed/process/reset.json?";
		
		List<RequestParameter> parameters = new LinkedList<RequestParameter>();
		parameters.add(new RequestParameter("id", String.valueOf(feedId)));
		
		EmoncmsResponse response = connection.sendRequest(request, parameters);
		JSONObject json = (JSONObject) response.parseJSON();
		if ((boolean) json.get("success")) {
			return true;
		}
		logger.debug("Unable to reset the processes of feed {}: {}", feedId, json.get("message"));
		return false;
	}
}
