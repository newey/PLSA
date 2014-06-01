package org.nuiz.parallelRecommend;

import java.util.HashMap;

/**
 * An object to take datum and map between normalised and non-normalised form over all users.
 * 
 * @author Robert Newey
 * 
 */
public class NormaliseData {
	HashMap <Integer, NormaliseUser> mapping;
	NormaliseUser population;
	
	/**
	 * Given some DataList this computes transformations to give each user mean 0 and variance 1.
	 * Then normFactor pulls this towards the population mean for mean and variance. This is to
	 * regularise the transform somewhat.
	 * See Hoffman 2002 for details
	 * @param data Data to compute transforms on
	 * @param normFactor How much to pull user transforms towards the population mean.
	 */
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
	
	/**
	 * Given a user and a denormalised rating, normalise the rating
	 * @param user Which user to do the transform for
	 * @param rating The rating to be normalise.
	 * @return The normalised rating
	 */
	public double normaliseUserRating (int user, double rating) {
		return mapping.get(user).normalise(rating);
	}
	
	/**
	 * Given a user and a normalised rating, denormalise the rating
	 * @param user Which user to do the transform for
	 * @param rating The rating to be denormalise.
	 * @return The denormalised rating
	 */
	public double denormaliseUserRating (int user, double rating) {
		return mapping.get(user).denormalise(rating);
	}
}
