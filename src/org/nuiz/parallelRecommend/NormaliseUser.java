package org.nuiz.parallelRecommend;


class NormaliseUser {
	private double mean = 0;
	private double meansq = 0;
	private int count = 0;
	private NormaliseUser population;
	private double normFactor;
	
	// norm can be null! if it is, ignore it
	public NormaliseUser(NormaliseUser population, double normFactor) {
		this.population = population;
		this.normFactor = normFactor;
	}
	
	public void addObservation(double obs) {
		count += 1;
		double delta = obs - mean;
		mean += delta/count;
		meansq += delta*(obs - mean);
	}
	
	public int getCount(){
		return count;
	}
	
	public double getMean(){
		double retval;
		if (population == null || normFactor == 0) {
			retval = mean;
		} else {
			retval = (mean*count + normFactor*population.getMean())/(count + normFactor);
		}
		
		return retval;
	}
	
	public double getVar(){
		double retval;
		if (population == null || normFactor == 0) {
			retval = meansq/(count-1);
		} else {
			retval = (meansq + normFactor*population.getVar())/(count + normFactor);
		}

		return retval;
	}
	
	public double normalise(double rating) {
		double retval = (rating - getMean())/Math.sqrt(getVar());

		return retval;
	}
	
	public double denormalise(double rating) {
		double retval = rating*Math.sqrt(getVar()) + getMean();

		return retval;
	}
}
