package org.nuiz.parallelRecommend;

public class Datum {
	private final int user;
	private final int item;
	private final double rating;
	private final int timestamp;
	
	public Datum (int user, int item, double rating, int timestamp){

		this.user = user;
		this.item = item;
		this.rating = rating;
		this.timestamp = timestamp;
	}

	public int getUser() {
		return user;
	}

	public int getItem() {
		return item;
	}

	public double getRating() {
		return rating;
	}
	
	public int getTimestamp() {
		return timestamp;
	}
}
