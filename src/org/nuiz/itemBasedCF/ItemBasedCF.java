package org.nuiz.itemBasedCF;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.nuiz.XMLStarter.GlobalSettings;
import org.nuiz.parallelRecommend.DataList;
import org.nuiz.parallelRecommend.Datum;
import org.nuiz.parallelRecommend.Model;
import org.nuiz.similarityEngines.AdjustedCosineSimilarity;
import org.nuiz.similarityEngines.CorrelationSimilarity;
import org.nuiz.similarityEngines.CosineSimilarity;
import org.nuiz.similarityEngines.SimilarityEngine;
import org.nuiz.utils.OrderedPair;

/**
 * Implements item-item based collaborative filtering
 * @author Robert Newey
 *
 */
public class ItemBasedCF implements Model {
	public enum SimType{
		COSINE_SIM,
		ADJUSTED_COSINE_SIM,
		CORRELATION_SIM
	}
	
	private Map<Integer, Double> userAvg;
	private HashMap <Integer, Map<Integer, Double>> itemSets;
	private HashMap <Integer, Map<Integer, Double>> userRatings;
	private SimilarityEngine simEngine;
	private final int hoodSize;
	
	public ItemBasedCF(SimType st, int hoodSize){
		userAvg = new HashMap<Integer, Double>();
		itemSets = new HashMap<Integer, Map<Integer,Double>>();
		userRatings = new HashMap<Integer, Map<Integer,Double>>();
		if (st == SimType.COSINE_SIM){
			simEngine = new CosineSimilarity(itemSets);
		} else if (st == SimType.ADJUSTED_COSINE_SIM) {
			simEngine = new AdjustedCosineSimilarity(itemSets, userAvg);
		} else if (st == SimType.CORRELATION_SIM) {
			simEngine = new CorrelationSimilarity(itemSets);
		}
		this.hoodSize = hoodSize;
	}
	
	@Override
	public void fit(DataList data) {
		for (Integer item : data.getItems()){
			itemSets.put(item, new TreeMap<Integer, Double>());
		}
		
		for (int user : data.getUsers()) {
			userRatings.put(user, new HashMap<Integer, Double>());
			userAvg.put(user, 1e-10);
		}
		
		for (Datum d : data) {
			double rating = d.getRating();
			itemSets.get(d.getItem()).put(d.getUser(), rating);
			userAvg.put(d.getUser(), userAvg.get(d.getUser()) + rating);
			userRatings.get(d.getUser()).put(d.getItem(), rating);
		}
		
		for (int user : data.getUsers()) {
			userAvg.put(user, userAvg.get(user)/userRatings.get(user).size());
		}
	}

	@Override
	public Iterable<Double> predict(DataList data) throws InterruptedException, ExecutionException {
		List <Future<Vector<Double>>> results = null;
		Vector <Double> retval = new Vector<Double>();
		ThreadPoolExecutor ex = GlobalSettings.getExecutor();
		int splits = GlobalSettings.getNumThreads()*2;
		int splitSize = data.getSize()/splits;
		int prev = 0;
		Vector<Callable<Vector<Double>>> tasks = new Vector<Callable<Vector<Double>>>();
		
		// Run on all but one split
		for (int i = 0; i < splits-1; i++){
			tasks.add(new WeightedCallable(data.iterator(prev, prev+splitSize)));
			prev+=splitSize;
		}
		
		tasks.add(new WeightedCallable(data.iterator(prev, data.getSize())));
		results = ex.invokeAll(tasks);
		
		for (Future<Vector<Double>> f : results) {
			retval.addAll(f.get());
		}
		return retval;
	}
	
	private class WeightedCallable implements Callable<Vector<Double>> {
		Iterator<Datum> dataIt;
		
		public WeightedCallable (Iterator<Datum> datIt) {
			dataIt = datIt;
		}

		@Override
		public Vector<Double> call() throws Exception {
			Vector <Double> retval = new Vector<Double>();
			while (dataIt.hasNext()) {
				retval.add(weightedPredict(dataIt.next()));
			}
			return retval;
		}
		
	}
	
	private double weightedPredict(Datum d) {
		PriorityQueue<OrderedPair<Double, Integer>> sims = 
				new PriorityQueue<OrderedPair<Double, Integer>>();
		
		Map<Integer, Double> rated = userRatings.get(d.getUser());
		
		for (int i : rated.keySet()) {
			sims.add(new OrderedPair<Double, Integer>(simEngine.similarity(i, d.getItem()), i));
		}
		
		int toFind = hoodSize;
		double pred = 0;
		double div = 1e-10;
		for (OrderedPair<Double, Integer> op : sims) {
			if (toFind == 0) {
				break;
			}
			if (rated.containsKey(op.b)) {
				pred += rated.get(op.b)*op.a;
				div += Math.abs(op.a);
				toFind--;
			}
		}
		if (Double.isNaN(div) || Double.isNaN(pred) || div == 0 || Double.isNaN(pred/div)) {
			throw new ArithmeticException();
		}
		return pred/div;
	}
	
	@Override
	public String getDescription() {
		return String.format("itemBasedCF,%s,%d",simEngine,hoodSize);
	}
}
