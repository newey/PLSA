package org.nuiz.parallelPLSA;

public class NormalDistributionFactory implements DistributionFactory {

	public Distribution makeDistribution() {
		return new NormalDistribution();
	}

}
