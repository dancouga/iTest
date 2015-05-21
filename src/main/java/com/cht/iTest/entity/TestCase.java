package com.cht.iTest.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;


/**
 * 
 * 測試案件
 * 
 * @author wen
 *
 */
@Entity
public class TestCase implements TestNode, Serializable, Cloneable {

	private static final long serialVersionUID = 7072039515440680617L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long Id;
	private String name;
	private Integer execOrder = Integer.valueOf(0);

	@ManyToOne
	private TestPlan testPlan;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "testCase", fetch = FetchType.EAGER, orphanRemoval = true)
	@OrderBy("execOrder")
	private List<TestStep> testSteps = new ArrayList<TestStep>();

	@Override
	public Long getId() {
		return Id;
	}

	@Override
	public void setId(Long id) {
		Id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getExecOrder() {
		return execOrder;
	}

	public void setExecOrder(Integer execOrder) {
		this.execOrder = execOrder;
	}

	public TestPlan getTestPlan() {
		return testPlan;
	}

	public void setTestPlan(TestPlan testPlan) {
		this.testPlan = testPlan;
	}

	public List<TestStep> getTestSteps() {
		return testSteps;
	}

	public void setTestSteps(List<TestStep> testSteps) {
		this.testSteps = testSteps;
	}

	@Override
	public List<TestStep> getChildren() {
		return testSteps;
	}

	@Transient
	@Override
	public String getDraggable() {
		return "TestCase";
	}

	@Transient
	@Override
	public String getDroppable() {
		return "TestCase,TestStep";
	}

	@Transient
	@Override
	public TestNode getParent() {
		return testPlan;
	}

	@Override
	public void setParent(TestNode parent) {
		this.testPlan=(TestPlan) parent;
	}

}
