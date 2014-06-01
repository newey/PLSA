package org.nuiz.XMLStarter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Node;

/**
 * Static class to hold settings global to all experiment runs.
 * @author Robert Newey
 */
public class GlobalSettings {
	private static ThreadPoolExecutor ex = null;
	private static int numThreads = 1;
	
	public static void parseGlobalSettings (Node globalSettings) {
		Node numThreadsNode = globalSettings.getChildNodes().item(1);
		numThreads = Integer.parseInt(numThreadsNode.getTextContent());
		
		ex = new ThreadPoolExecutor (numThreads, numThreads, 1, TimeUnit.DAYS,
				new LinkedBlockingQueue<Runnable>());
	}
	
	public static ThreadPoolExecutor getExecutor(){
		return ex;
	}
	
	public static int getNumThreads() {
		return numThreads;
	}
}
