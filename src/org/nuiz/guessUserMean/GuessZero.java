package org.nuiz.guessUserMean;

import java.util.Iterator;
import java.util.Vector;

import org.nuiz.parallelRecommend.*;

public class GuessZero implements Model {

	@Override
	public void fit(DataList data) {}

	@Override
	public Iterable<Double> predict(DataList data) {
		Vector <Double> preds = new Vector<Double>();
		
		for (Iterator<Datum> iterator = data.iterator(); iterator.hasNext();) {
			iterator.next();
			preds.add(0.0);
		}
		return preds;
	}

	@Override
	public String getDescription() {
		return String.format("guessZero");
	}

}
