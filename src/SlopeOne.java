import java.util.*;


public class SlopeOne implements Model {
	Vector<Integer> testIndexes = null;
	public SlopeOne(){
		
	}
	
	private class DiffNCount {
		public double diff;
		public int count;
		
		public DiffNCount() {
			diff = 0;
			count = 0;
		}
		
		public void addDiff(double d) {
			diff += d;
			count++;
		}
		
		public double average(){
			return diff/count;
		}
	}
	
	private class MoviePair {
		protected int m1;
		protected int m2;
		public MoviePair(int m1, int m2){
			if (m1 > m2) {
				m2 ^= m1;
				m1 ^= m2;
				m2 ^= m1;
			}
		}
		
		@Override
		public int hashCode(){
			return m2*909091+m1;
		}
		
		@Override
		public boolean equals(Object mp){
			if (mp.getClass() != this.getClass()){
				return false;
			};
			MoviePair o = (MoviePair) mp;
			return o.m1 == m1 && o.m2 == m2;
		}
	}
	
	@Override
	public Vector<Double> getFit(Vector<Rating> ratings, int fold) {
		HashMap<Integer, List<Integer>> userToMovieIndexes = new HashMap<Integer, List<Integer>>(); // For each user, stores the movies seen so far
		testIndexes = new Vector<Integer>();
		HashMap<MoviePair, DiffNCount> differences = new HashMap<MoviePair, DiffNCount>();
		Vector<Double> preds = new Vector<Double>();
		
		
		for (int i = 0; i < ratings.size(); i++){
			Rating cr = ratings.elementAt(i);
			List<Integer> movieIndexes = null;
			if (fold == cr.fold){
				testIndexes.add(i);
			} else if (userToMovieIndexes.containsKey(cr.user)){
				movieIndexes = userToMovieIndexes.get(cr.user);
				for (Integer j : movieIndexes) {
					Rating other = ratings.elementAt(j);
					MoviePair mp = new MoviePair(cr.movie, other.movie);
					DiffNCount cpair = null;
					if (!differences.containsKey(mp)){
						cpair = new DiffNCount();
						differences.put(mp, cpair);
					} else {
						cpair = differences.get(mp);
					}
					// difference refers to average of (bigger number) - (smaller number)
					cpair.addDiff((cr.rating - other.rating) * Math.signum(cr.movie - other.movie));
				}
				movieIndexes.add(i);
			} else {
				movieIndexes = new LinkedList<Integer>();
				userToMovieIndexes.put(cr.user, movieIndexes);
				movieIndexes.add(i);
			}
			
		}
		
		
		for (Integer i : testIndexes){
			double estimateSum = 0;
			int estimateCount = 0;
			Rating cr = ratings.elementAt(i);
			for (Integer j : userToMovieIndexes.get(ratings.elementAt(i).user)) {
				Rating other = ratings.elementAt(j);
				MoviePair mp = new MoviePair(cr.movie, other.movie);
				if (differences.containsKey(mp)){
					DiffNCount dc = differences.get(mp);
					estimateCount += 1;
					estimateSum += (other.rating + dc.average()*Math.signum(cr.movie - other.movie))*1;
				}
			}
			estimateSum /= estimateCount;
			preds.add(estimateSum);
		}
		
		return preds;
	}
	
	@Override
	public double doFold(final Vector<Rating> ratings, int fold) {
		
		double MSE = 0;
		Vector <Double> preds = getFit (ratings, fold);
		
		for (int i = 0; i < testIndexes.size(); i++){
			double estimate = preds.elementAt(i);
			Rating cr = ratings.elementAt(testIndexes.elementAt(i));
			MSE += (estimate - cr.rating)*(estimate - cr.rating);
		}
		
		
		return MSE/testIndexes.size();
	}



}
