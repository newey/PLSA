import java.util.*;


public class PLSADoubles implements Model {
	public HashMap <Pair <Integer, Integer>, Double> MCmean;
	private HashMap <Pair <Integer, Integer>, Double> MCvar;
	private HashMap <Integer, ArrayList<Double>> UtoCProbs;
	private Set <Integer> users;
	public Set <Integer> movies;
	private Vector <Integer> testIndexes;
	private int steps;
	private int classes;
	
	
	public PLSADoubles (int steps, int classes) {
		this.steps = steps;
		this.classes = classes;
	}
	
	private void reset(){
		MCmean = new HashMap<Pair<Integer, Integer>, Double>();
		MCvar = new HashMap<Pair<Integer, Integer>, Double>();
		UtoCProbs = new HashMap<Integer, ArrayList<Double>>();
		users = new HashSet<Integer>();
		movies = new HashSet<Integer>();
		testIndexes = new Vector<Integer>();
	}
	
	@Override
	public double doFold(Vector<Rating> ratings, int fold) {
		double MSE = 0;
		Vector <Double> preds = getFit (ratings, fold);
		
		for (int i = 0; i < testIndexes.size(); i++){
			double estimate = preds.elementAt(i);
			Rating cr = ratings.elementAt(testIndexes.elementAt(i));
			MSE += (estimate - cr.rating)*(estimate - cr.rating);
		}
		
		return MSE/testIndexes.size();
	}

	private void emStep (Vector<Rating> ratings) {
		Vector<ArrayList<Double>> expectations = new Vector<ArrayList<Double>>(ratings.size());
		
		HashMap <Integer, ArrayList<Double>> newUtoCProbs = new HashMap <Integer, ArrayList<Double>>();
		HashMap <Pair <Integer, Integer>, Double> newMCmean = new HashMap<Pair <Integer, Integer>, Double>();
		HashMap <Integer, Double> sumPerMovie = new HashMap<Integer, Double>();
		
		for (Rating r : ratings){
			ArrayList<Double> curExps = new ArrayList<Double>(classes);
			ArrayList<Double> userClassProbs = UtoCProbs.get(r.user);
			Double probsum = 0.0;
			for (int c = 0; c < classes; c++){
				curExps.add(userClassProbs.get(c) * MCRProb(r.movie, c, r.rating));
				probsum += curExps.get(c);
			}
			
			for (int c = 0; c < classes; c++){
				curExps.set(c, curExps.get(c)/probsum);
			}
			expectations.add(curExps);
			
		}
		
		HashMap <Pair <Integer, Integer>, Double> newMCvar = new HashMap <Pair <Integer, Integer>, Double>();
		
		for (int i = 0; i < ratings.size(); i++){
			Rating r = ratings.elementAt(i);
			ArrayList<Double> curExps = expectations.elementAt(i);
			// Computing next array of class probabilities per user
			ArrayList<Double> classpr = null;
			if (!newUtoCProbs.containsKey(r.user)){
				classpr = new ArrayList<Double>(classes);
				for (int j = 0; j < classes; j++){
					classpr.add(0.0);
				}
				newUtoCProbs.put(r.user, classpr);
			} else {
				classpr = newUtoCProbs.get(r.user);
			}
			
			Double curExpssum = 0.0;
			for (int c = 0; c < classes; c++){
				classpr.set(c, classpr.get(c) + curExps.get(c));
				curExpssum += curExps.get(c);
			}
			
			
			// Add to the sum per movie array
			if (!sumPerMovie.containsKey(r.movie)){
				sumPerMovie.put(r.movie,0.0);
			}
			sumPerMovie.put(r.movie, sumPerMovie.get(r.movie) + curExpssum);
			
			// Throw in numerators of newMus (per movie/class)
			for (int c = 0; c < classes; c++) {
				Pair <Integer, Integer> movieClass = new Pair <Integer, Integer> (r.movie, c);
				if (!newMCmean.containsKey(movieClass)){
					newMCmean.put(movieClass, 0.0);
				}
				newMCmean.put(movieClass, newMCmean.get(movieClass)+r.rating*curExps.get(c));
			}
			
		}
		// Normalise the new movie/class means
		for (Pair <Integer, Integer> movieClass : newMCmean.keySet()){
			newMCmean.put(movieClass, newMCmean.get(movieClass)/(sumPerMovie.get(movieClass.a)));
		}
		// Normalising the new class per user ratings
		for (ArrayList<Double> da : newUtoCProbs.values()) {
			Double sum = 0.0;
			for (Double d : da) {
				sum += d;
			}
			for (int i = 0; i < da.size(); i++){
				da.set(i, da.get(i)/sum);
			}
		}
			
		// Work out new variances
		for (int i = 0; i < ratings.size(); i++){
			Rating r = ratings.elementAt(i);
			ArrayList<Double> curExps = expectations.elementAt(i);
			// Throw in denominators of newMus (per movie/class)
			for (int c = 0; c < classes; c++) {
				Pair <Integer, Integer> movieClass = new Pair <Integer, Integer> (r.movie, c);
				if (!newMCvar.containsKey(movieClass)){
					newMCvar.put(movieClass, 0.0);
				}
				Double newmu = newMCmean.get(movieClass);
				Double delta = r.rating - newmu;
				delta *= delta;
				newMCvar.put(movieClass, newMCvar.get(movieClass)+delta*curExps.get(c));
			}
		}	
		// Normalise the new movie/class variances
		for (Pair <Integer, Integer> movieClass : newMCmean.keySet()){
			newMCvar.put(movieClass, newMCvar.get(movieClass)/sumPerMovie.get(movieClass.a));
		}
		for (Pair <Integer, Integer> p : MCmean.keySet()){
			if (!newMCmean.containsKey(p)){
				newMCmean.put(p, MCmean.get(p));
				newMCvar.put(p, MCmean.get(p));
			}
		}	
		for (Integer u : UtoCProbs.keySet()){
			if (!newUtoCProbs.containsKey(u)){
				newUtoCProbs.put(u, UtoCProbs.get(u));
			}
		}		
		UtoCProbs = newUtoCProbs;
		MCmean = newMCmean;
		MCvar = newMCvar;
	}
	
	
	@Override
	public Vector<Double> getFit(Vector<Rating> ratings, int fold) {
		reset();
		Vector <Rating> trainingRatings = new Vector <Rating>();
		for (int i = 0; i < ratings.size(); i++) {
			Rating r = ratings.elementAt(i);
			users.add(r.user);
			movies.add(r.movie);
			if (r.fold == fold){
				testIndexes.add(i);
			} else {
				trainingRatings.add(r);
			}
		}
		
		Random ran = new Random();
		ran.setSeed(1234);
		
		for (Integer user : users) {
			ArrayList<Double> al = new ArrayList<Double>(classes);
			double sum = 0;
			for (int i = 0; i < classes; i++){
				double increm = ran.nextDouble()+3;
				al.add(increm);
				sum += increm;
			}
			for (int i = 0; i < classes; i++){
				al.add(al.get(i)/sum);
			}
			UtoCProbs.put(user, al);
		}
		
		
		for (int m : movies) {
			for (int c = 0; c < classes; c++){
				Pair<Integer, Integer> mp = new Pair<Integer, Integer>(m, c);
				MCmean.put(mp, 0.0); // May have to change
				MCvar.put(mp, 1.0); // May have to change
			}
		}
		
		for (int i = 0; i < steps; i++){
			emStep(trainingRatings);
		}
		
		Vector<Double> results = new Vector<Double>();
		
		for (int index : testIndexes){
			Rating r = ratings.elementAt(index);
			double guess = 0;
			ArrayList<Double> classProbs = UtoCProbs.get(r.user);
			for (int c = 0; c < classes; c++){
				Pair<Integer, Integer> movieClass = new Pair<Integer, Integer>(r.movie, c);
				guess += MCmean.get(movieClass)*classProbs.get(c);
			}
			results.add(guess);
		}
		
		return results;
	}
	
	private Double MCRProb(int movie, int cl, double rating){
		Pair<Integer, Integer> mp = new Pair<Integer, Integer>(movie, cl);
		return normalp(rating, MCmean.get(mp), MCvar.get(mp));
	}
	
	private double normalp(double x, double mean, double var){
		if (Double.isNaN(mean) || Double.isNaN(var) || Double.isNaN(x)){
			throw new IllegalArgumentException();
		}
		
		if (var == 0) {
			return x == mean ? 1.0 : 0.0;
		}
		
		double divisor = Math.sqrt(var*2*Math.PI);
		double delta = mean-x;
		delta = (-delta*delta)/(2*var);
		double retval = Math.exp(delta)/(divisor);
		
		if (Double.isNaN(retval)){
			throw new ArithmeticException();
		}
		
		return retval;
	}

}
