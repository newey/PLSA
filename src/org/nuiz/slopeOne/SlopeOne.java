package org.nuiz.slopeOne;

import java.util.HashMap;
import java.util.Vector;

import org.nuiz.parallelRecommend.*;


public class SlopeOne implements Model {
	MovieStore movieStore;
	HashMap <Integer, Vector<Pair<Integer, Double>>> usersToMovies;
	
	@Override
	public void fit(DataList data) {
		movieStore = new MovieStore();
		usersToMovies = new HashMap <Integer, Vector<Pair<Integer, Double>>>();
		int count = 0;
		
		for (Datum d : data) {
			if (usersToMovies.containsKey(d.getUser())) {
				for (Pair<Integer, Double> prev : usersToMovies.get(d.getUser())) {
					movieStore.addDiff(d.getItem(), prev.a, d.getRating() - prev.b);	
				}
			} else {
				usersToMovies.put(d.getUser(), new Vector<Pair<Integer,Double>>());
			}
			usersToMovies.get(d.getUser()).add(new Pair<Integer, Double>(d.getItem(), d.getRating()));
			count++;
			if (count % 100000 == 0){
				System.err.printf("%d\n", count);
			}
		}
		System.err.printf("Completed a fit!\n");
	}

	@Override
	public Iterable<Double> predict(DataList data) {
		Vector <Double> preds = new Vector<Double>();
		for (Datum d : data) {
			double guess = 0;
			int count = 0;
			for (Pair<Integer, Double> oldR : usersToMovies.get(d.getUser())) {
				guess += oldR.b + movieStore.getDiff(d.getItem(), oldR.a);
				count++;
			}
			preds.add(count != 0? guess/count : 0);
		}
		System.err.printf("Completed a pred!\n");
		return preds;
	}

}
