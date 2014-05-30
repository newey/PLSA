package org.nuiz.parallelPLSA;

interface Distribution {
	// returns the probability of a given observation
	public double getProb (double observation);
	
	// returns the guess of the observation for this distriubtion
	public double getGuess ();
	
	// addObservation adds an observation to help the fit, though need not change it
	public void addObservation(double observation, double weighting);
	
	// updateFit then updates the fit based on the most recent set of added observations
	public void updateFit();
}
