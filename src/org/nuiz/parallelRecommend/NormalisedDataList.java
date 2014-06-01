package org.nuiz.parallelRecommend;

import java.util.Iterator;
import java.util.Set;

/**
 * Implements a DataList which puts out normalised ratings, then the denormalise function
 * allows us to reconstruct the original ratings.
 * @author Robert Newey
 */
public class NormalisedDataList implements DataList {
	private NormaliseData core;
	private DataList data;
	
	/**
	 * Constructs a NormalisedDataList backed by data, and normalises according to core
	 * @param data
	 * @param core
	 */
	public NormalisedDataList (DataList data, NormaliseData core) {
		this.data = data;
		this.core = core;
	}
	
	@Override
	public Iterator<Datum> iterator() {
		return new NormalIterator(data.iterator(), core);
	}

	@Override
	public Iterator<Datum> iterator(int from, int to) {
		return new NormalIterator(data.iterator(from, to), core);
	}
	
	class NormalIterator implements Iterator<Datum> {
		private NormaliseData core;
		private Iterator <Datum> it;
		
		public NormalIterator (Iterator<Datum> it, NormaliseData core) {
			this.it = it;
			this.core = core;
		}
		
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Datum next() {
			Datum d = it.next();
			double newRating = core.normaliseUserRating(d.getUser(), d.getRating());
			return new Datum(d.getUser(), d.getItem(), newRating, d.getTimestamp());
		}

		@Override
		public void remove() {}
		
	}

	@Override
	public int getNumUsers() {
		return data.getNumUsers();
	}

	@Override
	public int getNumItems() {
		return data.getNumItems();
	}

	@Override
	public int getMaxItem() {
		return data.getMaxItem();
	}

	@Override
	public int getSize() {
		return data.getSize();
	}

	@Override
	public Set<Integer> getUsers() {
		return data.getUsers();
	}

	@Override
	public Set<Integer> getItems() {
		return data.getItems();
	}

	@Override
	public DataList getSubDataList(int from, int to) {
		return new NormalisedDataList(data.getSubDataList(from, to), core);
	}

	@Override
	public double normaliseRating(Datum data) {
		return core.normaliseUserRating(data.getUser(), data.getRating());
	}

	@Override
	public double denormaliseRating(int user, double rating) {
		return core.denormaliseUserRating(user, rating);
	}

}
