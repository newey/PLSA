import org.apfloat.*;
import java.util.*;


public class PLSA implements Model {
	private HashMap <Pair <Integer, Integer>, Apfloat> MCmean;
	private HashMap <Pair <Integer, Integer>, Apfloat> MCvar;
	private HashMap <Integer, ArrayList<Apfloat>> UtoCProbs;
	private Set <Integer> users;
	private Set <Integer> movies;
	private Vector <Integer> testIndexes;
	private int steps;
	private int classes;
	
	
	public PLSA (int steps, int classes) {
		this.steps = steps;
		this.classes = classes;
	}
	
	private void reset(){
		MCmean = new HashMap<Pair<Integer, Integer>, Apfloat>();
		MCvar = new HashMap<Pair<Integer, Integer>, Apfloat>();
		UtoCProbs = new HashMap<Integer, ArrayList<Apfloat>>();
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
		System.out.println("Began emStep");
		Vector<ArrayList<Apfloat>> expectations = new Vector<ArrayList<Apfloat>>(ratings.size());
		
		HashMap <Integer, ArrayList<Apfloat>> newUtoCProbs = new HashMap <Integer, ArrayList<Apfloat>>();
		HashMap <Pair <Integer, Integer>, Apfloat> newMCmean = new HashMap<Pair <Integer, Integer>, Apfloat>();
		HashMap <Integer, Apfloat> sumPerMovie = new HashMap<Integer, Apfloat>();
		
		for (Rating r : ratings){
			ArrayList<Apfloat> curExps = new ArrayList<Apfloat>(classes);
			ArrayList<Apfloat> userClassProbs = UtoCProbs.get(r.user);
			Apfloat probsum = Apfloat.ZERO;
			for (int c = 0; c < classes; c++){
				curExps.add(userClassProbs.get(c).multiply(MCRProb(r.movie, c, r.rating)));
				probsum = probsum.add(curExps.get(c));
			}
			for (int c = 0; c < classes; c++){
				curExps.set(c, curExps.get(c).divide(probsum));
			}
			expectations.add(curExps);
			
		}
		
		HashMap <Pair <Integer, Integer>, Apfloat> newMCvar = new HashMap <Pair <Integer, Integer>, Apfloat>();
		

		System.out.println("Began emStep phase 2");
		for (int i = 0; i < ratings.size(); i++){
			Rating r = ratings.elementAt(i);
			ArrayList<Apfloat> curExps = expectations.elementAt(i);
			// Computing next array of class probabilities per user
			ArrayList<Apfloat> classpr = null;
			if (!newUtoCProbs.containsKey(r.user)){
				classpr = new ArrayList<Apfloat>(classes);
				for (int j = 0; j < classes; j++){
					classpr.add(Apfloat.ZERO);
				}
				newUtoCProbs.put(r.user, classpr);
			} else {
				classpr = newUtoCProbs.get(r.user);
			}
			
			Apfloat curExpssum = Apfloat.ZERO;
			for (int c = 0; c < classes; c++){
				classpr.set(c, classpr.get(c).add(curExps.get(c)));
				curExpssum = curExpssum.add(curExps.get(c));
			}
			
			
			// Add to the sum per movie array
			if (!sumPerMovie.containsKey(r.movie)){
				sumPerMovie.put(r.movie,Apfloat.ZERO);
			}
			sumPerMovie.put(r.movie, sumPerMovie.get(r.movie).add(curExpssum));
			
			// Throw in numerators of newMus (per movie/class)
			for (int c = 0; c < classes; c++) {
				Pair <Integer, Integer> movieClass = new Pair <Integer, Integer> (r.movie, c);
				if (!newMCmean.containsKey(movieClass)){
					newMCmean.put(movieClass, Apfloat.ZERO);
				}
				newMCmean.put(movieClass, newMCmean.get(movieClass).add(new Apfloat(r.rating).multiply(curExps.get(c))));
			}
			
		}
		System.out.println("Began emStep phase 3");		
		// Normalise the new movie/class means
		for (Pair <Integer, Integer> movieClass : newMCmean.keySet()){
			newMCmean.put(movieClass, newMCmean.get(movieClass).divide(sumPerMovie.get(movieClass.a)));
		}
		System.out.println("Began emStep phase 4");		
		// Normalising the new class per user ratings
		for (ArrayList<Apfloat> da : newUtoCProbs.values()) {
			Apfloat sum = Apfloat.ZERO;
			for (Apfloat d : da) {
				sum.add(d);
			}
			for (int i = 0; i < da.size(); i++){
				da.set(i, da.get(i).divide(sum));
			}
		}
		
		
		System.out.println("Began emStep phase 5");		
		// Work out new variances
		for (int i = 0; i < ratings.size(); i++){
			Rating r = ratings.elementAt(i);
			ArrayList<Apfloat> curExps = expectations.elementAt(i);
			// Throw in denominators of newMus (per movie/class)
			for (int c = 0; c < classes; c++) {
				Pair <Integer, Integer> movieClass = new Pair <Integer, Integer> (r.movie, c);
				if (!newMCvar.containsKey(movieClass)){
					newMCvar.put(movieClass, Apfloat.ZERO);
				}
				Apfloat newmu = newMCmean.get(movieClass);
				Apfloat delta = new Apfloat(r.rating).subtract(newmu);
				delta = delta.multiply(delta);
				newMCvar.put(movieClass, newMCvar.get(movieClass).add(delta.multiply(curExps.get(c))));
			}
		}
		System.out.println("Began emStep phase 6");		
		// Normalise the new movie/class variances
		for (Pair <Integer, Integer> movieClass : newMCmean.keySet()){
			newMCvar.put(movieClass, newMCvar.get(movieClass).divide(sumPerMovie.get(movieClass.a)));
		}
		System.out.println("Began emStep phase 7");		
		for (Pair <Integer, Integer> p : MCmean.keySet()){
			if (!newMCmean.containsKey(p)){
				newMCmean.put(p, MCmean.get(p));
				newMCvar.put(p, MCmean.get(p));
			}
		}
		System.out.println("Began emStep phase 8");		
		for (Integer u : UtoCProbs.keySet()){
			if (!newUtoCProbs.containsKey(u)){
				newUtoCProbs.put(u, UtoCProbs.get(u));
			}
		}
		System.out.println("Began emStep phase 9");		
		UtoCProbs = newUtoCProbs;
		MCmean = newMCmean;
		MCvar = newMCvar;
		System.out.println("Finishe an emStep");
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
			ArrayList<Apfloat> al = new ArrayList<Apfloat>(classes);
			double sum = 0;
			for (int i = 0; i < classes; i++){
				double increm = ran.nextDouble()+3;
				al.add(new Apfloat(increm));
				sum += increm;
			}
			Apfloat bigSum = new Apfloat(sum);
			for (int i = 0; i < classes; i++){
				al.add(al.get(i).divide(bigSum));
			}
			UtoCProbs.put(user, al);
		}
		
		
		for (int m : movies) {
			for (int c = 0; c < classes; c++){
				Pair<Integer, Integer> mp = new Pair<Integer, Integer>(m, c);
				MCmean.put(mp, Apfloat.ZERO); // May have to change
				MCvar.put(mp, Apfloat.ONE); // May have to change
			}
		}
		
		for (int i = 0; i < steps; i++){
			emStep(trainingRatings);
		}
		
		Vector<Double> results = new Vector<Double>();
		
		for (int index : testIndexes){
			Rating r = ratings.elementAt(index);
			double guess = 0;
			ArrayList<Apfloat> classProbs = UtoCProbs.get(r.user);
			for (int c = 0; c < classes; c++){
				Pair<Integer, Integer> movieClass = new Pair<Integer, Integer>(r.movie, c);
				guess += MCmean.get(movieClass).multiply(classProbs.get(c)).doubleValue();
			}
			results.add(guess);
		}
		
		return results;
	}
	
	private Apfloat MCRProb(int movie, int cl, double rating){
		Pair<Integer, Integer> mp = new Pair<Integer, Integer>(movie, cl);
		return normalp(rating, MCmean.get(mp), MCvar.get(mp));
	}
	
	private Apfloat normalp(double x, Apfloat mean, Apfloat var){
		Apfloat divisor = ApfloatMath.sqrt(var.multiply(new Apfloat(2*Math.PI)));
		Apfloat delta = mean.subtract(new Apfloat(x));
		delta = delta.multiply(delta).negate().divide(new Apfloat(2)).divide(var);
		
		return ApfloatMath.exp(delta).divide(divisor);
	}
	
	private double normalp(double x, double mean, double var){
		double divisor = Math.sqrt(var*2*Math.PI);
		double delta = mean-x;
		delta = (-delta*delta)/(2*var);
		
		return Math.exp(delta)/(divisor);
	}

}
