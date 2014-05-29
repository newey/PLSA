package org.nuiz.parallelRecommend;

public class Pair <A, B> {
	public A a;
	public B b;
	
	public Pair (A a, B b){
		this.a = a;
		this.b = b;
	}
	
	@Override
	public int hashCode (){
		return a.hashCode() ^ b.hashCode()*137;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals (Object o){
		if (o == null) {
			return false;
		} else if (o.getClass() != this.getClass()) {
			return false;
		} else {
			Pair <A, B> other = ((Pair<A, B>) o);
			return other.a.equals(a) && other.b.equals(b);
		}
	}
}
