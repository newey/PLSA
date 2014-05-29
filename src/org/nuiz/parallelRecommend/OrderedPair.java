package org.nuiz.parallelRecommend;

public class OrderedPair<A extends Comparable<A>, B> extends Pair<A, B> implements Comparable<OrderedPair<A,B>> {

	public OrderedPair(A a, B b) {
		super(a, b);
	}

	@Override
	public int compareTo(OrderedPair<A, B> o) {
		return -a.compareTo(o.a);
	}
}
