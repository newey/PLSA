package org.nuiz.parallelRecommend;

import java.util.HashMap;

class NormaliseData {
	HashMap <Integer, NormaliseUser> mapping;
	NormaliseUser population;
	
	public NormaliseData (DataList data, double normFactor) {
		mapping = new HashMap<Integer, NormaliseUser>();
		population = new NormaliseUser(null, 0);
		for (Datum d : data) {
			if (!mapping.containsKey(d.getUser())) {
				mapping.put(d.getUser(), new NormaliseUser(population, normFactor));
			}
			mapping.get(d.getUser()).addObservation(d.getRating());
			population.addObservation(d.getRating());
		}
	}
	
	public double normaliseUserRating (int user, double rating) {
		return mapping.get(user).normalise(rating);
	}
	
	public double denormaliseUserRating (int user, double rating) {
		return mapping.get(user).denormalise(rating);
	}
}
