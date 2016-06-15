package de.isc.emon.cms.data;

public class Input {
	
	private final int id;
	private final int nodeId;
	private final String name;
	private final String processList;
	

	public Input(int id, int nodeId, String name, String processList) {
		this.id = id;
		this.nodeId = nodeId;
		this.name = name;
		this.processList = processList;
	}
	
//	public Input(JSONObject json) {
//		this.id = Integer.valueOf((String) json.get("id"));
//		this.nodeId = Integer.valueOf((String) json.get("nodeid"));
//		this.name = (String) json.get("name");
//		this.processList = (String) json.get("processList");
//	}

	public int getId() {
		return id;
	}

	public int getNodeId() {
		return nodeId;
	}

	public String getName() {
		return name;
	}

	public String getProcessList() {
		return processList;
	}

	@Override
	public String toString() {
		return "id: " + id + "; nodeId: " + nodeId + "; name: " + name + "; processList: " + processList;
	}
}
