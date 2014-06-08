package org.nuiz.leastSquaresBaseline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.nuiz.parallelRecommend.DataList;
import org.nuiz.parallelRecommend.Datum;
import org.nuiz.parallelRecommend.Model;
import org.nuiz.utils.SortedSetIntersection;
import org.jblas.*;


public class LeastSquaresBaseline implements Model {
	private final double lambda1;
	private double overallBase;
	private Map<Integer, Double> userBase = new HashMap<Integer, Double>();
	private Map<Integer, Double> itemBase = new HashMap<Integer, Double>();
	
	public LeastSquaresBaseline(double l1) {
		lambda1 = l1;
	}
	
	
	@Override
	public void fit(DataList data) {
		// fit the baseline model
		fitBaseline(data);

	}

	@Override
	public Iterable<Double> predict(DataList data) throws InterruptedException,
			ExecutionException {
		LinkedList <Double> retval = new LinkedList<Double>();
		
		
		for (Datum d : data) {
			retval.add(overallBase + userBase.get(d.getUser()) + itemBase.get(d.getItem()));
		}
		
		return retval;
	}
	
	private void fitBaseline(DataList data) {
		/* Construct the design matrix
		 * column 0 = offset
		 * columns 1 - numUsers+1 = user params
		 * columns numUsers+2 - numUsers+1+numItems = item params
		 */
		Map <Integer, Integer> userToParamIndex = new HashMap<Integer, Integer>();
		Map <Integer, Integer> itemToParamIndex = new HashMap<Integer, Integer>();
		
		// Isn't going to store for 0, since that is all.
		Map <Integer, SortedSet<Integer>> paramLists = new HashMap<Integer, SortedSet<Integer>>();

		int paramCounter = 1;
		for (int user : data.getUsers()) {
			userToParamIndex.put(user, paramCounter);
			paramLists.put(paramCounter, new TreeSet<Integer>());
			paramCounter++;
		}
		for (int item : data.getItems()) {
			itemToParamIndex.put(item, paramCounter);
			paramLists.put(paramCounter, new TreeSet<Integer>());
			paramCounter++;
		}
		
		
		int numRows = data.getSize();
		int numCols = paramCounter;

		/*Matrix design = new CCSMatrix(numRows, numCols);
		Matrix ident = new CCSMatrix(numCols, numCols);
		BasicVector results = new BasicVector(numRows);*/
		DoubleMatrix design = new DoubleMatrix(numRows, numCols);
		DoubleMatrix results = new DoubleMatrix(numRows, 1);
		
		int rowCounter = 0;
		
		for (Datum d : data) {
			design.put(rowCounter, 0, 1);
			design.put(rowCounter, userToParamIndex.get(d.getUser()), 1);
			design.put(rowCounter, itemToParamIndex.get(d.getItem()), 1);
			paramLists.get(userToParamIndex.get(d.getUser())).add(rowCounter);
			paramLists.get(itemToParamIndex.get(d.getItem())).add(rowCounter);
			results.put(rowCounter, 0, d.getRating());
			rowCounter++;
		}
		
		
		DoubleMatrix hat = new DoubleMatrix(numCols, numCols);
		
		hat.put(0, 0, numRows + lambda1*lambda1);
		
		// do row 0 and col 0
		for (int i = 1; i < numCols; i++) {
			hat.put(i, 0, paramLists.get(i).size());
			hat.put(0, i, paramLists.get(i).size());
		}
		
		// do diag
		for (int i = 1; i < numCols; i++) {
			hat.put(i, i, paramLists.get(i).size()+lambda1*lambda1);
		}
		
		for (int i = 1; i < numCols; i++) {
			for (int j = i+1; j < numCols; j++) {
				int intersect = SortedSetIntersection.size(paramLists.get(i), paramLists.get(j));
				hat.put(i, j, intersect);
				hat.put(j, i, intersect);
			}
		}
		
		DoubleMatrix[] eigenDecomp = Eigen.symmetricEigenvectors(hat);
		
		for (int i = 0; i < numCols; i++) {
			eigenDecomp[1].put(i, i, 1/eigenDecomp[1].get(i,i));
		}
		hat = eigenDecomp[0].mmul(eigenDecomp[1]).mmul(eigenDecomp[0].transpose());
		DoubleMatrix foo = new DoubleMatrix(numCols, 1);
		
		for (int i = 1; i < numCols; i++) {
			double toSet = 0;
			for (int j : paramLists.get(i)) {
				toSet += results.get(j, 0);
			}
			foo.put(i, 0, toSet);
		}
		
		double toSet = 0;
		for (int j = 0; j < numRows; j++) {
			toSet += results.get(j, 0);
		}
		foo.put(0,0, toSet);
		
		DoubleMatrix coefs = hat.mmul(foo);
		
		overallBase = coefs.get(0);
		
		for (int user : userToParamIndex.keySet()){
			userBase.put(user, coefs.get(userToParamIndex.get(user), 0));
		}
		
		for (int item : itemToParamIndex.keySet()){
			itemBase.put(item, coefs.get(itemToParamIndex.get(item), 0));
		}
		
	}
	
	@Override
	public String getDescription() {
		return String.format("leastSquaresBaseline,%f", lambda1);
	}

}
