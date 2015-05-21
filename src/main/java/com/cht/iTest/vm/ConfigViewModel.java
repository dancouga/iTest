package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.cht.iTest.entity.ConfigParam;
import com.cht.iTest.service.CommonService;
import com.cht.iTest.util.Cache;
import com.cht.iTest.util.ExtractUtils;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ConfigViewModel implements Serializable {

	private static final long serialVersionUID = -1937240710782994376L;
	public static final String ZUL = "//config.zul";

	@WireVariable
	private CommonService myService;
	
	private ListModelList<ConfigParam> listModel = new ListModelList<ConfigParam>();
	private List<ConfigParam> deletes = new LinkedList<ConfigParam>();
	private String name;
	private String value;
	private String description;

	public static void show() {
		Window window = (Window) Executions.createComponents(ZUL, null, null);
		window.doModal();
	}

	@Init
	public void init() {
		Cache.getSysCategory();
		listModel.addAll(myService.getAllEntities(ConfigParam.class));
	}

	@Command
	public void save(@BindingParam("win") Window win) {
		for (ConfigParam param : listModel) {
			myService.saveOrUpdate(param);
		}

		for (ConfigParam param : deletes) {
			if (param.getId() != null) {
				myService.deleteEntity(param);
			}
		}

		Cache.refreshSysCategory();
		win.detach();
	}

	@Command
	public void add() {
		ConfigParam param = new ConfigParam();

		if (StringUtils.isBlank(name) || StringUtils.isBlank(description)) {
			Clients.showNotification("Name、Description can not be empty.");
			return;
		}

		Set<String> names = ExtractUtils.extract2Set(listModel, "name", String.class);

		if (names.contains(name)) {
			Messagebox.show("【" + name + "】 already exists. Please Confirm Aagin.");
			return;
		}

		param.setName(name);
		param.setValue(value);
		param.setDescription(description);
		listModel.add(param);
	}

	@Command
	public void delete(@BindingParam("param") ConfigParam param) {
		listModel.remove(param);
		deletes.add(param);
	}

	public ListModelList<ConfigParam> getListModel() {
		return listModel;
	}

	public void setListModel(ListModelList<ConfigParam> listModel) {
		this.listModel = listModel;
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
