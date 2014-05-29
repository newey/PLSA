package org.nuiz.parallelRecommend;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

public class BinaryFileDataList extends DataListImpl {

	public BinaryFileDataList (String fileName) throws IOException {
		Vector<Datum> data = new Vector<Datum>();
		FileInputStream br = new FileInputStream(fileName);
		long sizeleft = br.getChannel().size();
		byte[] cdata = new byte[9];
		int user;
		int movie;
		double rating;
		while (sizeleft > 0){
			br.read(cdata);
			user = cdata[0]*(1<<24) + cdata[1]*(1<<16) + cdata[2]*(1<<8) + cdata[3];
			movie = cdata[4]*(1<<24) + cdata[5]*(1<<16) + cdata[6]*(1<<8) + cdata[7];
			rating = cdata[8]/2.0;
			Datum currentDatum = new Datum(user, movie, rating,0);
			data.add(currentDatum);
			sizeleft -= cdata.length;
		}
		br.close();
		super.loadData(data);
	}
}
