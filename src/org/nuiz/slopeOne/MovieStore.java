package org.nuiz.slopeOne;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


class MovieStore {
	private class Slope {
		private double diff;
		private int count;
		private ReentrantLock lock;
		
		public Slope(){
			diff = 0;
			count = 0;
			lock = new ReentrantLock();
		}
		
		public void addDiff(double d){
			lock.lock();
			diff += d;
			count++;
			lock.unlock();
		}
		
		public double getDiff(){
			return diff/count;
		}
	}
	ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Slope>> slopes;
	
	public MovieStore() {
		slopes = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,Slope>>();
	}
	
	public void addDiff (int movie1, int movie2, double diff) {
		if (movie2 < movie1) {
			movie1 ^= movie2;
			movie2 ^= movie1;
			movie1 ^= movie2;
			diff *= -1;
		}
		
		ConcurrentHashMap<Integer, Slope> outer = null;
		
		if (!slopes.containsKey(movie1)) {
			outer = new ConcurrentHashMap<Integer, Slope>();
			slopes.put(movie1, outer);
		} else {
			outer = slopes.get(movie1);
		}
		
		Slope inner = null;
		if (!outer.containsKey(movie2)){
			inner = new Slope();
			outer.put(movie2, inner);
		} else {
			inner = outer.get(movie2);
		}

		inner.addDiff(diff);
	}
	
	public double getDiff (int movie1, int movie2) {
		int negate = movie2 < movie1 ? -1 : 1;
		if (movie2 < movie1) {
			movie1 ^= movie2;
			movie2 ^= movie1;
			movie1 ^= movie2;
		}
		
		if (!slopes.containsKey(movie1) || !slopes.get(movie1).containsKey(movie2) ){
			return 0;
		}
		
		Slope foo = slopes.get(movie1).get(movie2);
		
		return negate*foo.getDiff();
	}
}
