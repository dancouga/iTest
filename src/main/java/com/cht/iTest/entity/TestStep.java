package com.cht.iTest.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.cht.iTest.def.Action;
import com.cht.iTest.def.FindMethod;
import com.cht.iTest.def.Skip;
import com.cht.iTest.def.Snapshot;
import com.cht.iTest.def.Status;
import com.cht.iTest.def.Sync;

/**
 * 
 * 測試步驟
 * 
 * @author wen
 *
 */
@Entity
public class TestStep implements TestNode, Serializable, Cloneable {

	private static final long serialVersionUID = -1642197244518067803L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long Id;

	private String name;
	private Sync sync = Sync.Y;
	private Snapshot snapshot = Snapshot.N;
	private Action action = Action.input;
	private FindMethod findMethod = FindMethod.id;
	private String inputValue;
	private String element;
	private String description;
	private Integer execOrder;

	@ManyToOne
	private TestCase testCase;

	@Transient
	private Skip skip = Skip.N;
	@Transient
	private byte[] snapshotImg;
	@Transient
	private String errorMsg;
	@Transient
	private Status exeStatus = Status.Ready;

	public Status getExeStatus() {
		return exeStatus;
	}

	public void setExeStatus(Status exeStatus) {
		this.exeStatus = exeStatus;
	}

	public byte[] getSnapshotImg() {
		return snapshotImg;
	}

	public void setSnapshotImg(byte[] snapshotImg) {
		this.snapshotImg = snapshotImg;
	}

	public Integer getExecOrder() {
		return execOrder;
	}

	public void setExecOrder(Integer execOrder) {
		this.execOrder = execOrder;
	}

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

	public Skip getSkip() {
		return skip;
	}

	public void setSkip(Skip skip) {
		this.skip = skip;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

	@Override
	public List<? extends TestNode> getChildren() {
		return null;
	}

	@Transient
	@Override
	public String getDraggable() {
		return "TestStep";
	}

	@Transient
	@Override
	public String getDroppable() {
		return "TestStep";
	}

	@Transient
	@Override
	public TestNode getParent() {
		return testCase;
	}

	@Override
	public void setParent(TestNode parent) {
		this.testCase = (TestCase) parent;
	}

}
