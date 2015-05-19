package com.cht.iTest.entity;

import java.util.List;

public interface TestNode {

	String getName();

	void setName(String name);
	
	List<? extends TestNode> getChildren();

	Integer getExecOrder();

	void setExecOrder(Integer execOrder);

}
