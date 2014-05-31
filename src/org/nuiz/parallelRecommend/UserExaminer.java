package org.nuiz.parallelRecommend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

public class UserExaminer {

	public UserExaminer (DataList dataList, Model model, int user, OutputStream outputFile) throws IOException {
		model.fit(dataList);
		PrintStream outPrint = new PrintStream(outputFile);
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
		for (Datum d: dataList) {
			if (!numMovieRatings.containsKey(d.getItem())){
				numMovieRatings.put(d.getItem(), 1);
			} else {
				numMovieRatings.put(d.getItem(), numMovieRatings.get(d.getItem())+1);
			}
		}
		
		brm.close();
		
		int[] usersToScore = new int[] {user};
		
		for (Integer i : usersToScore) {
			Vector <Datum> toRate = new Vector<Datum>();
			for (int j : dataList.getItems()) {
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
				outPrint.printf("%d %d %f %s\n", i, numMovieRatings.get(op.b), dataList.denormaliseRating(i, op.a), movieTitles.get(op.b));
			}
		}


	}

}
