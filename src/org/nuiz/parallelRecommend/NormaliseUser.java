package org.nuiz.parallelRecommend;


/**
 * Performs the acutal work for normalising and denormalising user ratings.
 * @author Robert Newey
 */
class NormaliseUser {
	private double mean = 0;
	private double meansq = 0;
	private int count = 0;
	private NormaliseUser population;
	private double normFactor;
	
	/**
	 * Construct a NormaliseUser, with a population NormaliseUser and a given normFactor
	 * @param population NormaliseUser object referring to the population mean and variance. If this is null, we make no attempt to regularise the normalisation
	 * @param normFactor The factor by which to regularise the normalisation
	 */
	public NormaliseUser(NormaliseUser population, double normFactor) {
		this.population = population;
		this.normFactor = normFactor;
	}
	
	/**
	 * Add an observation to the data for this user.
	 * @param obs observation to add
	 */
	public void addObservation(double obs) {
		count += 1;
		double delta = obs - mean;
		mean += delta/count;
		meansq += delta*(obs - mean);
	}
	
	/**
	 * @return The number of observations of this user
	 */
	public int getCount(){
		return count;
	}
	
	/**
	 * @return The mean for the user.
	 */
	public double getMean(){
		double retval;
		if (population == null || normFactor == 0) {
			retval = mean;
		} else {
			retval = (mean*count + normFactor*population.getMean())/(count + normFactor);
		}
		
		return retval;
	}
	
	/**
	 * @return The variance for the user.
	 */
	public double getVar(){
		double retval;
		if (population == null || normFactor == 0) {
			retval = meansq/(count-1);
		} else {
			retval = (meansq + normFactor*population.getVar())/(count + normFactor);
		}

		return retval;
	}
	
	/**
	 * @param rating A rating to normalise.
	 * @return The normalised rating.
	 */
	public double normalise(double rating) {
		double retval = (rating - getMean())/Math.sqrt(getVar());

		return retval;
	}
	
	/**
	 * @param rating A rating to denormalise.
	 * @return The denormalised rating.
	 */
	public double denormalise(double rating) {
		double retval = rating*Math.sqrt(getVar()) + getMean();

		return retval;
	}
}
