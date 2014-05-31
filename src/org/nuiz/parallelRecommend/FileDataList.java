package org.nuiz.parallelRecommend;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class FileDataList extends DataListImpl {

	public FileDataList (String fileName, String separator) throws IOException {
		Vector<Datum> data = new Vector<Datum>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while (br.ready()){
			Scanner tk = new Scanner(br.readLine());
			tk.useDelimiter(separator);
			Datum currentDatum = new Datum(tk.nextInt(), tk.nextInt(),tk.nextFloat(),(int) tk.nextLong());
			data.add(currentDatum);
		}
		br.close();
		
		Random r = new Random();
		r.setSeed(100);
		Datum a;
		
		for (int i = 0; i < data.size()-1; i++) {
			int nextIndex = i+r.nextInt(data.size()-i);
			a = data.elementAt(i);
			data.setElementAt(data.elementAt(nextIndex), i);
			data.setElementAt(a, nextIndex);
		}
		
		super.loadData(data);
	}
}
