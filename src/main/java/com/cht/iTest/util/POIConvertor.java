package com.cht.iTest.util;

public interface POIConvertor<P,V> {
	
	V work(P p);
	
	int headerRow();

}
