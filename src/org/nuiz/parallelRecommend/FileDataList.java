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
	private int maxItem;
	private int numUsers;
	private int numItems;
	private Set<Integer> users;
	private Set<Integer> items;
	
	public FileDataList (String fileName, String separator) throws IOException {
		data = new Vector<Datum>();
		users = new HashSet<Integer>();
		items = new HashSet<Integer>();
		maxItem = -1;
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while (br.ready()){
			Scanner tk = new Scanner(br.readLine());
			tk.useDelimiter(separator);
			Datum currentDatum = new Datum(tk.nextInt(), tk.nextInt(),tk.nextInt(), tk.nextInt());
			data.add(currentDatum);
			users.add(currentDatum.getUser());
			items.add(currentDatum.getItem());
			maxItem = maxItem < currentDatum.getItem() ? currentDatum.getItem() : maxItem;
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

	@Override
	public int getMaxItem() {
		return maxItem;
	}

	@Override
	public Set<Integer> getUsers() {
		return users;
	}

	@Override
	public Set<Integer> getItems() {
		return items;
	}

}
