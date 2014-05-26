package org.nuiz.parallelRecommend;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class FileFolder{
	private Vector<LinkedList<Datum>> folds;
	private int numUsers;
	private int numItems;
	private int maxItem;
	private Set<Integer> users;
	private Set<Integer> items;
	
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
		maxItem = rawData.getMaxItem();
		users = rawData.getUsers();
		items = rawData.getItems();
	}
	
	public DataList getFoldTrainingData(int foldNum) {
		return new DataFoldList (folds, foldNum, numUsers, numItems, maxItem, users, items);
	}
	
	public DataList getFoldTestData(int foldNum) {
		return new SingleDataList(folds.elementAt(foldNum), numUsers, numItems, maxItem, users, items);
	}
	
	private class SingleDataList implements DataList {
		private Iterable <Datum> data;
		private int numUsers;
		private int numItems;
		private int maxItem;
		private Set<Integer> users;
		private Set<Integer> items;
		
		public SingleDataList (Iterable<Datum> data, int numUsers, int numItems, int maxItem, Set<Integer> users, Set<Integer> items) {
			this.data = data;
			this.numItems = numItems;
			this.numUsers = numUsers;
			this.maxItem = maxItem;
			this.users = users;
			this.items = items;
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

		@Override
		public int getMaxItem() {
			return maxItem;
		}

		@Override
		public Set<Integer> getUsers() {
			return users;
		}

		@Override
		public Set<Integer> getItems() {
			return items;
		}
		
	}
	
	private class DataFoldList implements DataList {
		private int fold;
		private int numUsers;
		private int numItems;
		private Vector <LinkedList<Datum>> data;
		private int maxItem;
		private Set<Integer> users;
		private Set<Integer> items;
		
		public DataFoldList (Vector <LinkedList<Datum>> data, int fold, int numUsers, int numItems, int maxItem, Set<Integer> users, Set<Integer> items) {
			this.fold = fold;
			this.numUsers = numUsers;
			this.numItems = numItems;
			this.data = data;
			this.maxItem = maxItem;
			this.users = users;
			this.items = items;
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

		@Override
		public int getMaxItem() {
			return maxItem;
		}

		@Override
		public Set<Integer> getUsers() {
			return users;
		}

		@Override
		public Set<Integer> getItems() {
			return items;
		}
	}
}
