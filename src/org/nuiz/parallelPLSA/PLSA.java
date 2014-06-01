package org.nuiz.parallelPLSA;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.nuiz.XMLStarter.GlobalSettings;
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

		ThreadPoolExecutor ex = GlobalSettings.getExecutor();
		
		double minSoFar = Double.POSITIVE_INFINITY;
		double current;
		
		int prev = (data.getSize()/5)*4;
		double retval = 0;
		int count = 0;
		
		Iterator <Double> predIt = predict(data.iterator(prev, data.getSize())).iterator();
		Iterator <Datum> dataIt = data.iterator(prev, data.getSize());
		while (predIt.hasNext()) {
			double diff = predIt.next() - dataIt.next().getRating();
			retval += diff*diff;
			count++;
		}
		
		System.out.printf("Initial error: %f\n", retval/count);
		
		for (int i = 0; i < steps; i++) {
			current = emStep(data, ex, true);
			System.out.printf("Step %d: %f\n",i,  current);
			minSoFar = current < minSoFar ? current : minSoFar;
		}
		
		emStep(data, ex, false);
		
		ex.shutdown();
	}

	@Override
	public Iterable<Double> predict(DataList data) {
		Iterable<Double> results = predict(data.iterator());
		return results;
	}
	
	private Iterable<Double> predict (Iterator <Datum> data) {
		LinkedList<Double> results = new LinkedList<Double>();
		
		while (data.hasNext()) {
			Datum d = data.next();
			double guess = 0;
			for (int c = 0; c < classes; c++){
				guess += userClasses.getProb(d.getUser(), c)*caTdm.getGuess(d.getItem(), c);
			}
			results.add(guess);
		}
		return results;
	}

	private double emStep(DataList data, ThreadPoolExecutor ex, boolean holdOut) {
		Vector<Future<?>> tasks = new Vector<Future<?>>();
		int splits = 5;
		
		int splitSize = data.getSize()/splits;
		int prev = 0;
		Runnable r;
		
		// Run on all but one split
		for (int i = 0; i < splits-1; i++){
			r = new EmStepRunnable(data.iterator(prev, prev+splitSize));
			tasks.add(ex.submit(r));
			prev += splitSize;
		}
		
		if (!holdOut) {
			r = new EmStepRunnable(data.iterator(prev, data.getSize()));
			tasks.add(ex.submit(r));
		}
		
		if (userData != null) {
			r = new EmStepRunnable(userData.iterator(0, userData.getSize()));
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
		
		double retval = 0;
		int count = 0;
		
		if (holdOut) {
			Iterator <Double> predIt = predict(data.iterator(prev, data.getSize())).iterator();
			Iterator <Datum> dataIt = data.iterator(prev, data.getSize());
			while (predIt.hasNext()) {
				double diff = predIt.next() - dataIt.next().getRating();
				retval += diff*diff;
				count++;
			}
		}
		
		return retval/count;
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
					double a = caTdm.getProb(d.getItem(), c, d.getRating());
					double b = userClasses.getProb(d.getUser(), c);
					perClass[c] = a*b;
					if (Double.isNaN(a) || Double.isNaN(b) || a*b == 0){
						throw new ArithmeticException();
					}
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
