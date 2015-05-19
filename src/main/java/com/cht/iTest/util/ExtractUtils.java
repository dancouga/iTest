package com.cht.iTest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;

@SuppressWarnings("unchecked")
public class ExtractUtils {

	public static <K, V> Map<K, V> extract2Map(Collection<?> collection, String keyPropertyName, String valuePropertyName) {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();

		try {
			for (Object obj : collection) {
				Object key = PropertyUtils.getProperty(obj, keyPropertyName);
				Object value = PropertyUtils.getProperty(obj, valuePropertyName);
				map.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Map<K, V>) map;
	}

	public static <K, V> Map<K, V> extract2Map(Collection<V> collection, MapKey<K, V> extractor) {
		Map<K, V> map = new LinkedHashMap<K, V>();

		try {
			for (V param : collection) {
				map.put(extractor.key(param), param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Map<K, V>) map;
	}
	

	public static <K, V, P> Map<K, V> extract2Map(Collection<P> collection, MapEntry<K, V, P> extractor) {
		Map<K, V> map = new LinkedHashMap<K, V>();

		try {
			for (P param : collection) {
				map.put(extractor.key(param), extractor.val(param));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Map<K, V>) map;
	}

	public static <K, V> Map<K, List<V>> extract2ListMap(Collection<?> collection, String keyPropertyName, String valuePropertyName) {
		Map<K, List<V>> map = new LinkedHashMap<K, List<V>>();

		try {
			for (Object obj : collection) {
				K key = (K) PropertyUtils.getProperty(obj, keyPropertyName);
				V value = (V) PropertyUtils.getProperty(obj, valuePropertyName);

				if (!map.containsKey(key)) {
					map.put(key, new ArrayList<V>());
				}

				map.get(key).add(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}

	public static <K, V> Map<K, V> convert2Map(Collection<V> collection, String keyPropertyName) {
		Map<K, V> map = new LinkedHashMap<K, V>();

		try {
			for (V obj : collection) {
				Object key = PropertyUtils.getProperty(obj, keyPropertyName);
				if (key != null) {
					map.put((K) key, obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}

	public static <K, V> Map<K, List<V>> convert2ListMap(Collection<V> collection, String keyPropertyName) {
		Map<K, List<V>> map = new LinkedHashMap<K, List<V>>();

		try {
			K key = null;
			List<V> list = null;

			for (V obj : collection) {
				key = (K) PropertyUtils.getProperty(obj, keyPropertyName);

				if (map.containsKey(key)) {
					list = map.get(key);
				} else {
					list = new ArrayList<V>();
					map.put(key, list);
				}

				list.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}

	public static <T, V> V extract(Collection<T> collection, Extractor<T, V> extractor) {
		for (T obj : collection) {
			V val = extractor.fetch(obj);

			if (val != null) {
				return val;
			}
		}
		return null;
	}

	public static <T> List<T> extract2List(Collection<?> collection, String propertyName, Class<T> clazz) {
		List<T> result = new ArrayList<T>();
		extractToTargetCollection(collection, result, propertyName, clazz);
		return result;
	}

	public static <T, V> List<V> extract2List(Collection<T> collection, Extractor<T, V> extractor) {
		List<V> result = new ArrayList<V>();
		addFromExtractor(result, collection, extractor);
		return result;
	}

	public static <T> boolean isSatisfied(Collection<T> collection, Extractor<T, Boolean> extractor) {
		for (T obj : collection) {
			Boolean val = extractor.fetch(obj);

			if (!val) {
				return false;
			}
		}

		return true;
	}

	public static <T> Set<T> extract2Set(Collection<?> collection, String propertyName, Class<T> clazz) {
		Set<T> result = new HashSet<T>();
		extractToTargetCollection(collection, result, propertyName, clazz);
		return result;
	}

	public static <T, V> Set<V> extract2Set(Collection<T> collection, Extractor<T, V> extractor) {
		Set<V> result = new LinkedHashSet<V>();
		addFromExtractor(result, collection, extractor);
		return result;
	}

	public static String extractJoinString(Collection<?> source, String propertyName, String separator) {
		StringBuilder sb = new StringBuilder();

		for (Object obj : source) {
			if (sb.length() > 0) {
				sb.append(separator);
			}

			try {
				sb.append(PropertyUtils.getProperty(obj, propertyName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	private static <T> void extractToTargetCollection(Collection<?> source, Collection<T> target, String propertyName, Class<T> clazz) {
		for (Object obj : source) {
			try {
				Object val = PropertyUtils.getProperty(obj, propertyName);

				if (val == null) {
					continue;
				}

				target.add(clazz.cast(val));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static <T, V> void addFromExtractor(Collection<V> result, Collection<T> collection, Extractor<T, V> extractor) {
		for (T obj : collection) {
			V val = extractor.fetch(obj);

			if (val != null) {
				result.add(val);
			}
		}
	}

	public static <E> List<String> extractName2List(Enum<?>[] values) {
		List<String> result = new ArrayList<String>();

		for (Enum<?> temp : values) {
			result.add(temp.name());
		}

		return result;
	}

}
