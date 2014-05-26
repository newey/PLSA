package org.nuiz.parallelRecommend;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

public class FileFolder{
	private Vector<LinkedList<Datum>> folds;
	private int numUsers;
	private int numItems;
	
	public FileFolder(String fileName, String separator, int numFolds, int seed) throws IOException{
		Random r = new Random();
		r.setSeed(seed);
		
		folds = new Vector<LinkedList<Datum>>(numFolds);
		for (int i = 0; i < numFolds; i++) {
			folds.add(i, new LinkedList<Datum>());
		}
		
		DataList rawData = new FileDataList(fileName, separator);
		
		for (Datum d : rawData) {
			folds.elementAt(r.nextInt(numFolds)).add(d);
		}
		
		numUsers = rawData.getNumUsers();
		numItems = rawData.getNumItems();
	}
	
	public DataList getFoldTrainingData(int foldNum) {
		return new DataFoldList (folds, foldNum, numUsers, numItems);
	}
	
	public DataList getFoldTestData(int foldNum) {
		return new SingleDataList(folds.elementAt(foldNum), numUsers, numItems);
	}
	
	private class SingleDataList implements DataList {
		Iterable <Datum> data;
		int numUsers;
		int numItems;
		
		public SingleDataList (Iterable<Datum> data, int numUsers, int numItems) {
			this.data = data;
			this.numItems = numItems;
			this.numUsers = numUsers;
		}
		
		@Override
		public Iterator<Datum> iterator() {
			return data.iterator();
		}

		@Override
		public int getNumUsers() {
			return numUsers;
		}

		@Override
		public int getNumItems() {
			return numItems;
		}
		
	}
	
	private class DataFoldList implements DataList {
		private int fold;
		private int numUsers;
		private int numItems;
		private Vector <LinkedList<Datum>> data;
		
		public DataFoldList (Vector <LinkedList<Datum>> data, int fold, int numUsers, int numItems) {
			this.fold = fold;
			this.numUsers = numUsers;
			this.numItems = numItems;
			this.data = data;
		}
		
		@Override
		public Iterator<Datum> iterator() {
			return new DatumIterator(this.data, fold);
		}

		@Override
		public int getNumUsers() {
			return numUsers;
		}

		@Override
		public int getNumItems() {
			return numItems;
		}
		
		private class DatumIterator implements Iterator <Datum> {
			private Vector <LinkedList<Datum>> data;
			private int fold;
			private int currentListIndex;
			private Iterator <Datum> currentIterator;
			private int finalIndex;
			
			public DatumIterator (Vector <LinkedList<Datum>> data, int fold) {
				this.data = data;
				this.fold = fold;
				currentListIndex = 0;
				
				if (currentListIndex == this.fold) {
					currentListIndex++;
				}
				
				finalIndex = data.size()-1 == fold ? data.size()-2 : data.size() -1 ;
				currentIterator = data.elementAt(currentListIndex).iterator();
			}
			
			@Override
			public boolean hasNext() {
				if (currentIterator.hasNext()) {
					return true;
				} if (currentListIndex != finalIndex) {
					// Not quite accurate, relies on all lists having elements in them
					return true;
				}
				return false;
			}

			@Override
			public Datum next() {
				if (!currentIterator.hasNext()) {
					currentListIndex++;
					if (currentListIndex == this.fold) {
						currentListIndex++;
					}
					currentIterator = data.elementAt(currentListIndex).iterator();
				}
				return currentIterator.next();
			}

			@Override
			public void remove() { }
		}
	}
}
