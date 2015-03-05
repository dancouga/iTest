package com.cht.iTest.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.cht.iTest.def.Action;
import com.cht.iTest.def.FindMethod;
import com.cht.iTest.def.Snapshot;
import com.cht.iTest.def.Sync;



@Entity
public class TestStep implements Serializable, Cloneable {

	private static final long serialVersionUID = -1642197244518067803L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long Id;

	private String name;
	private Sync sync = Sync.Y;
	private Snapshot snapshot = Snapshot.N;
	private Action action;
	private FindMethod findMethod;
	private String inputValue;
	private String element;
	private String description;
	private String caseName;
	private Integer execOrder;

	@Transient
	private byte[] snapshotImg;

	public byte[] getSnapshotImg() {
		return snapshotImg;
	}

	public void setSnapshotImg(byte[] snapshotImg) {
		this.snapshotImg = snapshotImg;
	}

	public String getCaseName() {
		return caseName;
	}

	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}

	public Integer getExecOrder() {
		return execOrder;
	}

	public void setExecOrder(Integer execOrder) {
		this.execOrder = execOrder;
	}

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

	public Sync getSync() {
		return sync;
	}

	public void setSync(Sync sync) {
		this.sync = sync;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public FindMethod getFindMethod() {
		return findMethod;
	}

	public void setFindMethod(FindMethod findMethod) {
		this.findMethod = findMethod;
	}

	public String getInputValue() {
		return inputValue;
	}

	public void setInputValue(String inputValue) {
		this.inputValue = inputValue;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
