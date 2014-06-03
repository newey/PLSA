package org.nuiz.parallelRecommend;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * An object to get statistics on how well a model performs predictions.
 * Specifically it computes the RMSE and ABS for a given model, training data, and test data.
 * @author Robert Newey
 */
class Rater {
	double RMS = 0;
	double ABS = 0;
	
	
	/**
	 * Specifies a rater by training DataList, test DataList, and model
	 * @param train
	 * @param test
	 * @param model
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public Rater (DataList train, DataList test, Model model) throws InterruptedException, ExecutionException{
		model.fit(train);
		Iterable <Double> preds = model.predict(test);
		Iterator <Double> predIt = preds.iterator();
		Iterator <Datum> truthIt = test.iterator();
		double cPred, cRat;
		Datum cTruth;
		
		int count = 0;
		
		while (predIt.hasNext()) {
			cPred = predIt.next();
			cTruth = truthIt.next();
			
			cPred = train.denormaliseRating(cTruth.getUser(), cPred);
			cRat = train.denormaliseRating(cTruth.getUser(), cTruth.getRating());
			
			double err = Math.abs(cRat - cPred);
			RMS += err*err;
			ABS += err;
			count++;
		}
		
		ABS /= count;
		RMS /= count;
		RMS = Math.sqrt(RMS);
	}
	
	/**
	 * @return The mean absolute error of the model.
	 */
	public double getABS(){
		return ABS;
	}
	
	/**
	 * @return The root mean squared error of the model.
	 */
	public double getRMS(){
		return RMS;
	}
}
