package org.nuiz.parallelRecommend;

public interface Model {
	public void fit(DataList data);
	public Iterable<Double> predict(DataList data);
}
