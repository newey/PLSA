import java.io.*;
import java.util.*;

public class App {

	public static void main(String[] args) throws IOException {
		Random rand = new Random();
		rand.setSeed(1);
		int nfolds = Integer.valueOf(args[1]);
		int classes = Integer.valueOf(args[2]);
		int steps = Integer.valueOf(args[3]);
		int normalise = Integer.valueOf(args[3]);
		
		
		Vector<Rating> ratings = new Vector<Rating>();
		
		//BufferedReader br = new BufferedReader(new FileReader("ml-100K/u.data"));
		BufferedReader br = new BufferedReader(new FileReader("/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1M/ratings.dat"));
		while (br.ready()){
			Scanner tk = new Scanner(br.readLine());
			tk.useDelimiter("::");
			ratings.add(new Rating(tk.nextInt(), tk.nextInt(),tk.nextInt(),tk.nextInt(),rand.nextInt(nfolds)));
		}
		br.close();
		

		//CVReport cv = CVRunner.run(new NormalisationModel(new SlopeOne(), 3), ratings, 10);
		//CVReport cv = CVRunner.run(new NormalisationModel(new GuessZero(), 3), ratings, 10);
		//CVReport cv = CVRunner.run(new GuessZero(), ratings, 10);
		//CVReport cv = CVRunner.run(new SlopeOne(), ratings, 10);
		//CVReport cv = CVRunner.run(new RandomModel(), ratings, 10);
		CVReport cv = CVRunner.run(new NormalisationModel(new PLSADoubles(steps, classes), normalise), ratings, nfolds);
		
		System.out.printf("Mean: %f,  SD: %f\n", cv.MSE, cv.var);
		
	}

}
