package com.cht.iTest.service;

import com.cht.iTest.entity.Log;

import java.util.List;

public interface MyService {

	Log addLog(Log log);

	List<Log> getLogs();

	void deleteLog(Log log);
}
