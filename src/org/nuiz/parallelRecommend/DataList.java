package org.nuiz.parallelRecommend;

public interface DataList extends Iterable<Datum> {
	public int getNumUsers();
	public int getNumItems();
}
