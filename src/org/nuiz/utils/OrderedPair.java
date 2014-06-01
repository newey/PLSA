package org.nuiz.utils;


/**
 * Pair which allows comparisons made by the first element. Useful for sorting.
 * This will sort into decreasing order
 * @param <A> Any comparable type
 * @param <B> Any type
 * @author Robert Newey
 */
public class OrderedPair<A extends Comparable<A>, B> extends Pair<A, B> implements Comparable<OrderedPair<A,B>> {

	public OrderedPair(A a, B b) {
		super(a, b);
	}

	@Override
	public int compareTo(OrderedPair<A, B> o) {
		return -a.compareTo(o.a);
	}
}
