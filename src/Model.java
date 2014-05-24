import java.util.Vector;


public interface Model {
	double doFold (final Vector<Rating> ratings, int fold);
	
	Vector <Double> getFit (final Vector<Rating> ratings, int fold);
}
