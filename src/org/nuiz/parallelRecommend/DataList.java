package org.nuiz.parallelRecommend;

import java.util.Iterator;
import java.util.Set;

public interface DataList extends Iterable<Datum> {
	public int getNumUsers();
	public int getNumItems();
	public int getMaxItem();
	public int getSize();
	public Set<Integer> getUsers();
	public Set<Integer> getItems();
	public Iterator<Datum> iterator();
	public Iterator<Datum> iterator(int from, int to);
}
