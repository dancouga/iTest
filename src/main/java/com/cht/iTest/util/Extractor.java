package com.cht.iTest.util;

public interface Extractor<P,V> {
	
	V fetch(P param);

}
