package org.nuiz.parallelRecommend;

public class Datum {
	private int user;
	private int item;
	private double rating;
	private int timestamp;
	
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
