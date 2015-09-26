package de.isc.emon.cms.connection.http;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.EmoncmsException;
import de.isc.emon.cms.connection.EmoncmsConnection;
import de.isc.emon.cms.connection.EmoncmsResponse;
import de.isc.emon.cms.connection.RequestParameter;
import de.isc.emon.cms.connection.http.EmoncmsTask.EmoncmsTaskCallbacks;


public class EmoncmsHTTPConnection implements EmoncmsConnection, EmoncmsTaskCallbacks {
	private static final Logger logger = LoggerFactory.getLogger(EmoncmsHTTPConnection.class);
	
	private static final int SEND_RETRY_INTERVAL = 30000;
	
	private final String URL;
	private final String KEY;

	private ExecutorService executor = null;
	private volatile Timer timer = null;
	
	private final List<EmoncmsTask> queuedTasks = new LinkedList<EmoncmsTask>();
    
	
	public EmoncmsHTTPConnection(String address, String apiKey) {
		String url;
		if (!address.startsWith("http://")) {
			url = "http://".concat(address);
		}
		else {
			url = address;
		}
    	this.URL = url.concat("emoncms/");
    	this.KEY = apiKey;

		executor = Executors.newCachedThreadPool();
    }

	@Override
	public String getId() {
		return "HTTP connection";
	}

	@Override
	public String getAddress() {
		return URL;
	}
	
	@Override
	public void writeRequest(String request, List<RequestParameter> parameters) {
		executeTask(URL + request + "&apikey=" + KEY, parameters);
	}
	
	@Override
	public EmoncmsResponse sendRequest(String request, List<RequestParameter> parameters) throws EmoncmsException {
		CountDownLatch taskFinishedSignal = new CountDownLatch(1);
		
		EmoncmsTask task = new EmoncmsTask(this, taskFinishedSignal, URL + request + "&apikey=" + KEY, parameters);
		executor.execute(task);
		try {
			taskFinishedSignal.await();
		} catch (InterruptedException e) {
		}
		
		EmoncmsResponse response = task.getResponse();
		if (response != null) {
			return response;
		}
		throw new EmoncmsException("Error while connecting to \"" + request + "\"");
	}
	
	@Override
	public void onConnectionFailure(EmoncmsTask task) {
		StringBuilder request = new StringBuilder();
		request.append(task.getRequest());
		if (task.getParameters() != null) {
			for (RequestParameter r : task.getParameters()) {
				request.append("&");
				request.append(r.toString());
			}
		}
		logger.debug("Error sending request \"{}\"", request);
		System.out.println("Error sending request \"" + request + "}\"");
		
		synchronized (queuedTasks) {
			queuedTasks.add(task);

	    	if (timer == null) {
	    		LinkedList<EmoncmsTask> tasks = new LinkedList<EmoncmsTask>(queuedTasks);
	    		ResendTask resendTask = new ResendTask(tasks);
        		queuedTasks.clear();
        		
	    		timer = new Timer();
	            timer.schedule(resendTask, SEND_RETRY_INTERVAL);
			}
		}
	}
	
	private void executeTask(CountDownLatch taskFinishedSignal, String request, List<RequestParameter> parameters) {
		EmoncmsTask task = new EmoncmsTask(this, taskFinishedSignal, request, parameters);
		executor.execute(task);
	}
	
	private void executeTask(String request, List<RequestParameter> parameters) {
		executeTask(null, request, parameters);
	}
	
	private class ResendTask extends TimerTask {
		private final List<EmoncmsTask> tasks;
		
		public ResendTask(List<EmoncmsTask> tasks) {
			this.tasks = tasks;
		}
		
		@Override
		public void run() {
			System.out.println("Resending " + tasks.size() + " messages, failed to be sent");
			logger.debug("Resending {} messages, failed to be sent", tasks.size());
			for (EmoncmsTask t : tasks) {
				if (logger.isTraceEnabled()) {
					StringBuilder request = new StringBuilder();
					request.append(t.getRequest());
					for (RequestParameter r : t.getParameters()) {
						request.append("&");
						request.append(r.toString());
					}
					logger.trace("Resending request \"{}\"", request);
				}
				executeTask(t.getRequest(), t.getParameters());
			}

			synchronized (queuedTasks) {
		    	timer.cancel();
		    	timer = null;
			}
		}
	}
}
