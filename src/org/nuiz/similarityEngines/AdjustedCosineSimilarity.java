package org.nuiz.similarityEngines;

import java.util.Map;
import java.util.Set;

public class AdjustedCosineSimilarity extends SimilarityEngine {
	Map<Integer, Double> orthogAverages;
	
	public AdjustedCosineSimilarity(
			Map<Integer, Map<Integer, Double>> ratingSets,
			Map<Integer, Double> orthogAverages) {
		super(ratingSets);
		this.orthogAverages = orthogAverages;
	}

	protected double computeLength (int id) {
		Map<Integer, Double> ratings = getRatingSet(id);
		double retval = 1e-10;
		
		for (int orthogId : ratings.keySet()) {
			double orthogAverage = orthogAverages.get(orthogId);
			retval += (ratings.get(orthogId) - orthogAverage)*(ratings.get(orthogId) - orthogAverage);
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
		double support = 0;
		for (Integer orthogId : smaller) {
			if (larger.contains(orthogId)){
				dotProduct += (ratingsA.get(orthogId)-orthogAverages.get(orthogId))*
						(ratingsB.get(orthogId)-orthogAverages.get(orthogId));
				support += 1;
			}	
		}
		return dotProduct/(aLen*bLen);

	}

}
