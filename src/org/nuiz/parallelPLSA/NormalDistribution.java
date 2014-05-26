package org.nuiz.parallelPLSA;

import java.util.Vector;

public class NormalDistribution implements Distribution {
	double mean;
	double variance;
	Vector <Double> observations;
	Vector <Double> weights;
	double observationsSum;
	double weightingsSum;
	
	public NormalDistribution (){
		mean = 0;
		variance = 1;
		observations = new Vector<Double>();
		weights = new Vector<Double>();
		observationsSum = 0;
		weightingsSum = 0;
	}
	
	@Override
	public double getProb(double observation) {
		if (variance == 0) {
			return observation == mean ? 1.0 : 0.0;
		}
		double exp = -(observation - mean)*(observation-mean)/(2*variance);
		double base = 1/Math.sqrt(2*Math.PI*variance);

		return Math.exp(exp)*base;
	}

	@Override
	public void addObservation(double observation, double weighting) {
		observations.add(observation);
		weights.add(weighting);
		observationsSum += observation*weighting;
		weightingsSum += weighting;
	}

	@Override
	public void updateFit() {
		if (observations.size() == 0) {
			return;
		}

		mean = observationsSum/weightingsSum;

		variance = 0;
		for (int i = 0; i < observations.size(); i++){
			double delta = observations.elementAt(i) - mean;
			variance += delta *delta*weights.elementAt(i);
		}
		variance /= weightingsSum;
		
		observations = new Vector<Double>();
		weights = new Vector<Double>();
		observationsSum = 0;
		weightingsSum = 0;
	}

	@Override
	public double getGuess() {
		return mean;
	}

}
