package org.nuiz.parallelRecommend;

import java.io.IOException;

import org.nuiz.parallelPLSA.PLSA;

public class Runner {

	public static void main(String[] args) throws IOException {
		int folds = 10;
		
		String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/ratings.dat";
		String separator = "::";
		//String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-100k/u.data";
		//String separator = "\t";
		FileFolder rawData = new FileFolder (fileName, separator, folds, 1);
		
		for (int i = 0; i < folds; i++) {
			NormaliseData core = new NormaliseData(rawData.getFoldTrainingData(i), 5);
			DataList train = new NormalisedDataList(rawData.getFoldTrainingData(i), core);
			DataList test = new NormalisedDataList(rawData.getFoldTestData(i), core);
			Model m = new PLSA(10, 3);
			Rater r = new Rater(train, test, m, core);
			System.out.printf("Fold: %d\tRMS: %f\tABS: %f\n", i, r.getRMS(), r.getABS());
		}

	}

}
