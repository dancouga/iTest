package com.cht.iTest.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 
 * 系統變數
 * 
 * @author wen
 *
 */
@Entity
public class ConfigParam implements Serializable, Cloneable {

	private static final long serialVersionUID = -7544422839749448121L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	private String value;
	private String description;

	public ConfigParam() {

	}

	public ConfigParam(String name, String value, String description) {
		this.name = name;
		this.value = value;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
