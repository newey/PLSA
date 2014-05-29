package org.nuiz.parallelRecommend;

import java.io.IOException;

import org.nuiz.parallelPLSA.PLSA;

public class Runner {

	public static void main(String[] args) throws IOException {
		int folds = 5;
		double normFactor = 10;
		int iterations = 20;
		int classes = 20;
		
		String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/ratings.dat";
		String userFileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/users.dat";
		String separator = "::";
		//String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-100k/u.data";
		//String separator = "\t";
		FileFolder rawData = new FileFolder (fileName, separator, folds, 1);
		DataList userData = new UserFileDataList(userFileName, separator);
		
		
		double RMS = 0;
		double RMS2 = 0;
		double ABS = 0;
		double ABS2 = 0;
		
		for (int i = 0; i < folds; i++) {
			DataList trainingData = rawData.getFoldTrainingData(i);
			NormaliseData core = new NormaliseData(trainingData, normFactor);
			DataList train = new NormalisedDataList(trainingData, core);
			DataList test = new NormalisedDataList(rawData.getFoldTestData(i), core);
			Model m = new PLSA(iterations, classes, userData);
			Rater r = new Rater(train, test, m, core);
			double delta = r.RMS - RMS;
			RMS += delta/(i+1);
			RMS2 += delta*(r.RMS - RMS);
			
			delta = r.ABS - ABS;
			ABS += delta/(i+1);
			ABS2 += delta*(r.ABS - ABS);
			System.out.printf("ABS: %f\t RMS: %f\n", r.ABS, r.RMS);
		}

		System.out.printf("Folds: %d\tIterations: %d\tClasses: %d\tNorm fac: %f\n", folds, iterations, classes, normFactor);
		System.out.printf("RMS: %f\t RMS SD: %f\n", RMS, Math.sqrt(RMS2/(folds-1)));
		System.out.printf("ABS: %f\t ABS SD: %f\n", ABS, Math.sqrt(ABS2/(folds-1)));

	}

}
