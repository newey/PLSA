package org.nuiz.similarityEngines;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SimilarityEngine {
	private final Map<Integer, Map<Integer, Double>> similarities =
			new ConcurrentHashMap<Integer, Map<Integer, Double>>();
	private final Map<Integer, Map<Integer, Double>> ratingSets;
	private Map<Integer, Double> lengths = new ConcurrentHashMap<Integer, Double>();
	
	
	protected SimilarityEngine (Map<Integer, Map<Integer, Double>> ratingSets){
		this.ratingSets = ratingSets;
	}
	
	protected abstract double computeLength (int id);
	
	protected double getLength (int id) {
		if (lengths.containsKey(id)) {
			return lengths.get(id);
		}
		double retval = computeLength(id);
		
		lengths.put(id, retval);
		return retval;
	}
	
	
	public double similarity (int a, int b){
		if (b < a){
			a ^= b;
			b ^= a;
			a ^= b;
		}
		Map<Integer, Double> outer = null;
		if (!similarities.containsKey(a)){
			outer = new HashMap<Integer, Double>();
			similarities.put(a, outer);
		} else {
			outer = similarities.get(a);
		}
		
		Double sim;
		if (!outer.containsKey(b)){
			sim = getSimilarity(a, b);
			outer.put(b, sim);
		} else {
			sim = outer.get(b);
		}
		
		return sim;
		
	}
	
	protected Map<Integer, Double> getRatingSet (int id) {
		return ratingSets.get(id);
	}
	
	protected abstract double getSimilarity(int a, int b);

}
