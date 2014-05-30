package org.nuiz.parallelPLSA;

class MultinomialDistributionFactory implements DistributionFactory {
	int classes;
	public MultinomialDistributionFactory(int classes){
		this.classes = classes;
	}
	
	@Override
	public Distribution makeDistribution() {
		return new MultinomialDistribution(classes);
	}

}
