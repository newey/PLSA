package org.nuiz.parallelRecommend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class UserFileDataList implements DataList {
	private Vector <Datum> data;
	private int maxItem;
	private int numUsers;
	private int numItems;
	private Set<Integer> users;
	private Set<Integer> items;
	public static final int GENDER_ITEM = 1000000;
	public static final int GENDER_CLASSES = 2;
	public static final int AGE_ITEM = 1000001;
	public static final int AGE_CLASSES = 7;
	public static final int OCCUPATION_ITEM = 1000002;
	public static final int OCCUPATION_CLASSES = 21;
	
	public UserFileDataList (String fileName, String separator) throws IOException {
		data = new Vector<Datum>();
		users = new HashSet<Integer>();
		items = new HashSet<Integer>();
		maxItem = OCCUPATION_ITEM;
		items.add(GENDER_ITEM);
		items.add(AGE_ITEM);
		items.add(OCCUPATION_ITEM);
		
		
		HashMap<Integer, Integer>ageMap = new HashMap<Integer, Integer>();
		ageMap.put(1, 0);
		ageMap.put(18, 1);
		ageMap.put(25, 2);
		ageMap.put(35, 3);
		ageMap.put(45, 4);
		ageMap.put(50, 5);
		ageMap.put(56, 6);
		
		
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while (br.ready()){
			Scanner tk = new Scanner(br.readLine());
			tk.useDelimiter(separator);
			
			int user = tk.nextInt();
			int gender = tk.next().equals("M") ? 1 : 0;
			int age = tk.nextInt();
			int occupation = tk.nextInt();
			//int zipCode = tk.nextInt();
			
			data.add(new Datum(user, GENDER_ITEM, gender, 0));
			data.add(new Datum(user, AGE_ITEM, ageMap.get(age), 0));
			data.add(new Datum(user, OCCUPATION_ITEM, occupation, 0));
			//data.add(new Datum(user, 3, zipCode, 0));
			
			users.add(user);
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
		return users.size();
	}

	@Override
	public int getNumItems() {
		return items.size();
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
