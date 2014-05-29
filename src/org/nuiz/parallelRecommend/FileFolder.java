package org.nuiz.parallelRecommend;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class FileFolder{
	private Vector<Vector<Datum>> folds;
	private int maxItem;
	private Set<Integer> users;
	private Set<Integer> items;
	
	public FileFolder(String fileName, String separator, int numFolds, int seed) throws IOException{
		Random r = new Random();
		r.setSeed(seed);
		
		folds = new Vector<Vector<Datum>>(numFolds);
		for (int i = 0; i < numFolds; i++) {
			folds.add(i, new Vector<Datum>());
		}
		
		DataList rawData = new FileDataList(fileName, separator);
		
		for (Datum d : rawData) {
			folds.elementAt(r.nextInt(numFolds)).add(d);
		}

		maxItem = rawData.getMaxItem();
		users = rawData.getUsers();
		items = rawData.getItems();
	}
	
	public DataList getFoldTrainingData(int foldNum) {
		Vector <Vector<Datum>> cFolds = new Vector<Vector<Datum>>(folds.size()-1);
		for (int i = 0; i < folds.size(); i++){
			if (i != foldNum){
				cFolds.add(folds.elementAt(i));
			}
		}
		return new DataFoldList (cFolds, users, items, maxItem);
	}
	
	public DataListImpl getFoldTestData(int foldNum) {
		return new DataListImpl(folds.elementAt(foldNum));
	}
}
