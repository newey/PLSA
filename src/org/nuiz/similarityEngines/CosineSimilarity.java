package org.nuiz.similarityEngines;

import java.util.Map;
import java.util.Set;

public class CosineSimilarity extends SimilarityEngine {
	
	public CosineSimilarity(Map<Integer, Map<Integer, Double>> ratingSets) {
		super(ratingSets);
	}
	
	protected double computeLength (int id) {
		Map<Integer, Double> ratings = getRatingSet(id);
		double retval = 1e-10;
		for (double rating : ratings.values()) {
			retval += rating*rating;
		}
		retval = Math.sqrt(retval);
		return retval;
	}
	
	@Override
	protected double getSimilarity(int a, int b) {
		double aLen = getLength(a);
		double bLen = getLength(b);
		
		Map<Integer, Double> ratingsA = getRatingSet(a);
		Map<Integer, Double> ratingsB = getRatingSet(b);
		
		Set<Integer> smaller = (ratingsA.size() < ratingsB.size() ? ratingsA : ratingsB).keySet();
		Set<Integer> larger = (ratingsA.size() >= ratingsB.size() ? ratingsA : ratingsB).keySet();
		double dotProduct = 0;
		for (Integer i : smaller) {
			if (larger.contains(i))
				dotProduct += ratingsA.get(i)*ratingsB.get(i);
		}
		return dotProduct/(aLen*bLen);

	}

}
