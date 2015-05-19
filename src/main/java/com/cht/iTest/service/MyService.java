package com.cht.iTest.service;

import java.util.List;

import com.cht.iTest.entity.TestPlan;

public interface MyService {

	public <T> T saveOrUpdate(Object entity);

	public <T> List<T> getAllEntities(Class<T> clazz);

	public void deleteEntity(Object entity);

	public void initSysConfigParam();
	
	public List<String> findAllPlanNames();

	TestPlan findTestPlan(String name);

	void deleteTestPlan(String name);

}
