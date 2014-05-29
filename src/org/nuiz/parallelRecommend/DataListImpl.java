package org.nuiz.parallelRecommend;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class DataListImpl implements DataList  {
	private Vector<Datum> data = null;
	private Set<Integer> users = null;
	private Set<Integer> items = null;
	private int maxItem = -1;
	
	public DataListImpl() {}
	
	public DataListImpl(DataListImpl dli){
		data = dli.getData();
		users = dli.users;
		items = dli.items;
		maxItem = dli.maxItem;
	}
	
	public DataListImpl(Vector<Datum> v) {
		loadData(v);
	}
	
	public void loadData(Vector<Datum> v) {
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

	protected Vector<Datum> getData() {
		return data;
	}

}
