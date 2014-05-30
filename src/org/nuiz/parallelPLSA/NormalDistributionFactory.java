package org.nuiz.parallelPLSA;

class NormalDistributionFactory implements DistributionFactory {

	public Distribution makeDistribution() {
		return new NormalDistribution();
	}

}
