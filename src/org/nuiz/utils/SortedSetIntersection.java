package org.nuiz.utils;

import java.util.Iterator;
import java.util.SortedSet;

public class SortedSetIntersection {
	public static int size(SortedSet<Integer> a, SortedSet<Integer> b) {
		Iterator<Integer> ai = a.iterator();
		Iterator<Integer> bi = b.iterator();
		
		if (!ai.hasNext() || !bi.hasNext()){
			return 0;
		}
		
		Integer acur = ai.next();
		Integer bcur = bi.next();
		int size = 0;
		
		while (acur != null && bcur != null) {
			if (acur == bcur) {
				size++;
			}
			if (!ai.hasNext() || !bi.hasNext()){
				break;
			}
			if (acur < bcur) {
				acur = ai.next();
			} else if (bcur < acur) {
				bcur = bi.next();
			} else {
				acur = ai.next();
				bcur = bi.next();
			}
		}
		return size;
	}
}
