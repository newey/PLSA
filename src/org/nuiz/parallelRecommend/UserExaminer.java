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
import java.util.concurrent.ExecutionException;

import org.nuiz.utils.OrderedPair;

/**
 * Gets predictions for a given user after training.
 * @author Robert Newey
 */
public class UserExaminer {

	/**
	 * Writes predictions to an OutputStream in lines of the form:
	 * <ul>
	 * <li>User number</li>
	 * <li>Number of ratings the movie has</li>
	 * <li>Predicted (denormalised) user rating</li>
	 * <li>Movie title</li>
	 * </ul>
	 * @param dataList DataList to train on
	 * @param model Model to use
	 * @param user User id to predict for
	 * @param moviesFile Path to file with movie titles in it.
	 * @param separator Separator to use in the moviesFile
	 * @param outputFile Where to write output.
	 * @throws Exception 
	 */
	public UserExaminer (DataList dataList, Model model, int user, String moviesFile, String separator, OutputStream outputFile) throws Exception {
		model.fit(dataList);
		PrintStream outPrint = new PrintStream(outputFile);
		HashMap<Integer, String> movieTitles = new HashMap<Integer, String>();
		
		BufferedReader brm = new BufferedReader(new FileReader(moviesFile));
		while (brm.ready()){
			Scanner tk = new Scanner(brm.readLine());
			tk.useDelimiter(separator);
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
