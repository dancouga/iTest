package com.cht.iTest.util;

import java.util.List;
import java.util.Map;


public interface POIObjHelper {
	
	public abstract Map<String,POIConvertor<Object, String[]>> getSheetConvertorMap();
	public abstract Map<String, String[]> getSheetHeaderMap();
	public abstract Map<String, List<Object>> getSheetRowMap();

}
