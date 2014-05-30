package org.nuiz.parallelRecommend;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

class FileDataList extends DataListImpl {

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
		super.loadData(data);
	}
}
