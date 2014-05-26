package org.nuiz.parallelPLSA;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.nuiz.parallelRecommend.Model;
import org.nuiz.parallelRecommend.Datum;
import org.nuiz.parallelRecommend.DataList;

public class PLSA implements Model{
	private int steps;
	private int classes;
	private UserClasses userClasses;
	private ClassAttributeToDistributionMap caTdm;
	
	public PLSA (int steps, int classes) {
		this.steps = steps;
		this.classes = classes;
		userClasses = new UserClasses(classes);
		caTdm = new ClassAttributeToDistributionMap(classes);
	}
	
	@Override
	public void fit(DataList data) {
		DistributionFactory distFactory = new NormalDistributionFactory();
		Set<Integer> movies = new HashSet<Integer>();
		for (Datum d : data){
			if (!movies.contains(d.getItem())){
				caTdm.addDisribution(d.getItem(), distFactory);
			}
		}
		
		for (int i = 0; i < steps; i++) {
			emStep(data);
		}
	}

	@Override
	public Iterable<Double> predict(DataList data) {
		LinkedList<Double> results = new LinkedList<Double>();
		
		for (Datum d : data) {
			double guess = 0;
			for (int c = 0; c < classes; c++){
				guess += userClasses.getProb(d.getUser(), c)*caTdm.getGuess(d.getItem(), c);
			}
			results.add(guess);
		}
		return results;
	}

	private void emStep(DataList data){
		double[] perClass = new double[classes];
		double sum = 0;
		for (Datum d : data) {
			sum = 0;
			for (int c = 0; c < classes; c++){
				perClass[c] = caTdm.getProb(d.getItem(), c, d.getRating())*
						userClasses.getProb(d.getUser(), c);
				sum += perClass[c];
			}
			for (int c = 0; c < classes; c++){
				perClass[c] /= sum;

				userClasses.addUserClassWeight(d.getUser(), c, perClass[c]);
				caTdm.addObservation(d.getItem(), c, d.getRating(), perClass[c]);
			}
		}
		userClasses.finaliseRound();
		caTdm.finaliseRound();
		
	}
	
}
