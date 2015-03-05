package com.cht.iTest.util;

import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

public class ZKUtils {

	public static ListModel<?> createListEnumModel(String className) {
		Class<?> enumType = null;
		try {
			enumType = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ListModelList<>(enumType.getEnumConstants());
	}

}
