package org.nuiz.itemBasedCF;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.nuiz.parallelRecommend.DataList;
import org.nuiz.parallelRecommend.Datum;
import org.nuiz.parallelRecommend.Model;
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
	
	private HashMap<Integer, SortedSet<OrderedPair<Double, Integer>>> similarities;
	private Map<Integer, Map<Integer, Double>> userRatings;
	private Map<Integer, Double> userSum;
	private Map<Integer, Double> userSqSum;
	private Map<Integer, Double> itemSum;
	private Map<Integer, Double> itemSqSum;
	private HashMap <Integer, Map<Integer, Double>> itemSets;
	private HashMap <Integer, Double> lengths = null;
	private SimType st;
	
	public ItemBasedCF(SimType st){
		similarities = new HashMap<Integer, SortedSet<OrderedPair<Double, Integer>>>();
		userRatings = new HashMap<Integer, Map<Integer, Double>>();
		userSum = new HashMap<Integer, Double>();
		userSqSum = new HashMap<Integer, Double>();
		itemSum = new HashMap<Integer, Double>();
		itemSqSum = new HashMap<Integer, Double>();
		itemSets = new HashMap<Integer, Map<Integer,Double>>();
		this.st = st;
	}
	
	@Override
	public void fit(DataList data) {
		for (Integer item : data.getItems()){
			itemSets.put(item, new TreeMap<Integer, Double>());
			similarities.put(item, new TreeSet<OrderedPair<Double, Integer>>());
			itemSum.put(item, 1e-10);
			itemSqSum.put(item, 1e-10);
		}
		
		for (Integer user : data.getUsers()) {
			userRatings.put(user, new HashMap<Integer, Double>());
			userSum.put(user, 1e-10);
			userSqSum.put(user, 1e-10);
		}
		
		for (Datum d : data) {
			itemSets.get(d.getItem()).put(d.getUser(), d.getRating());
			double rating = d.getRating();
			double sqRating = rating*rating;
			userSum.put(d.getUser(), userSum.get(d.getUser()) + rating);
			itemSum.put(d.getItem(), itemSum.get(d.getItem()) + rating);
			userSqSum.put(d.getUser(), userSum.get(d.getUser()) + sqRating);
			itemSqSum.put(d.getItem(), itemSqSum.get(d.getItem()) + sqRating);
			userRatings.get(d.getUser()).put(d.getItem(), d.getRating());
		}
		
		for (Integer i1 : data.getItems()) {
			for (Integer i2 : data.getItems()) {
				if (i2 >= i1) 
					continue;
				
				double sim = 0;
				
				if (st == SimType.COSINE_SIM){
					sim = cosineSim (i1, i2);
				} else if (st == SimType.ADJUSTED_COSINE_SIM) {
					sim = adjustedCosine(i1, i2);
				} else if (st == SimType.CORRELATION_SIM) {
					sim = correlationSim(i1, i2);
				}
				
				
				similarities.get(i1).add(new OrderedPair<Double, Integer>(sim, i2));
				similarities.get(i2).add(new OrderedPair<Double, Integer>(sim, i1));
			}
			System.out.printf("Computed item %d\n", i1);
		}
	}

	@Override
	public Iterable<Double> predict(DataList data) {
		Vector <Double> retval = new Vector<Double>();
		for (Datum d : data) {
			retval.add(weightedPredict(d));
		}
		return retval;
	}
	
	private void cosineSimLengths(){
		lengths = new HashMap<Integer, Double>();
		for (Integer itemSetId : itemSets.keySet()) {
			double len = 1e-10;
			len += itemSqSum.get(itemSetId);
			len = Math.sqrt(len);
			lengths.put(itemSetId, len);
		}
	}
	
	private double cosineSim (int a, int b){
		if (lengths == null) {
			cosineSimLengths();
		}
		
		Map<Integer, Double> itemsA = itemSets.get(a);
		Map<Integer, Double> itemsB = itemSets.get(b);
		Set<Integer> smaller = (itemsA.size() < itemsB.size() ? itemsA : itemsB).keySet();
		Set<Integer> larger = (itemsA.size() >= itemsB.size() ? itemsA : itemsB).keySet();
		double dotProduct = 0;
		for (Integer i : smaller) {
			if (larger.contains(i))
				dotProduct += itemsA.get(i)*itemsB.get(i);
		}
		return dotProduct/(lengths.get(a)*lengths.get(b));
	}
	
	private void adjustedCosineComputeLengths() {
		lengths = new HashMap<Integer, Double>();
		for (Integer itemSetId : itemSets.keySet()) {
			Map<Integer, Double> itemSet = itemSets.get(itemSetId);
			double len = 1e-10;
			for (Integer user : itemSet.keySet()) {
				double userAverage = userSum.get(user)/userRatings.get(user).size();
				len += (itemSet.get(user) - userAverage)*(itemSet.get(user) - userAverage);
			}
			lengths.put(itemSetId, Math.sqrt(len));
		}
	}
	
	private double adjustedCosine(int a, int b){
		if (lengths == null) {
			adjustedCosineComputeLengths();
		}
		
		Map<Integer, Double> itemsA = itemSets.get(a);
		Map<Integer, Double> itemsB = itemSets.get(b);
		Set<Integer> smaller = (itemsA.size() < itemsB.size() ? itemsA : itemsB).keySet();
		Set<Integer> larger = (itemsA.size() >= itemsB.size() ? itemsA : itemsB).keySet();
		
		double dotProduct = 0;
		double alen = 1e-10;
		double blen = 1e-10;
		for (Integer i : smaller) {
			if (larger.contains(i)){
				double userAverage = userSum.get(i)/userRatings.get(i).size();
				dotProduct += (itemsA.get(i)-userAverage)*(itemsB.get(i)-userAverage);
			}
		}
		
		alen = lengths.get(a);
		blen = lengths.get(b);
		return dotProduct/(alen*blen);
	}
	
	private void correlationSimComputeLengths() {
		lengths = new HashMap<Integer, Double>();
		for (Integer itemSetId : itemSets.keySet()) {
			Map<Integer, Double> itemSet = itemSets.get(itemSetId);
			double len = 1e-10;
			double itemAverage = itemSum.get(itemSetId)/itemSet.size();
			for (Integer user : itemSet.keySet()) {
				len += (itemSet.get(user) - itemAverage)*(itemSet.get(user) - itemAverage);
				if (len <= 0 || Double.isNaN(len)){
					throw new ArithmeticException();
				}
			}
			lengths.put(itemSetId, Math.sqrt(len));
		}
	}
	
	private double correlationSim(int a, int b) {
		if (lengths == null) {
			correlationSimComputeLengths();
		}
		
		Map<Integer, Double> itemsA = itemSets.get(a);
		Map<Integer, Double> itemsB = itemSets.get(b);
		Set<Integer> smaller = (itemsA.size() < itemsB.size() ? itemsA : itemsB).keySet();
		Set<Integer> larger = (itemsA.size() >= itemsB.size() ? itemsA : itemsB).keySet();
		

		double itemAAverage = itemSum.get(a)/itemSets.get(a).size();
		double itemBAverage = itemSum.get(b)/itemSets.get(b).size();
		
		double dotProduct = 0;
		double alen = 1e-10;
		double blen = 1e-10;
		for (Integer i : smaller) {
			if (larger.contains(i)){
				if (Double.isInfinite((itemsA.get(i)-itemAAverage)*(itemsB.get(i)-itemBAverage)))
					throw new ArithmeticException();
				dotProduct += (itemsA.get(i)-itemAAverage)*(itemsB.get(i)-itemBAverage);
			}
		}
		

		alen = lengths.get(a);
		blen = lengths.get(b);
		if (Double.isNaN(dotProduct) || Double.isNaN(alen) || Double.isNaN(alen) || alen*blen == 0 ||
			Double.isInfinite(dotProduct) || Double.isInfinite(alen) || Double.isInfinite(blen)){
			throw new ArithmeticException();
		}
		
		return dotProduct/(alen*blen);
	}
	
	private double weightedPredict(Datum d) {
		SortedSet<OrderedPair<Double, Integer>> sims = similarities.get(d.getItem());
		Map<Integer, Double> rated = userRatings.get(d.getUser());
		int toFind = 5;
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
		System.out.println(pred/div);
		return pred/div;
	}
}
