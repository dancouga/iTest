package com.cht.iTest.entity;

import java.util.List;

import javax.persistence.Transient;

/**
 * 
 * 測試節點介面
 * 
 * @author wen
 *
 */
public interface TestNode {

	Long getId();

	void setId(Long id);

	String getName();

	void setName(String name);

	List<? extends TestNode> getChildren();

	Integer getExecOrder();

	void setExecOrder(Integer execOrder);

	@Transient
	String getDraggable();

	@Transient
	String getDroppable();

	@Transient
	TestNode getParent();

	<T extends TestNode> void setParent(T parent);

}
