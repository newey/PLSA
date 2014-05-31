package org.nuiz.parallelRecommend;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class DataListMultiplexer implements DataList {
	Vector <DataList> data;
	Set <Integer> users;
	Set <Integer> items;
	int maxItem = -1;
	int size = 0;
	
	public DataListMultiplexer (Vector <DataList> data, Set<Integer> users, Set<Integer> items, int maxItem) {
		this.data=data;
		this.users = users;
		this.items = items;
		this.maxItem = maxItem;
		
		for (DataList vd : data) {
			size += vd.getSize();
		}

	}
	
	@Override
	public Iterator<Datum> iterator() {
		List<Iterator<Datum>> v = new Vector<Iterator<Datum>>();
		for (DataList d : data) {
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
		List<Iterator<Datum>> v = new Vector<Iterator<Datum>>();
		int soFar = 0;
		
		for (DataList d : data) {
			if (soFar + d.getSize() < from || soFar >= to) {
				soFar += d.getSize();
				continue;
			}
			int startIndex = Math.max(0, from - soFar);
			int endIndex = Math.min(d.getSize(), to - soFar);
			v.add(d.getSubDataList(startIndex, endIndex).iterator());
			soFar += d.getSize();
		}
		return new DatumIterator(v.iterator());
	}

	@Override
	public DataList getSubDataList(int from, int to) {
		Vector<DataList> v = new Vector<DataList>();
		int soFar = 0;
		
		for (DataList d : data) {
			if (soFar + d.getSize() < from || soFar >= to) {
				soFar += d.getSize();
				continue;
			}
			int startIndex = Math.max(0, from - soFar);
			int endIndex = Math.min(d.getSize(), to - soFar);
			v.add(d.getSubDataList(startIndex, endIndex));
			soFar += d.getSize();
		}
		return new DataListMultiplexer(v, users, items, maxItem);
	}

	@Override
	public double normaliseRating(Datum data) {
		return data.getRating();
	}

	@Override
	public double denormaliseRating(int user, double rating) {
		return rating;
	}
}
