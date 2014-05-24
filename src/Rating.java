
public class Rating {
	public int user;
	public int movie;
	public double rating;
	public int timestamp;
	public int fold;
	
	public Rating(int user, int movie, double rating, int timestamp, int fold) {
		this.user = user;
		this.movie = movie;
		this.rating = rating;
		this.timestamp = timestamp;
		this.fold = fold;
	}
}
