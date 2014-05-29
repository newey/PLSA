package org.nuiz.parallelRecommend;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


public class DataFoldList implements DataList {
	Vector <Vector<Datum>> data;
	Set <Integer> users;
	Set <Integer> items;
	int maxItem = -1;
	int size = 0;
	
	public DataFoldList (Vector <Vector<Datum>> data, Set<Integer> users, Set<Integer> items, int maxItem) {
		this.data=data;
		this.users = users;
		this.items = items;
		this.maxItem = maxItem;
		for (Vector <Datum> vd : data) {
			size += vd.size();
		}

	}
	
	@Override
	public Iterator<Datum> iterator() {
		Vector<Iterator<Datum>> v = new Vector<Iterator<Datum>>();
		for (Vector<Datum> d : data) {
			v.add(d.iterator());
		}
		return new DatumIterator(v.iterator());
	}

	@Override
	public int getNumUsers() {
		return users.size();
	}

	@Override
	public int getNumItems() {
		return items.size();
	}
	
	private class DatumIterator implements Iterator <Datum> {
		private Iterator <Iterator<Datum>> outerIterator;
		private Iterator <Datum> currentIterator;
		
		public DatumIterator (Iterator <Iterator<Datum>> it) {
			this.outerIterator = it;
			currentIterator = it.next();
		}
		
		@Override
		public boolean hasNext() {
			while (outerIterator.hasNext() && !currentIterator.hasNext()) {
				currentIterator = outerIterator.next();
			}
			
			return currentIterator.hasNext();
		}

		@Override
		public Datum next() {
			hasNext();
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
	public int getSize() {
		return size;
	}

	@Override
	public Set<Integer> getUsers() {
		return users;
	}

	@Override
	public Set<Integer> getItems() {
		return items;
	}

	@Override
	public Iterator<Datum> iterator(int from, int to) {
		Vector<Iterator<Datum>> v = new Vector<Iterator<Datum>>();
		int soFar = 0;
		
		for (Vector<Datum> d : data) {
			if (soFar + d.size() < from || soFar >= to) {
				soFar += d.size();
				continue;
			}
			int startIndex = Math.max(0, from - soFar);
			int endIndex = Math.min(d.size(), to - soFar);
			v.add(d.subList(startIndex, endIndex).iterator());
			soFar += d.size();
		}
		return new DatumIterator(v.iterator());
	}
}