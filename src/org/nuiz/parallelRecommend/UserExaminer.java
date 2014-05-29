package org.nuiz.parallelRecommend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

import org.nuiz.parallelPLSA.PLSA;
import org.nuiz.slopeOne.SlopeOne;

public class UserExaminer {

	public static void main(String[] args) throws IOException {
		double normFactor = 10;
		int iterations = 20;
		int classes = 5;
		
		String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/ratings.dat";
		String userFileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1m/users.dat";
		String separator = "::";
		//String fileName = "/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-100k/u.data";
		//String separator = "\t";
		DataList rawData = new FileDataList (fileName, separator);
		DataList userData = new UserFileDataList(userFileName, separator);
		
		NormaliseData core = new NormaliseData(rawData, normFactor);
		Model model = new PLSA(iterations, classes, userData);
		//Model model = new SlopeOne();
		model.fit(new NormalisedDataList(rawData, core));
		
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
		
		int[] usersToScore = new int[] {21};
		
		for (Integer i : usersToScore) {
			Vector <Datum> toRate = new Vector<Datum>();
			for (int j : rawData.getItems()) {
				toRate.add(new Datum(i, j, 0, 0));
			}
			DataList dl = new DataListImpl(toRate);
			Iterator<Double> predIt = model.predict(dl).iterator();
			Vector <OrderedPair<Double, Integer>> ratingsToMovies = new Vector <OrderedPair<Double, Integer>>();
			Iterator<Datum> dataIt = dl.iterator();
			while (predIt.hasNext()){
				ratingsToMovies.add(new OrderedPair <Double, Integer>(predIt.next(), dataIt.next().getItem()));
			}
			Collections.sort(ratingsToMovies);
			
			for (OrderedPair<Double, Integer> op : ratingsToMovies){
				System.out.printf("%d %d %f %s\n", i, numMovieRatings.get(op.b), op.a, movieTitles.get(op.b));
			}
		}


	}

}
