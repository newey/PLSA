import java.util.Random;
import java.util.Vector;


public class GuessZero implements Model {

	@Override
	public double doFold(Vector<Rating> ratings, int fold) {
		Vector <Double> preds = getFit(ratings, fold);
		int predIndex = 0;
		double MSE = 0;
		
		for (Rating rat : ratings){
			if (fold == rat.fold){
				MSE += (preds.elementAt(predIndex)-rat.rating)*(preds.elementAt(predIndex)-rat.rating);
				predIndex++;
			}
		}
		return MSE/predIndex;
	}

	@Override
	public Vector<Double> getFit(Vector<Rating> ratings, int fold) {
		Vector <Double> preds = new Vector<Double>();
		
		Random r = new Random();
		r.setSeed(fold);
	
		for (Rating rat : ratings){
			if (fold == rat.fold){
				int guess = 0;
				preds.add((double) guess);
			}
		}
		
		return preds;
	}

}
