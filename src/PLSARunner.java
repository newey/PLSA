import java.util.*;
import java.io.*;

public class PLSARunner {

	public static void main(String[] args) throws IOException {
		int classes = 5;
		int topToShow = 10;
		
		Vector<Rating> ratings = new Vector<Rating>();
		
		//BufferedReader br = new BufferedReader(new FileReader("ml-100K/u.data"));
		BufferedReader br = new BufferedReader(new FileReader("/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1M/ratings.dat"));
		while (br.ready()){
			Scanner tk = new Scanner(br.readLine());
			tk.useDelimiter("::");
			ratings.add(new Rating(tk.nextInt(), tk.nextInt(),tk.nextInt(),tk.nextInt(),1));
		}
		br.close();
		
		PLSADoubles model = new PLSADoubles(20, classes);
		NormalisationModel normalise = new NormalisationModel(model, 15);
		
		normalise.doFold(ratings, 0);
		
		HashMap<Integer, String> movieTitles = new HashMap<Integer, String>();
		
		//BufferedReader brm = new BufferedReader(new FileReader("/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-100K/u.item"));
		BufferedReader brm = new BufferedReader(new FileReader("/Users/robert/Documents/ScalaWorkspace/LocalRec/ml-1M/movies.dat"));
		while (brm.ready()){
			Scanner tk = new Scanner(brm.readLine());
			//tk.useDelimiter("\\|");
			tk.useDelimiter("::");
			int movie = tk.nextInt();
			String title = new String(tk.next());
			movieTitles.put(movie, title);
		}
		brm.close();
		
		for (int c = 0 ; c < classes; c++){
			PriorityQueue <OrderedPair<Double, String> > pq = new PriorityQueue<OrderedPair<Double, String>>();
			for (int movie : model.movies){
				Pair <Integer, Integer> movieClass = new Pair<Integer, Integer> (movie, c);
				double rating = model.MCmean.get(movieClass);
				OrderedPair <Double, String> mp = new OrderedPair<Double,String>(rating, movieTitles.get(movie));
				pq.add(mp);
			}
			
			System.out.printf("\nClass %d likes:\n", c);
			for (int i = 0; i < topToShow; i++){
				System.out.printf("%s %f\n", pq.peek().b, pq.peek().a);
				pq.remove();
			}
		}
		
	}

}
