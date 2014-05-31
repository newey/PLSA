package org.nuiz.parallelRecommend;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

class FileFolder{
	private Vector<List<Datum>> folds;
	private int maxItem;
	private Set<Integer> users;
	private Set<Integer> items;
	
	public FileFolder(String fileName, String separator, int numFolds, int seed, boolean isBinary) throws IOException{
		Random r = new Random();
		r.setSeed(seed);
		
		folds = new Vector<List<Datum>>(numFolds);
		for (int i = 0; i < numFolds; i++) {
			folds.add(i, new Vector<Datum>());
		}
		
		DataList rawData = isBinary ? new BinaryFileDataList(fileName) : new FileDataList(fileName, separator);
		
		for (Datum d : rawData) {
			folds.elementAt(r.nextInt(numFolds)).add(d);
		}

		maxItem = rawData.getMaxItem();
		users = rawData.getUsers();
		items = rawData.getItems();
	}
	
	public DataList getFoldTrainingData(int foldNum) {
		Vector <List<Datum>> cFolds = new Vector<List<Datum>>(folds.size()-1);
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
