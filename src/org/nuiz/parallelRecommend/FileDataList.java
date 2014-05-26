package org.nuiz.parallelRecommend;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class FileDataList implements DataList {
	private Vector <Datum> data;
	private int numUsers;
	private int numItems;
	
	public FileDataList (String fileName, String separator) throws IOException {
		data = new Vector<Datum>();
		Set<Integer> users = new HashSet<Integer>();
		Set<Integer> items = new HashSet<Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while (br.ready()){
			Scanner tk = new Scanner(br.readLine());
			tk.useDelimiter(separator);
			Datum currentDatum = new Datum(tk.nextInt(), tk.nextInt(),tk.nextInt(), tk.nextInt());
			data.add(currentDatum);
			users.add(currentDatum.getUser());
			items.add(currentDatum.getItem());
		}
		br.close();

		numUsers = users.size();
		numItems = items.size();
	}

	@Override
	public Iterator<Datum> iterator() {
		return data.iterator();
	}

	@Override
	public int getNumUsers() {
		return numUsers;
	}

	@Override
	public int getNumItems() {
		return numItems;
	}

}
