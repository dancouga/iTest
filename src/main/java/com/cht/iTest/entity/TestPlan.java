package com.cht.iTest.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

@Entity
public class TestPlan implements TestNode, Serializable, Cloneable {

	private static final long serialVersionUID = -8943614854852247714L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long Id;
	@Column(unique = true)
	private String name;
	private Integer execOrder = Integer.valueOf(0);
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "testPlan", fetch = FetchType.EAGER, orphanRemoval = true)
	@OrderBy("execOrder")
	private List<TestCase> testCaseDetails = new ArrayList<TestCase>();

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TestCase> getTestCaseDetails() {
		return testCaseDetails;
	}

	public void setTestCaseDetails(List<TestCase> testCaseDetails) {
		this.testCaseDetails = testCaseDetails;
	}

	@Override
	public Integer getExecOrder() {
		return execOrder;
	}

	@Override
	public void setExecOrder(Integer execOrder) {
		this.execOrder = execOrder;
	}

	@Override
	@Transient
	public List<TestCase> getChildren() {
		return testCaseDetails;
	}
}
