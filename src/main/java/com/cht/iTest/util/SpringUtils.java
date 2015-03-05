package com.cht.iTest.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SpringUtils {

	private static ApplicationContext applicationContext;

	@Autowired
	private SpringUtils(ApplicationContext applicationContext) {
		SpringUtils.applicationContext = applicationContext;
	}

	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}

	public static <T> T getBean(Class<T> clazz, String beanName) {
		return applicationContext.getBean(clazz, beanName);
	}
	
	public static Resource getResource(String name) {
		return applicationContext.getResource(name);
	}

}
