package com.cht.iTest.service.impl;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cht.iTest.entity.ConfigParam;
import com.cht.iTest.entity.TestPlan;
import com.cht.iTest.selenium.App;
import com.cht.iTest.service.MyService;
import com.cht.iTest.util.JPAHelper;

@Service("myService")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyServiceImpl implements MyService {

	@SuppressWarnings("unchecked")
	@Transactional
	public <T> T saveOrUpdate(Object entity) {
		return (T) JPAHelper.save(entity);
	}

	@Transactional(readOnly = true)
	public <T> List<T> getAllEntities(Class<T> clazz) {
		return JPAHelper.findAllEntities(clazz);
	}

	@Transactional
	public void deleteEntity(Object entity) {
		JPAHelper.delete(entity);
	}

	@Transactional
	public void initSysConfigParam() {
		List<ConfigParam> list = getAllEntities(ConfigParam.class);

		if (list.isEmpty()) {
			saveOrUpdate(new ConfigParam(App.SNANSHOT_PATH, "D:\\snapshot", "執行測試時,截圖存放位置"));
			saveOrUpdate(new ConfigParam(App.DEFAULT_WAIT_SEC, "10", "執行測試時,Action為wait的等待時間"));
			saveOrUpdate(new ConfigParam(App.IMPLICITLY_WAIT, "30", "執行測試時,取得元件的最大等待時間"));
			saveOrUpdate(new ConfigParam(App.INDEX, "http://210.13.77.92:7767/insurance/gs/sp/spLogin", "測試首頁"));
			saveOrUpdate(new ConfigParam(App.WEBDRIVER_IE_DRIVER, "classpath:IEDriverServer.exe", "IEDriverServer.exe所在路徑"));
			saveOrUpdate(new ConfigParam(App.WEBDRIVER_CHROME_DRIVER, "classpath:chromedriver.exe", "chromedriver.exe所在路徑"));
			saveOrUpdate(new ConfigParam(App.RETRY_TIMES, "2", "失敗時自動重試次數"));
		}
	}

	@Override
	public List<String> findAllPlanNames() {
		return JPAHelper.findEntities("select distinct c.name from TestPlan c", String.class);
	}

	@Override
	public TestPlan findTestPlan(String name) {
		return JPAHelper.findEntity("select c from TestPlan c where c.name=?1", TestPlan.class, name);
	}
	
	@Override
	@Transactional
	public void deleteTestPlan(String name) {
		 JPAHelper.delete(findTestPlan(name));
	}

}
