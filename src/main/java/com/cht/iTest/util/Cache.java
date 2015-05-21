package com.cht.iTest.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.cht.iTest.entity.ConfigParam;
import com.cht.iTest.service.CommonService;

/**
 * 
 * 系統快取
 * 
 * @author wen
 *
 */
public class Cache {

	private static Map<String, Map<String, Object>> myCache = new ConcurrentHashMap<String, Map<String, Object>>();

	/**
	 * 
	 * 系統快取-系統變數類別名稱
	 * 
	 */
	public static String SYSTEM_CATEGORY = "SYSTEM_CATEGORY";

	/**
	 * 
	 * 清除所有快取
	 * 
	 */
	public static void clearAll() {
		myCache.clear();
	}

	/**
	 * 
	 * 針對特定類別建立快取
	 * 
	 * @param category
	 * @param value
	 */
	public static void putByCategory(String category, Map<String, Object> value) {
		myCache.put(category, value);
	}

	/**
	 * 
	 * 取得特定類別快取
	 * 
	 * @param category
	 * @return
	 */
	public static Map<String, Object> getCategory(String category) {
		return myCache.get(category);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(String category, String key) {
		if (!myCache.containsKey(category)) {
			return null;
		}

		return (T) myCache.get(category).get(key);
	}

	public static Map<String, Object> removeCategory(String category) {
		return myCache.remove(category);
	}

	@SuppressWarnings("unchecked")
	public static <T> T remove(String category, String key) {
		if (!myCache.containsKey(category)) {
			return null;
		}

		return (T) myCache.get(category).remove(key);
	}

	public static Map<String, Object> getSysCategory() {
		if (isCategoryEmpty(SYSTEM_CATEGORY)) {
			refreshSysCategory();
		}
		
		return myCache.get(SYSTEM_CATEGORY);
	}

	public static boolean isCategoryEmpty(String category) {
		return !myCache.containsKey(category) || myCache.get(category).isEmpty();
	}

	public static void putSysCategory(Map<String, Object> value) {
		myCache.put(SYSTEM_CATEGORY, value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getSysCateVal(String key) {
		if (isCategoryEmpty(SYSTEM_CATEGORY)) {
			refreshSysCategory();
		}

		return (T) myCache.get(SYSTEM_CATEGORY).get(key);
	}

	public static String getSysCateVal(String key, String defaultVal) {
		if (isCategoryEmpty(SYSTEM_CATEGORY)) {
			refreshSysCategory();
		}

		String val = (String) myCache.get(SYSTEM_CATEGORY).get(key);
		return StringUtils.isBlank(val) ? defaultVal : val;
	}

	public static void refreshSysCategory() {
		List<ConfigParam> list = SpringUtils.getBean(CommonService.class).getAllEntities(ConfigParam.class);

		if (list.isEmpty()) {
			SpringUtils.getBean(CommonService.class).initSysConfigParam();
			list = SpringUtils.getBean(CommonService.class).getAllEntities(ConfigParam.class);
		}

		Cache.putSysCategory(ExtractUtils.extract2Map(list, "name", "value"));
	}

}
