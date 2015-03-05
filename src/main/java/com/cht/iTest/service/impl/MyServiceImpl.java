package com.cht.iTest.service.impl;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cht.iTest.entity.Log;
import com.cht.iTest.service.MyService;
import com.cht.iTest.util.JPAHelper;

@Service("myService")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyServiceImpl implements MyService {

	@Transactional
	public Log addLog(Log log) {
		return JPAHelper.save(log);
	}

	@Transactional(readOnly = true)
	public List<Log> getLogs() {
		return JPAHelper.findAllEntities(Log.class);
	}

	@Transactional
	public void deleteLog(Log log) {
		JPAHelper.delete(log);
	}

}
