package com.cht.iTest.service;

import java.util.List;

import com.cht.iTest.entity.TestPlan;

/**
 * 
 * iTest常用服務介面
 * 
 * @author wen
 *
 */
public interface CommonService {

	/**
	 * JPA Entity儲存或更新
	 * 
	 * @param entity
	 * @return
	 */
	public <T> T saveOrUpdate(Object entity);

	/**
	 * 取得特定JPA類別所有實體
	 * 
	 * @param clazz
	 * @return
	 */
	public <T> List<T> getAllEntities(Class<T> clazz);

	/**
	 * 刪除特定JPA實體
	 * 
	 * @param entity
	 */
	public void deleteEntity(Object entity);

	/**
	 * 
	 * 初始化系統變數
	 * 
	 */
	public void initSysConfigParam();
	
	/**
	 * 
	 * 取得所有TestPlan名稱
	 * 
	 * @return
	 */
	public List<String> findAllPlanNames();

	/**
	 * 
	 * 依計畫名稱取得TestPlan Entity
	 * 
	 * @param name
	 * @return
	 */
	TestPlan findTestPlan(String name);

	/**
	 * 
	 * 依計畫名稱刪除TestPlan Entity
	 * 
	 * @param name
	 */
	void deleteTestPlan(String name);

}
