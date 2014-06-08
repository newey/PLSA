package org.nuiz.asymSVD;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.nuiz.parallelRecommend.DataList;
import org.nuiz.parallelRecommend.Model;
import org.nuiz.similarityEngines.SimilarityEngine;
import org.nuiz.utils.Pair;

public class AsymSVD implements Model {
	private final double lambda;
	private final double gamma;
	private final SimilarityEngine se;
	private final int steps;
	private final int hoodSize;
	private double overallBase = 0;
	private final Map<Integer, Double> userBase = new HashMap<Integer, Double>();
	private final Map<Integer, Double> itemBase = new HashMap<Integer, Double>();
	private final Map<Pair<Integer,Integer>, Double> ratInterpWeights
	= new HashMap<Pair<Integer,Integer>, Double>();
	private final Map<Pair<Integer,Integer>, Double> seenInterpWeights
	= new HashMap<Pair<Integer,Integer>, Double>();
	
	public AsymSVD (double lamb, double gamm, SimilarityEngine se, int steps, int neighbourHood) {
		lambda = lamb;
		gamma = gamm;
		this.se = se;
		this.steps = steps;
		hoodSize = neighbourHood;
	}
	
	
	@Override
	public void fit(DataList data) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<Double> predict(DataList data) throws InterruptedException,
			ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getDescription() {
		return String.format("asymSVD,%f,%f,%s,%s,%s", lambda, gamma, se.toString(), steps, hoodSize);
	}

}
