import java.util.*;


public class NormalisationModel implements Model {
	private Vector<Rating> testSet;
	private Vector<Rating> normalisedRatings;
	private HashMap <Integer, NormaliseUser> userNormalisations;
	private double normFactor = 0;
	private Model model = null;
	
	public NormalisationModel (Model model, double normalisationFactor){
		reset();
		
		this.model = model;
		normFactor = normalisationFactor;
	}
	
	public void reset(){
		normalisedRatings = new Vector<Rating>();
		testSet = new Vector<Rating>();
		userNormalisations = new HashMap <Integer, NormaliseUser>();
	}
	
	
	@Override
	public double doFold(Vector<Rating> ratings, int fold) {
		reset();
		bulidUserRatings(ratings, fold);
		
		Vector <Double> predictions = model.getFit(normalisedRatings, fold);
		
		double sumSqErr = 0;
		
		for (int i = 0; i < predictions.size(); i++){
			Rating rat = testSet.elementAt(i);
			double denorm = userNormalisations.get(rat.user).denormalise(predictions.elementAt(i));
			sumSqErr += (denorm-rat.rating)*(denorm-rat.rating);
			//sumSqErr += Math.abs(denorm-rat.rating);
		}
		
		if (predictions.size() == 0){
			return 0;
		}
		
		return sumSqErr/predictions.size();
	}
	
	@Override
	public Vector<Double> getFit(Vector<Rating> ratings, int fold) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void bulidUserRatings (Vector<Rating> ratings, int testFold){
		HashMap <Integer, StatsHelper> userStats = new HashMap <Integer, StatsHelper>();
		StatsHelper popStats = new StatsHelper();
		
		for (Rating rat : ratings) {
			if (!userStats.containsKey(rat.user)) {
				userStats.put(rat.user, new StatsHelper());
			}
			if (rat.fold == testFold) {
				testSet.add(rat);
				continue;
			}
			
			popStats.addObservation(rat.rating);

			userStats.get(rat.user).addObservation(rat.rating);
		}
		
		
		for (Integer user : userStats.keySet()) {
			StatsHelper cUser = userStats.get(user);
			NormaliseUser nm = new NormaliseUser(cUser.getMean(), cUser.getVar());
			userNormalisations.put(user, nm);
			
			int count = cUser.getCount();
			
			double newMean = (nm.mean*count + normFactor*popStats.getMean())/(count + normFactor);
			double newVar = (nm.variance*count + normFactor*popStats.getVar())/(count + normFactor);
			nm.mean = newMean;
			nm.variance = newVar;
		}
		
		
		for (Rating mr : ratings){
			double newRatingVal = userNormalisations.get(mr.user).normalise(mr.rating);
			Rating newRat = new Rating(mr.user, mr.movie, newRatingVal, mr.timestamp, mr.fold);
			
			normalisedRatings.add(newRat);
		}
		
	}
	
	
	private class NormaliseUser {
		public double mean = 0;
		public double variance = 1;
		
		public NormaliseUser (double mu, double sigma) {
			mean = mu;
			variance = sigma;
		}
		
		public double normalise (double rating) {
			return (rating - mean)/Math.sqrt(variance);
		}
		
		public double denormalise (double rating) {
			return (rating*Math.sqrt(variance)) + mean;
		}
	}

	private class StatsHelper {
		double mean = 0;
		double meansq = 0;
		int count = 0;
		
		public StatsHelper() { }
		
		public void addObservation(double obs) {
			count += 1;
			double delta = obs - mean;
			mean += delta/count;
			meansq += delta*(obs - mean);
		}
		
		public int getCount(){
			return count;
		}
		
		public double getMean(){
			return mean;
		}
		
		public double getVar(){
			return meansq/(count-1);
		}
	}

}














