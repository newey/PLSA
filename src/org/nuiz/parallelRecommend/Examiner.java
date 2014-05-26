package org.nuiz.parallelRecommend;

import java.io.IOException;

import org.nuiz.parallelPLSA.PLSA;

public class Examiner {

	public static void main(String[] args) throws IOException {
		String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/ratings.dat";
		String separator = "::";
		//String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-100k/u.data";
		//String separator = "\t";
		FileDataList rawData = new FileDataList (fileName, separator);
		NormaliseData core = new NormaliseData(rawData, 5);

		//PLSA model = new PLSA(10, 3);
		
		//model.fit(new NormalisedDataList(rawData, core));
		
		
	}

}
