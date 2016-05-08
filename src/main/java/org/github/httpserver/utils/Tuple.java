package org.github.httpserver.utils;

public class Tuple<K, V> {

	private K v1;
	private V v2;
	
	public Tuple(K v1, V v2) {
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public K v1() {
		return v1;
	}
	
	public V v2() {
		return v2;
	}
}
