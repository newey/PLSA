package org.nuiz.parallelPLSA;

import java.util.Random;

public class MultinomialDistribution implements Distribution {
	private double params[];
	private double newParams[];
	private int classes;
	
	public MultinomialDistribution (int classes){
		this(classes, new Random());
	}
	
	public MultinomialDistribution(int classes, Random rand) {
		params = new double[classes];
		newParams = new double[classes];
		double sum = 0;
		for (int i = 0; i < classes; i++){
			newParams[i] = 0;
			params[i] = rand.nextDouble() + 1;
			sum += params[i] + 1;
		}
		for (int i = 0; i < classes; i++){
			params[i] /= sum;
		}
		this.classes = classes;
	}
	
	@Override
	public double getProb(double observation) {
		return getProb((int) Math.round(observation));
	}
	
	public double getProb(int observation) {
		return params[observation];
	}

	@Override
	public void addObservation(double observation, double weighting) {
		addObservation((int) Math.round(observation), weighting);
	}

	public void addObservation(int observation, double weighting) {
		newParams[observation] += weighting;
	}	
	
	@Override
	public void updateFit() {
		params = newParams;
		newParams = new double[classes];
		double sum = 0;
		for (int i = 0; i < classes; i++){
			newParams[i] = 0;
			sum += params[i];
		}
		for (int i = 0; i < classes; i++){
			params[i] /= sum;
		}

	}

	@Override
	public double getGuess() {
		double maxSeen = params[0];
		int best = 0;
		for (int i = 1; i < classes; i++){
			if (params[i] > maxSeen) {
				maxSeen = params[i];
				best = i;
			}
		}
		return best;
	}

}
