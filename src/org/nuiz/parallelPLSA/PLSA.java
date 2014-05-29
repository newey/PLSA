package org.nuiz.parallelPLSA;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.nuiz.parallelRecommend.Model;
import org.nuiz.parallelRecommend.Datum;
import org.nuiz.parallelRecommend.DataList;
import org.nuiz.parallelRecommend.UserFileDataList;

public class PLSA implements Model{
	private int steps;
	private int classes;
	private UserClasses userClasses;
	private ClassAttributeToDistributionMap caTdm;
	private DataList userData;
	
	public PLSA (int steps, int classes, DataList userData) {
		this.steps = steps;
		this.classes = classes;
		userClasses = new UserClasses(classes);
		caTdm = new ClassAttributeToDistributionMap(classes);
		this.userData = userData;

		caTdm.addDisribution(UserFileDataList.AGE_ITEM, new MultinomialDistributionFactory(UserFileDataList.AGE_CLASSES));
		caTdm.addDisribution(UserFileDataList.GENDER_ITEM, new MultinomialDistributionFactory(UserFileDataList.GENDER_CLASSES));
		caTdm.addDisribution(UserFileDataList.OCCUPATION_ITEM, new MultinomialDistributionFactory(UserFileDataList.OCCUPATION_CLASSES));
	}
	
	public PLSA (int steps, int classes) {
		this.steps = steps;
		this.classes = classes;
		userClasses = new UserClasses(classes);
		caTdm = new ClassAttributeToDistributionMap(classes);
		this.userData = null;
	}
	
	@Override
	public void fit(DataList data) {
		DistributionFactory distFactory = new NormalDistributionFactory();
		for (Integer i : data.getItems()){
			caTdm.addDisribution(i, distFactory);
		}
		
		BlockingQueue<Runnable> work = new LinkedBlockingQueue<Runnable>();
		ThreadPoolExecutor ex = new ThreadPoolExecutor(4, 4, 60, TimeUnit.SECONDS, work);
		
		for (int i = 0; i < steps; i++) {
			emStep(data, ex);
		}
		
		ex.shutdown();
	}

	@Override
	public Iterable<Double> predict(DataList data) {
		LinkedList<Double> results = new LinkedList<Double>();
		
		for (Datum d : data) {
			double guess = 0;
			for (int c = 0; c < classes; c++){
				guess += userClasses.getProb(d.getUser(), c)*caTdm.getGuess(d.getItem(), c);
			}
			results.add(guess);
		}
		return results;
	}

	private void emStep(DataList data, ThreadPoolExecutor ex) {
		Vector<Future<?>> tasks = new Vector<Future<?>>();
		int splits = 4;
		for (DataList dl : userData != null ? new DataList[]{data, userData} : new DataList[]{data}) {
			int splitSize = dl.getSize()/splits;
			int prev = 0;
			for (int i = 0; i < splits-1; i++){
				Runnable r = new EmStepRunnable(dl.iterator(prev, prev+splitSize));
				tasks.add(ex.submit(r));
				prev += splitSize;
			}
			Runnable r = new EmStepRunnable(dl.iterator(prev, dl.getSize()));
			tasks.add(ex.submit(r));
		}
		
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
		
		userClasses.finaliseRound();
		caTdm.finaliseRound();
	}
	
	public double getItemRatingForClass(int item, int cl){
		return caTdm.getGuess(item, cl);
	}
	
	private class EmStepRunnable implements Runnable {
		Iterator<Datum> it;
		public EmStepRunnable (Iterator<Datum> it) {
			//System.err.println("Constructed an EmStep");
			this.it = it;
		}
		
		@Override
		public void run() {
			//System.err.println("Running an EmStep");
			double sum;
			double[] perClass = new double[classes];
			while (it.hasNext()) {
				Datum d = it.next();
				sum = 0;
				for (int c = 0; c < classes; c++){
					perClass[c] = caTdm.getProb(d.getItem(), c, d.getRating())*
							userClasses.getProb(d.getUser(), c);
					sum += perClass[c];
				}
				for (int c = 0; c < classes; c++){
					perClass[c] /= sum;
	
					userClasses.addUserClassWeight(d.getUser(), c, perClass[c]);
					caTdm.addObservation(d.getItem(), c, d.getRating(), perClass[c]);
				}
			}
			//System.err.println("Ran an EmStep");
		}	
	}
}
