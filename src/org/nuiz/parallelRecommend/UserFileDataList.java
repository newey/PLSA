package org.nuiz.parallelRecommend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

/**
 * Gets the user data from a given file. This is tailored for the movieLens 1m dataset's user data.
 * See their readme for how it's stuctured.
 * @author Robert Newey
 */
public class UserFileDataList extends DataListImpl {

	public static final int GENDER_ITEM = 1000000;
	public static final int GENDER_CLASSES = 2;
	public static final int AGE_ITEM = 1000001;
	public static final int AGE_CLASSES = 7;
	public static final int OCCUPATION_ITEM = 1000002;
	public static final int OCCUPATION_CLASSES = 21;
	
	public UserFileDataList (String fileName, String separator) throws IOException {
		HashMap<Integer, Integer>ageMap = new HashMap<Integer, Integer>();
		ageMap.put(1, 0);
		ageMap.put(18, 1);
		ageMap.put(25, 2);
		ageMap.put(35, 3);
		ageMap.put(45, 4);
		ageMap.put(50, 5);
		ageMap.put(56, 6);

		Vector<Datum> cdata = new Vector<Datum>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while (br.ready()){
			Scanner tk = new Scanner(br.readLine());
			tk.useDelimiter(separator);
			
			int user = tk.nextInt();
			int gender = tk.next().equals("M") ? 1 : 0;
			int age = tk.nextInt();
			int occupation = tk.nextInt();
			//int zipCode = tk.nextInt();
			
			cdata.add(new Datum(user, GENDER_ITEM, gender, 0));
			cdata.add(new Datum(user, AGE_ITEM, ageMap.get(age), 0));
			cdata.add(new Datum(user, OCCUPATION_ITEM, occupation, 0));
			//data.add(new Datum(user, 3, zipCode, 0));
		}
		br.close();
		super.loadData(cdata);
	}
}
