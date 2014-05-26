package org.nuiz.parallelRecommend;

import java.util.Iterator;
import java.util.Set;

public class NormalisedDataList implements DataList {
	private DataList data;
	private NormaliseData core;
	
	public NormalisedDataList (DataList data, double normalisationFactor){
		this.data = data;
		core = new NormaliseData(data, normalisationFactor);
	}
	
	public NormalisedDataList (DataList data, NormaliseData core){
		this.data = data;
		this.core = core;
	}
	
	@Override
	public Iterator<Datum> iterator() {
		class NormalIterator implements Iterator<Datum> {
			private NormaliseData core;
			private Iterator <Datum> it;
			
			public NormalIterator (DataList data, NormaliseData core) {
				this.core = core;
				it = data.iterator();
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
		return new NormalIterator(data, core);
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
	public Set<Integer> getUsers() {
		return data.getUsers();
	}

	@Override
	public Set<Integer> getItems() {
		return data.getItems();
	}

}
