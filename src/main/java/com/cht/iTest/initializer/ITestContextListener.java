package com.cht.iTest.initializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.cht.iTest.util.JPAHelper;

@WebListener()
public class ITestContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("init....");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("destroy....");
		JPAHelper.shutdown();
	}
}
