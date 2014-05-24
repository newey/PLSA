import java.util.Vector;


public class CVRunner {
	public static CVReport run (Model model, Vector<Rating> ratings, int nfolds) {
		double errors[] = new double[nfolds];
		double sum = 0;
		for (int i = 0; i < nfolds; i++){
			errors[i] = model.doFold(ratings, i);
			System.out.println(errors[i]);
			sum += errors[i];
		}
		double var = 0;
		double mean = sum / nfolds;
		for (int i = 0; i < nfolds; i++){
			var += (errors[i] - mean)*(errors[i] - mean);
		}
		var /= nfolds;
		return new CVReport(mean, var);
	}
}
