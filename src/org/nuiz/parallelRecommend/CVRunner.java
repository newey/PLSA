package org.nuiz.parallelRecommend;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;
import java.util.concurrent.ExecutionException;


/**
 * Runs cross validation over the dataset
 * 
 * @author Robert Newey
 */
public class CVRunner {

	/**
	 * Runs cross validation over dataList, given some model, randomSeed, number of folds,
	 * and outputs to the given OutputStream
	 * @param dataList The data to perform CV on
	 * @param model The model to perform CV with
	 * @param randomSeed The random seed to use
	 * @param folds The number of folds to perform
	 * @param out Where to write output
	 * @throws Exception 
	 */
	public CVRunner(DataList dataList, Model model, int randomSeed, int folds,
			OutputStream out) throws Exception {
		double RMS = 0;
		double RMS2 = 0;
		double ABS = 0;
		double ABS2 = 0;
	
		PrintStream outPrint = new PrintStream(out);
		int foldSize = dataList.getSize()/folds;
		Vector <Integer> boundaries = new Vector<Integer>();
		for (int i = 0; i < folds; i++){
			boundaries.add(i*foldSize);
		}
		boundaries.add(dataList.getSize());
		
		for (int i = 0; i < folds; i++) {
			DataList train = null;
			if (i == 0){
				train = dataList.getSubDataList(boundaries.elementAt(1), dataList.getSize());
			} else if (i == folds-1) {
				train = dataList.getSubDataList(0, boundaries.elementAt(i));
			} else {
				Vector <DataList> dLists = new Vector<DataList> ();
				DataList first = dataList.getSubDataList(0, boundaries.elementAt(i));
				DataList second = dataList.getSubDataList(boundaries.elementAt(i), dataList.getSize());
				dLists.add(first);
				dLists.add(second);
				train = new DataListMultiplexer(dLists, dataList.getUsers(), dataList.getItems(),dataList.getMaxItem());
			}
			
			
			DataList test = dataList.getSubDataList(boundaries.elementAt(i), boundaries.elementAt(i+1));

			Rater r = new Rater(train, test, model);
			double delta = r.RMS - RMS;
			RMS += delta/(i+1);
			RMS2 += delta*(r.RMS - RMS);
			
			delta = r.ABS - ABS;
			ABS += delta/(i+1);
			ABS2 += delta*(r.ABS - ABS);
			//outPrint.printf("ABS: %f\t RMS: %f\n", r.ABS, r.RMS);
		}
		// (RMS, RMS(sd), ABS, ABS(sd), model description)
		outPrint.printf("%f,%f,%f,%f,%s\n", RMS, Math.sqrt(RMS2/(folds-1)),ABS, Math.sqrt(ABS2/(folds-1)),model.getDescription());

	}

}
