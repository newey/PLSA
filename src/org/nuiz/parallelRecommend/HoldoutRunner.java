package org.nuiz.parallelRecommend;

import java.io.IOException;

import org.nuiz.parallelPLSA.PLSA;

public class HoldoutRunner {
	public static void main(String[] args) throws IOException {
		int folds = 5;
		double normFactor = 10;
		int iterations = 60;
		int classes = 5;
		
		String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/ratingsBin.dat";
		String userFileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/users.dat";
		//String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-10M100K/ratingsBin.dat";
		String separator = "::";
		//String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-100k/u.data";
		//String separator = "\t";
		//FileFolder rawData = new FileFolder (fileName, separator, folds, 1);
		FileFolder rawData = new FileFolder (fileName, separator, folds, 1, true);
		//DataList userData = new UserFileDataList(userFileName, separator);
	
		System.out.printf("Read in input");
		
		DataList userData = null;
		
		
		DataList trainingData = rawData.getFoldTrainingData(0);
		NormaliseData core = new NormaliseData(trainingData, normFactor);
		DataList train = new NormalisedDataList(trainingData, core);
		DataList test = new NormalisedDataList(rawData.getFoldTestData(0), core);
		Model m = new PLSA(iterations, classes, userData);
		//Model m = new GuessZero();
		//Model m = new SlopeOne();
		Rater r = new Rater(train, test, m, core);
		

		System.out.printf("Folds: %d\tIterations: %d\tClasses: %d\tNorm fac: %f\n", folds, iterations, classes, normFactor);
		System.out.printf("ABS: %f\t RMS: %f\n", r.ABS, r.RMS);
		


	}
}
