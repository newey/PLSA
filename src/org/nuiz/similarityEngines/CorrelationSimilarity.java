package org.nuiz.similarityEngines;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CorrelationSimilarity extends SimilarityEngine {
	Map <Integer, Double> primaryAvg = new ConcurrentHashMap<Integer,Double>();
	
	
	public CorrelationSimilarity(
			Map<Integer, Map<Integer, Double>> ratingSets) {
		super(ratingSets);
	}

	private double getAvg(int id) {
		if (primaryAvg.containsKey(id)) {
			return primaryAvg.get(id);
		}
		double retval = 0;
		int count = 0;
		
		for (double rating : getRatingSet(id).values()) {
			retval += rating;
			count++;
		}
		retval /= count;
		
		primaryAvg.put(id, retval);
		return retval;
	}
	
	@Override
	protected double computeLength(int id) {
		Map<Integer, Double> ratings = getRatingSet(id);
		double retval = 1e-10;
		double itemAvg = getAvg(id);
		
		for (int orthogId : ratings.keySet()) {
			retval += (ratings.get(orthogId) - itemAvg)*(ratings.get(orthogId) - itemAvg);
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
		
		double aAvg = getAvg(a);
		double bAvg = getAvg(b);
		
		double dotProduct = 0;
		for (Integer orthogId : smaller) {
			if (larger.contains(orthogId)){
				dotProduct += (ratingsA.get(orthogId)-aAvg)*(ratingsB.get(orthogId)-bAvg);
			}
		}
		return dotProduct/(aLen*bLen);
	}

	
	@Override
	public String toString(){
		return "CorrelationSimilarity";
	}
	
}
