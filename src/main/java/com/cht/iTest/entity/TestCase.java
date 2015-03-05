package com.cht.iTest.entity;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class TestCase implements Serializable, Cloneable {

	private static final long serialVersionUID = 7072039515440680617L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long Id;
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "testCase")
	@OrderBy("execOrder")
	private Set<TestCaseDetail> testCaseDetails = new LinkedHashSet<TestCaseDetail>();

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

}
