package org.nuiz.parallelRecommend;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

/**
 * Gets the ABS and RMSE over a holdout set for a model.
 * @author Robert Newey
 */
public class HoldoutRunner {
	DataList dataList;
	Model model;
	
	/**
	 * Gets error statistics for a holdout set, given a model
	 * @param data Data to perform holdout with
	 * @param model Model to use
	 * @param seed Random seed to use.
	 * @param testSize Size of the test set
	 * @param outputFile Where to write the output to
	 * @throws Exception 
	 */
	public HoldoutRunner(DataList data, Model model, int seed, int testSize,
			OutputStream outputFile) throws Exception {		
		PrintStream outPrint = new PrintStream(outputFile);
		
		DataList testData = data.getSubDataList(0, testSize);
		DataList trainData = data.getSubDataList(testSize, data.getSize());
		
		
		Rater r = new Rater(trainData, testData, model);
		
		outPrint.printf("%f,%f,%s\n", r.ABS, r.RMS, model.getDescription());
	}
}
