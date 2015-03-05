package com.cht.iTest.vm;

import java.io.Serializable;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import com.cht.iTest.entity.TestStep;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class TestStepViewModel implements Serializable {

	private static final long serialVersionUID = -7042665557139145209L;

	private ListModelList<TestStep> stepListModel;
	private String name;
	private String action;
	private String inputVal;
	private String description;
	
	@Init
	public void init() {
		stepListModel = new ListModelList<TestStep>();
	}

	@Command
	public void addTestStep() {
		if (Strings.isBlank(name)||Strings.isBlank(action)||Strings.isBlank(inputVal)) {
			Clients.alert("Please enter name、Action、inputVal");
			return;
		}

		TestStep step = new TestStep();
		// log = myService.addLog(log);
		stepListModel.add(step);
	}

	@Command
	public void deleteTestStep(@BindingParam("step") TestStep step) {
		// myService.deleteLog(log);
		stepListModel.remove(step);
	}

	public ListModelList<TestStep> getStepListModel() {
		return stepListModel;
	}

	public void setStepListModel(ListModelList<TestStep> stepListModel) {
		this.stepListModel = stepListModel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getInputVal() {
		return inputVal;
	}

	public void setInputVal(String inputVal) {
		this.inputVal = inputVal;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
