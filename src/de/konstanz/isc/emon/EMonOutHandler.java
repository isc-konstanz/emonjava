package de.konstanz.isc.emon;

import java.util.List;

public class EMonOutHandler {

	private final String url;
	private final String apiKey;
	
	public EMonOutHandler(String emonURL, String apiKey) {
		this.url = "http://" + emonURL + "/feed/";
		this.apiKey = apiKey;
	}
	
	public List<Feed> getFeedList() {
		
		return null;
	}
	
	public Feed getEntryList() {
		
		return null;
	}
}
