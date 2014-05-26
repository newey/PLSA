package org.nuiz.parallelPLSA;

import java.util.HashMap;

public class ClassAttributeToDistributionMap {
	HashMap <Integer, Distribution>[] distributions;
	int classes;
	
	@SuppressWarnings("unchecked")
	public ClassAttributeToDistributionMap (int classes) {
		distributions = ((HashMap<Integer, Distribution>[]) new HashMap[classes]);
		for (int i = 0; i < classes; i++){
			distributions[i] = new HashMap<Integer, Distribution>();
		}
		this.classes = classes;
	}
	
	public void addDisribution (int key, DistributionFactory distFactory) {
		for (int i = 0; i < classes; i++) {
			distributions[i].put(key, distFactory.makeDistribution());
		}
	}
	
	public void addObservation(int key, int cl, double observation, double weighting) {
		distributions[cl].get(key).addObservation(observation, weighting);
	}
	
	public double getProb (int key, int cl, double observation) {
		double retval = distributions[cl].get(key).getProb(observation);
		if (Double.isNaN(retval)) {
			throw new ArithmeticException();
		}
		return retval;
	}
	
	public double getGuess (int key, int cl){
		if (!distributions[cl].containsKey(key)) {
			return 0;
		}
		return distributions[cl].get(key).getGuess();
	}
	
	public void finaliseRound(){
		for (int i = 0; i < classes; i++){
			for (Distribution d : distributions[i].values()) {
				d.updateFit();
			}
		}
	}
}
