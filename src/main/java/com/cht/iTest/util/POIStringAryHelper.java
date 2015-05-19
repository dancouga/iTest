package com.cht.iTest.util;

import java.io.InputStream;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;

public interface POIStringAryHelper {

	InputStream getXLSInputStream();
	
	public abstract Map<String, POIConvertor<Row, Object>> getSheetConvertorMap();

}
