package org.nuiz.slopeOne;

import java.util.HashMap;

public class MovieStore {
	HashMap<Integer, HashMap<Integer, Double>> slopes;
	HashMap<Integer, HashMap<Integer, Integer>> counts;
	
	public MovieStore() {
		slopes = new HashMap<Integer, HashMap<Integer, Double>>();
		counts = new HashMap<Integer, HashMap<Integer, Integer>>();
	}
	
	public void addDiff (int movie1, int movie2, double diff) {
		if (movie2 < movie1) {
			movie1 ^= movie2;
			movie2 ^= movie1;
			movie1 ^= movie2;
			diff *= -1;
		}
		
		if (!slopes.containsKey(movie1)) {
			slopes.put(movie1, new HashMap<Integer, Double>());
			counts.put(movie1, new HashMap<Integer, Integer>());
		}
		
		if (!slopes.get(movie1).containsKey(movie2)){
			slopes.get(movie1).put(movie2, 0.0);
			counts.get(movie1).put(movie2, 0);
		}

		counts.get(movie1).put(movie2, 1 + counts.get(movie1).get(movie2));
		slopes.get(movie1).put(movie2, diff + slopes.get(movie1).get(movie2));
	}
	
	public double getDiff (int movie1, int movie2) {
		int negate = movie2 < movie1 ? -1 : 1;
		if (movie2 < movie1) {
			movie1 ^= movie2;
			movie2 ^= movie1;
			movie1 ^= movie2;
		}
		
		if (!counts.containsKey(movie1) || !counts.get(movie1).containsKey(movie2) ){
			return 0;
		}
		
		int count = counts.get(movie1).get(movie2);
		double slope = slopes.get(movie1).get(movie2);
		
		return negate*slope/count;
	}
}
