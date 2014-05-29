package org.nuiz.guessUserMean;

import java.util.Vector;

import org.nuiz.parallelRecommend.*;

public class GuessZero implements Model {

	@Override
	public void fit(DataList data) {}

	@Override
	public Iterable<Double> predict(DataList data) {
		Vector <Double> preds = new Vector<Double>();
		
		for (Datum d : data){
			preds.add(0.0);
		}
		return preds;
	}

}
