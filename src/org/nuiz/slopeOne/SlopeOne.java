package org.nuiz.slopeOne;


import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import org.nuiz.XMLStarter.GlobalSettings;
import org.nuiz.parallelRecommend.*;
import org.nuiz.utils.Pair;


public class SlopeOne implements Model {
	MovieStore movieStore;
	ConcurrentHashMap <Integer, Vector<Pair<Integer, Double>>> usersToMovies;
	ConcurrentHashMap <Integer, ReentrantLock> locks;
	
	@Override
	public void fit(DataList data) {
		movieStore = new MovieStore();
		usersToMovies = new ConcurrentHashMap <Integer, Vector<Pair<Integer, Double>>>();
		locks = new ConcurrentHashMap<Integer, ReentrantLock>();
		
		for (Integer user : data.getUsers()) {
			locks.put(user, new ReentrantLock());
			usersToMovies.put(user, new Vector<Pair<Integer,Double>>());
		}
		
		ThreadPoolExecutor ex = GlobalSettings.getExecutor();
		int splits = 4;
		Vector<Future<?>> tasks = new Vector<Future<?>>();
		
		int splitSize = data.getSize()/splits;
		int prev = 0;
		Runnable r;
		
		// Run on all but one split
		for (int i = 0; i < splits-1; i++){
			r = new StepRunnable(data.iterator(prev, prev+splitSize));
			tasks.add(ex.submit(r));
			prev += splitSize;
		}
		
		r = new StepRunnable(data.iterator(prev, data.getSize()));
		tasks.add(ex.submit(r));

		for (Future <?> f : tasks){
			try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException();
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}

	}

	@Override
	public Iterable<Double> predict(DataList data) {
		Vector <Double> preds = new Vector<Double>();
		for (Datum d : data) {
			double guess = 0;
			int count = 0;
			for (Pair<Integer, Double> oldR : usersToMovies.get(d.getUser())) {
				guess += oldR.b + movieStore.getDiff(d.getItem(), oldR.a);
				count++;
			}
			preds.add(count != 0? guess/count : 0);
		}
		return preds;
	}

	private class StepRunnable implements Runnable {
		Iterator<Datum> it;
		public StepRunnable (Iterator<Datum> it) {
			//System.err.println("Constructed a step");
			this.it = it;
		}
		
		@Override
		public void run() {
			while (it.hasNext()) {
				Datum d = it.next();
				locks.get(d.getUser()).lock();
				for (Pair<Integer, Double> prev : usersToMovies.get(d.getUser())) {
					movieStore.addDiff(d.getItem(), prev.a, d.getRating() - prev.b);	
				}
				
				usersToMovies.get(d.getUser()).add(new Pair<Integer, Double>(d.getItem(), d.getRating()));
				locks.get(d.getUser()).unlock();
			}
		}
	}

	
	@Override
	public String getDescription() {
		return String.format("slopeOne");
	}
	
}
