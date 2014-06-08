package org.nuiz.svd;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import org.la4j.vector.dense.BasicVector;
import org.la4j.vector.Vector;
import org.nuiz.parallelRecommend.DataList;
import org.nuiz.parallelRecommend.Datum;
import org.nuiz.parallelRecommend.Model;


public class SVDModel implements Model {
	private final int classes;
	private final Map<Integer, Vector> itemDecomp =
			new HashMap<Integer, Vector>();
	private final Map<Integer, Vector> userDecomp =
			new HashMap<Integer, Vector>();
	private final Map<Integer, Map<Integer, Double>> userRatings = 
			new HashMap<Integer, Map<Integer, Double>>();
	private final Map<Integer, Map<Integer, Double>> itemRatings = 
			new HashMap<Integer, Map<Integer, Double>>();
	private final double gamma;
	private final double lambda;
	private final int steps;
	
	public SVDModel (int steps, int classes, double lambda, double gamma) {
		this.classes = classes;
		this.gamma = gamma;
		this.lambda = lambda;
		this.steps = steps;
	}
	
	@Override
	public void fit(DataList data) {
		for (int user : data.getUsers()) {
			userDecomp.put(user, new BasicVector(classes).add(0.01));
			userRatings.put(user, new HashMap<Integer, Double>());
		}
		for (int item : data.getItems()) {
			itemDecomp.put(item, new BasicVector(classes).add(0.01));
			itemRatings.put(item, new HashMap<Integer, Double>());
		}
		
		for (Datum d : data){
			itemRatings.get(d.getItem()).put(d.getUser(), d.getRating());
			userRatings.get(d.getUser()).put(d.getItem(), d.getRating());
		}
		
		for (int step = 0; step < steps; step++) {
			// update userDecomps
			for (int user : data.getUsers()) {
				Vector initial = userDecomp.get(user);
				Vector updateUser = initial.subtract(initial.multiply(gamma*lambda));
				Map<Integer, Double> cUserRatings = userRatings.get(user);
				for (int item : cUserRatings.keySet()) {
					Vector itemVector = itemDecomp.get(item);
					double scalar = (initial.innerProduct(itemVector) - cUserRatings.get(item))*gamma;
					if (Double.isNaN(scalar)) {
						throw new ArithmeticException();
					}
					updateUser = updateUser.subtract(itemVector.multiply(scalar));
				}
				userDecomp.put(user, updateUser);
			}
			
			// update itermDecomps
			for (int item : data.getItems()) {
				Vector initial = itemDecomp.get(item);
				Vector updateItem = initial.subtract(initial.multiply(gamma*lambda));
				Map<Integer, Double> cItemRatings = itemRatings.get(item);
				for (int user : cItemRatings.keySet()) {
					Vector userVector = userDecomp.get(user);
					double scalar = (initial.innerProduct(userVector) - cItemRatings.get(user))*gamma;
					if (Double.isNaN(scalar)) {
						throw new ArithmeticException();
					}
					updateItem = updateItem.subtract(userVector.multiply(scalar));
				}
				itemDecomp.put(item, updateItem);
			}
		}		
	}

	@Override
	public Iterable<Double> predict(DataList data) throws InterruptedException,
			ExecutionException {
		List<Double> ratings = new LinkedList<Double>();
		
		for (Datum d : data) {
			ratings.add(itemDecomp.get(d.getItem()).innerProduct(userDecomp.get(d.getUser())));
		}
		
		return ratings;
	}

	@Override
	public String getDescription() {
		return String.format("SVD,%d,%d,%f,%f",steps,classes,lambda,gamma);
	}
	
	

}
