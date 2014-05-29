package org.nuiz.utils;

public interface MapperFactory <T> {
	public Mapper<T> getMapper(T t);
}
