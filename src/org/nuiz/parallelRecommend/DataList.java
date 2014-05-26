package org.nuiz.parallelRecommend;

import java.util.Set;

public interface DataList extends Iterable<Datum> {
	public int getNumUsers();
	public int getNumItems();
	public int getMaxItem();
	public Set<Integer> getUsers();
	public Set<Integer> getItems();
}
