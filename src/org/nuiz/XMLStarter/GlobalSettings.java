package org.nuiz.XMLStarter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Node;

public class GlobalSettings {
	private static ThreadPoolExecutor ex = null;
	
	public static void parseGlobalSettings (Node globalSettings) {
		Node numThreadsNode = globalSettings.getChildNodes().item(1);
		int numThreads = Integer.parseInt(numThreadsNode.getTextContent());
		
		ex = new ThreadPoolExecutor (numThreads, numThreads, 1, TimeUnit.DAYS,
				new LinkedBlockingQueue<Runnable>());
	}
	
	public static ThreadPoolExecutor getExecutor(){
		return ex;
	}
}
