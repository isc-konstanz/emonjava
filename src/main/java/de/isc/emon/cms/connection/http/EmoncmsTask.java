package de.isc.emon.cms.connection.http;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emon.cms.connection.EmoncmsResponse;


public class EmoncmsTask extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(EmoncmsTask.class);
	
	private final CountDownLatch taskFinishedSignal;
	

	/**
	 * The Tasks current callback object, which is notified of task events
	 */
	protected EmoncmsTaskCallbacks callbacks;
	
	/**
	 * Interface used by {@link EmoncmsTask} to notify the {@link EmoncmsHTTPConnection} handler about task events
	 */
	public static interface EmoncmsTaskCallbacks {
		
		void onTaskFinished(EmoncmsTask task, EmoncmsResponse response);
		
		void onConnectionFailure(EmoncmsTask task);
	}
	
	public EmoncmsTask(EmoncmsTaskCallbacks emoncms, CountDownLatch taskFinishedSignal) {
		this.callbacks = (EmoncmsTaskCallbacks) emoncms;
		this.taskFinishedSignal = taskFinishedSignal;
	}

	@Override
	public final void run() {
		
		
		if (taskFinishedSignal != null) {
			taskFinishedSignal.countDown();
		}
	}
}
