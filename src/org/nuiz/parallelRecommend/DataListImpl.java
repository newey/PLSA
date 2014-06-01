package org.nuiz.parallelRecommend;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A base implementation of a DataList
 * @author Robert Newey
 */
class DataListImpl implements DataList  {
	private List<Datum> data = null;
	private Set<Integer> users = null;
	private Set<Integer> items = null;
	private int maxItem = -1;
	
	/**
	 * Default constructor, This will not become a usable DataList until loadData is called
	 */
	public DataListImpl() {}
	
	/**
	 * Copy constructor, this creates a new DataListImpl backed by the one passed in. Constant time, constant memory
	 * @param dli The DataListImpl to copy
	 */
	public DataListImpl(DataListImpl dli){
		data = dli.getData();
		users = dli.users;
		items = dli.items;
		maxItem = dli.maxItem;
	}
	
	/**
	 * Creates a DataListImpl backed by a List of Data
	 * @param v List of Data to load
	 */
	public DataListImpl(List<Datum> v) {
		loadData(v);
	}
	
	/**
	 * Creates a DataListImpl backed by the arguments passed in
	 * @param v List of Data to use
	 * @param users Set containing user ids in v. Equality is preferable, but supersets are allowed
	 * @param items Set containing item ids in v. Equality is preferable, but supersets are allowed
	 * @param maxItem The largest item id in v.
	 */
	public DataListImpl(List<Datum> v, Set<Integer> users, Set<Integer> items, int maxItem) {
		data = v;
		this.users = users;
		this.items = items;
		this.maxItem = maxItem;
	}
	
	/**
	 * Sets this DataListImpl to be backed by the provided list
	 * @param v List of Data to back this DataListImpl
	 */
	public void loadData(List<Datum> v) {
		users = new HashSet<Integer>();
		items = new HashSet<Integer>();
		data = v;
		for (Datum d : data) {
			users.add(d.getUser());
			items.add(d.getItem());
			maxItem = Math.max(maxItem, d.getItem());
		}
	}
	

	@Override
	public int getNumUsers() {
		return users.size();
	}

	@Override
	public int getNumItems() {
		return items.size();
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


	@Override
	public int getSize() {
		return data.size();
	}


	@Override
	public Iterator<Datum> iterator() {
		return data.iterator();
	}
	
	@Override
	public Iterator<Datum> iterator(int from, int to) {
		return data.subList(from, to).iterator();
	}

	protected List<Datum> getData() {
		return data;
	}

	@Override
	public DataList getSubDataList(int from, int to) {
		return new DataListImpl(data.subList(from, to), users, items, maxItem);
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
