package org.nuiz.parallelPLSA;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class MultinomialDistribution implements Distribution {
	private double params[];
	private double newParams[];
	private int classes;
	private final Lock lock = new ReentrantLock();
	
	public MultinomialDistribution (int classes){
		this(classes, new Random());
	}
	
	public MultinomialDistribution(int classes, Random rand) {
		lock.lock();
		params = new double[classes];
		newParams = new double[classes];
		double sum = 0;
		for (int i = 0; i < classes; i++){
			newParams[i] = 1e-14;
			params[i] = rand.nextDouble() + 1;
			sum += params[i] + 1;
		}
		for (int i = 0; i < classes; i++){
			params[i] /= sum;
		}
		lock.unlock();
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
		lock.lock();
		params = newParams;
		newParams = new double[classes];
		double sum = 0;
		for (int i = 0; i < classes; i++){
			newParams[i] = 1e-14;
			sum += params[i];
		}
		for (int i = 0; i < classes; i++){
			params[i] /= sum;
		}
		lock.unlock();

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
