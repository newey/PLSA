package org.nuiz.parallelRecommend;

import java.util.Iterator;
import java.util.Set;

/**
 * The fundamental interface for operating on data.
 * @author Robert Newey
 */
public interface DataList extends Iterable<Datum> {
	/**
	 * @return An upper bound for the number of users in the list, actual number may be lower.
	 */
	public int getNumUsers();
	
	/**
	 * @return An upper bound for the number of items in the list, actual number may be lower.
	 */
	public int getNumItems();
	
	/**
	 * @return An upper bound for the highest number item in the list. 
	 */
	public int getMaxItem();
	
	/**
	 * @return The number of data points in the list.
	 */
	public int getSize();
	
	/**
	 * @return The set of users who may be in the list. May include more.
	 */
	public Set<Integer> getUsers();
	
	/**
	 * @return The set of items which may be in the list. May include more.
	 */
	public Set<Integer> getItems();
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Datum> iterator();
	
	/**
	 * Gets an iterator for a sublist on the data.
	 * @param from Position to start iterator at, inclusive.
	 * @param to Position to end iterator at, exclusive.
	 * @return An iterator ranging from 'from' (inclusive) to 'to' (Exclusive)
	 */
	public Iterator<Datum> iterator(int from, int to);
	
	/**
	 * Constructs a sub-dataList backed by this dataList
	 * @param from Position to start sub-list at, inclusive.
	 * @param to Position to end sub-list at, exclusive.
	 * @return A DataList ranging from 'from' (inclusive) to 'to' (Exclusive)
	 */
	public DataList getSubDataList (int from, int to);
	
	/**
	 * Given a datapoint, normalises it in accordance with the DataList implementation.
	 * This transformation is the inverse of denormaliseRating. 
	 * @param data Datum to normalise.
	 * @return The normalised rating from the datum.
	 */
	public double normaliseRating(Datum data);
	
	/**
	 * Given a datapoint, denormalises it in accordance with the DataList implementation.
	 * This transformation is the inverse of normaliseRating. 
	 * @param user the user to denormalise for
	 * @param rating the rating to denormalise
	 * @return The denormalised rating from the datum.
	 */
	public double denormaliseRating(int user, double rating);
}
