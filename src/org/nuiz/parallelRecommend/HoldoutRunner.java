package org.nuiz.parallelRecommend;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class HoldoutRunner {
	DataList dataList;
	Model model;
	
	public HoldoutRunner(DataList data, Model model, int seed, int testSize,
			OutputStream outputFile) throws IOException {		
		PrintStream outPrint = new PrintStream(outputFile);
		
		DataList testData = data.getSubDataList(0, testSize);
		DataList trainData = data.getSubDataList(testSize, data.getSize());
		
		
		Rater r = new Rater(trainData, testData, model);
		
		outPrint.printf("ABS: %f\t RMS: %f\n", r.ABS, r.RMS);
	}
}
