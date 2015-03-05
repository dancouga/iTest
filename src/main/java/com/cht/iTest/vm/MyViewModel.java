package com.cht.iTest.vm;

import java.io.Serializable;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

import com.cht.iTest.entity.Log;
import com.cht.iTest.service.MyService;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class MyViewModel  implements Serializable {

	private static final long serialVersionUID = -7225185726560340400L;
	
	@WireVariable
	private MyService myService;
	private ListModelList<Log> logListModel;
	private String message;

	@Init
	public void init() {
		logListModel = new ListModelList<Log>(myService.getLogs());
	}

	public ListModel<Log> getLogListModel() {
		return logListModel;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Command
	public void addLog() {
		if (Strings.isBlank(message)) {
			Clients.showNotification("Please enter Message.");
			return;
		}
		
		Log log = new Log(message);
		log = myService.addLog(log);
		logListModel.add(log);
	}

	@Command
	public void deleteLog(@BindingParam("log") Log log) {
		myService.deleteLog(log);
		logListModel.remove(log);
	}

}
