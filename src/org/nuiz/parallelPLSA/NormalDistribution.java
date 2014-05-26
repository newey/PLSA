package org.nuiz.parallelPLSA;

public class NormalDistribution implements Distribution {
	double mean;
	double variance;
	double obsXobsXweightsSum;
	double obsXweightsSum;
	double weightsSum;
	
	public NormalDistribution (){
		mean = 0;
		variance = 1;
		obsXobsXweightsSum = 0;
		obsXweightsSum = 0;
		weightsSum = 0;
	}
	
	@Override
	public double getProb(double observation) {
		if (variance == 0) {
			return Math.abs(observation - mean) < 1e-8 ? 1.0 : 0.0;
		}
		double exp = -(observation - mean)*(observation-mean)/(2*variance);
		double base = 1/Math.sqrt(2*Math.PI*variance);
		if (Double.isNaN(base)) {
			throw new ArithmeticException();
		}
		return Math.exp(exp)*base;
	}

	@Override
	public void addObservation(double observation, double weighting) {

		if (Double.isNaN(observation) || Double.isNaN(weighting)) {
			throw new ArithmeticException();
		}
		obsXobsXweightsSum += observation*observation*weighting;
		obsXweightsSum += observation*weighting;
		weightsSum += weighting;
	}

	@Override
	public void updateFit() {
		if (weightsSum == 0) {
			return;
		}
		mean = obsXweightsSum/weightsSum;
		variance = (obsXobsXweightsSum)/weightsSum - mean*mean;

		if (variance < 0 && variance > -(1E-8)) {
			variance = 0;
		} else if (variance < 0) {
			throw new ArithmeticException();
		}
		
		obsXobsXweightsSum = 0;
		obsXweightsSum = 0;
		weightsSum = 0;
	}

	@Override
	public double getGuess() {
		return mean;
	}

}
