package org.nuiz.parallelRecommend;

import java.util.Iterator;

public class Rater {
	double RMS = 0;
	double ABS = 0;
	
	
	public Rater (DataList train, DataList test, Model model, NormaliseData normaliser){
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
			if (normaliser != null) {
				cPred = normaliser.denormaliseUserRating(cTruth.getUser(), cPred);
				cRat = normaliser.denormaliseUserRating(cTruth.getUser(), cTruth.getRating());
			} else {
				cRat = cTruth.getRating();
			}
			
			double err = Math.abs(cRat - cPred);
			RMS += err*err;
			ABS += err;
			count++;
		}
		
		ABS /= count;
		RMS /= count;
		RMS = Math.sqrt(RMS);
	}
	
	public double getABS(){
		return ABS;
	}
	
	public double getRMS(){
		return RMS;
	}
}
