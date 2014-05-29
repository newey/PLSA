package org.nuiz.parallelRecommend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import org.nuiz.parallelPLSA.PLSA;

public class Examiner {

	public static void main(String[] args) throws IOException {
		double normFactor = 10;
		int iterations = 10;
		int classes = 2;
		
		String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/ratings.dat";
		String userFileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/users.dat";
		String separator = "::";
		//String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-100k/u.data";
		//String separator = "\t";
		DataList rawData = new FileDataList (fileName, separator);
		DataList userData = new UserFileDataList(userFileName, separator);
		
		NormaliseData core = new NormaliseData(rawData, normFactor);
		PLSA model = new PLSA(iterations, classes, userData);
		model.fit(new NormalisedDataList(rawData, core));
		
		Vector <Vector<OrderedPair<Double, String>>> ratings =
				new Vector <Vector<OrderedPair<Double, String>>>();
		
		HashMap<Integer, String> movieTitles = new HashMap<Integer, String>();
		
		BufferedReader brm = new BufferedReader(new FileReader("/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1M/movies.dat"));
		while (brm.ready()){
			Scanner tk = new Scanner(brm.readLine());
			//tk.useDelimiter("\\|");
			tk.useDelimiter("::");
			int movie = tk.nextInt();
			String title = new String(tk.next());
			movieTitles.put(movie, title);
		}


		brm.close();
		for (int i = 0; i < classes; i++){
			Vector<OrderedPair<Double, String>> cratings = new Vector<OrderedPair<Double, String>>();
			ratings.add(cratings);
			for (Integer movie : rawData.getItems()) {
				cratings.add(new OrderedPair<Double,String>(model.getItemRatingForClass(movie, i), movieTitles.get(movie)));
			}
			Collections.sort(cratings);
			
			System.out.printf("Class %d:\n", i);
			for (int j = 0; j < 30; j++){
				System.out.printf("%f %s:\n", cratings.elementAt(j).a, cratings.elementAt(j).b);
			}
		}
		
		
	}

}
