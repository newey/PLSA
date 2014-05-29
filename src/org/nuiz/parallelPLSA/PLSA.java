package org.nuiz.parallelPLSA;

import java.util.LinkedList;

import org.nuiz.parallelRecommend.Model;
import org.nuiz.parallelRecommend.Datum;
import org.nuiz.parallelRecommend.DataList;
import org.nuiz.parallelRecommend.UserFileDataList;

public class PLSA implements Model{
	private int steps;
	private int classes;
	private UserClasses userClasses;
	private ClassAttributeToDistributionMap caTdm;
	private DataList userData;
	
	public PLSA (int steps, int classes, DataList userData) {
		this.steps = steps;
		this.classes = classes;
		userClasses = new UserClasses(classes);
		caTdm = new ClassAttributeToDistributionMap(classes);
		this.userData = userData;

		caTdm.addDisribution(UserFileDataList.AGE_ITEM, new MultinomialDistributionFactory(UserFileDataList.AGE_CLASSES));
		caTdm.addDisribution(UserFileDataList.GENDER_ITEM, new MultinomialDistributionFactory(UserFileDataList.GENDER_CLASSES));
		caTdm.addDisribution(UserFileDataList.OCCUPATION_ITEM, new MultinomialDistributionFactory(UserFileDataList.OCCUPATION_CLASSES));
	}
	
	public PLSA (int steps, int classes) {
		this.steps = steps;
		this.classes = classes;
		userClasses = new UserClasses(classes);
		caTdm = new ClassAttributeToDistributionMap(classes);
		this.userData = null;
	}
	
	@Override
	public void fit(DataList data) {
		DistributionFactory distFactory = new NormalDistributionFactory();
		for (Integer i : data.getItems()){
			caTdm.addDisribution(i, distFactory);
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
		for (DataList dl : userData != null ? new DataList[]{data, userData} : new DataList[]{data}) {
			for (Datum d : dl) {
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
		}
		userClasses.finaliseRound();
		caTdm.finaliseRound();
	}
	
	public double getItemRatingForClass(int item, int cl){
		return caTdm.getGuess(item, cl);
	}
	
}
