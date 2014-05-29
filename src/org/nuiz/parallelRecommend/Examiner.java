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
		int iterations = 30;
		int classes = 20;
		
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
		
		Vector <Vector<OrderedPair<Double, Integer>>> ratings =
				new Vector <Vector<OrderedPair<Double, Integer>>>();
		
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

		HashMap<Integer, Integer> numMovieRatings = new HashMap<Integer, Integer>();
		for (Datum d: rawData) {
			if (!numMovieRatings.containsKey(d.getItem())){
				numMovieRatings.put(d.getItem(), 1);
			} else {
				numMovieRatings.put(d.getItem(), numMovieRatings.get(d.getItem())+1);
			}
		}
		
		brm.close();
		for (int i = 0; i < classes; i++){
			Vector<OrderedPair<Double, Integer>> cratings = new Vector<OrderedPair<Double, Integer>>();
			ratings.add(cratings);
			for (Integer movie : rawData.getItems()) {
				cratings.add(new OrderedPair<Double,Integer>(model.getItemRatingForClass(movie, i), movie));
			}
			Collections.sort(cratings);
			
			for (OrderedPair<Double, Integer> op : cratings){
				System.out.printf("%d %d %f %s\n", i, numMovieRatings.get(op.b), op.a, movieTitles.get(op.b));
			}
		}
		
		
	}

}
