package org.nuiz.parallelPLSA;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserClasses {
	private Map <Integer, MultinomialDistribution> userToClassProb;
	private int numClasses;
	
	public UserClasses (int numClasses) {
		this.numClasses = numClasses;
		userToClassProb = new ConcurrentHashMap<Integer, MultinomialDistribution>();
	}
	
	public double getProb (int user, int cl){
		if (!userToClassProb.containsKey(user)) {
			userToClassProb.put(user, new MultinomialDistribution(numClasses));
		}
		double retval = userToClassProb.get(user).getProb(cl);
		if (Double.isNaN(retval)) {
			throw new ArithmeticException();
		}
		return retval;
	}
	
	public void addUserClassWeight (int user, int cl, double weight) {
		userToClassProb.get(user).addObservation(cl, weight);
	}
	
	public void finaliseRound() {
		for (MultinomialDistribution md : userToClassProb.values()) {
			md.updateFit();
		}
	}

}
