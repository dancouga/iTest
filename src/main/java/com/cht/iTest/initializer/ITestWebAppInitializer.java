package com.cht.iTest.initializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;


/**
 * 
 * 當Web啟動時，進行Spring的載入
 * 
 * @author wen
 *
 */
public class ITestWebAppInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext container) throws ServletException {
		container.setInitParameter(ContextLoaderListener.CONFIG_LOCATION_PARAM, "classpath:applicationContext.xml");
		container.addListener(new ContextLoaderListener());
		container.addListener(new RequestContextListener());
	}
}