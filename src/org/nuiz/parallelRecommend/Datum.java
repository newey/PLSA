package org.nuiz.parallelRecommend;

/**
 * The fundamental unit of data.
 * @author Robert Newey
 */
public class Datum {
	private final int user;
	private final int item;
	private final double rating;
	private final int timestamp;
	
	/**
	 * Constructs a Datum from a user, item, rating and timestamp.
	 * @param user
	 * @param item
	 * @param rating
	 * @param timestamp
	 */
	public Datum (int user, int item, double rating, int timestamp){

		this.user = user;
		this.item = item;
		this.rating = rating;
		this.timestamp = timestamp;
	}

	/**
	 * @return This Datum's user
	 */
	public int getUser() {
		return user;
	}

	/**
	 * @return This Datum's item
	 */
	public int getItem() {
		return item;
	}

	/**
	 * @return This Datum's rating
	 */
	public double getRating() {
		return rating;
	}
	
	/**
	 * @return This Datum's timestamp
	 */
	public int getTimestamp() {
		return timestamp;
	}
}
