package org.nuiz.parallelRecommend;

import java.util.concurrent.ExecutionException;

/**
 * The interface defining models in this system.
 * @author Robert Newey
 */
public interface Model {
	/**
	 * Fit a model to the training data given.
	 * @param data The data for the model to train on
	 */
	public void fit(DataList data);
	
	/**
	 * Perform prediction based on the model fit. It is only valid to call this after fit has been called
	 * @param data Data to perform prediction on
	 * @return An Iterable over the predictions in the same order as were given in data.
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 * @throws Exception 
	 */
	public Iterable<Double> predict(DataList data) throws InterruptedException, ExecutionException, Exception;
	
	public String getDescription();
}
