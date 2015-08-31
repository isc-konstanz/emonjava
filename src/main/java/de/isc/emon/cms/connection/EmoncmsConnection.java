package de.isc.emon.cms.connection;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public interface EmoncmsConnection {

    public String postInputData(String inputName, double value) throws IOException;
    
    public String postInputData(String inputName, int node, double value) throws IOException;
    
    public String postInputData(String inputName, long time, double value) throws IOException;
    
    public String postInputData(String inputName, int node, long time, double value) throws IOException;
    
    public JSONObject deleteInput(int inputId) throws IOException;
    
    public JSONArray listInputs() throws IOException;
    
    public JSONObject addInputProcess(int inputId, int processId, String arguments) throws IOException;
    
    public JSONArray listInputProcessList(int inputId) throws IOException;
    
    public JSONObject deleteInputProcess(int inputId, int processId) throws IOException;
    
    public JSONObject moveInputProcess(int inputId, int processId, int moveBy) throws IOException;
    
    public JSONObject resetInputProcess(int inputId) throws IOException;
    
    public JSONObject createFeed(String feedName, int engine, String options) throws IOException;
    
    public JSONObject deleteFeed(int feedId) throws IOException;
    
    public JSONArray listFeeds() throws IOException;
    
    public JSONArray listFeedsByUser(int userId) throws IOException;
    
    public JSONObject gedFeedId(String feedName) throws IOException;
    
    public JSONObject getFeedValue(int feedId) throws IOException;
    
    public JSONArray getFeedData(int feedId, long start, long end, int datapoints) throws IOException;
    
    public JSONObject renameFeed(int feedId, String newName) throws IOException;
    
    public JSONObject updateFeed(int feedId, long time, double value) throws IOException;
    
    public JSONObject instertFeedData(int feedId, long time, double value) throws IOException;
    
    public JSONObject deleteFeedDatapoint(int feedId, long time) throws IOException;
}
