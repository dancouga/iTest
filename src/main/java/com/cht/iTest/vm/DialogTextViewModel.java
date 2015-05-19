package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Window;

import com.cht.iTest.util.ZKUtils;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DialogTextViewModel implements Serializable {

	private static final long serialVersionUID = 6431206703329439970L;

	public static final String ZUL = "//dialogText.zul";
	private static final String PARAM_ACT = "Action";
	private String value;
	private String title;
	private Action action;

	public interface Action {

		void ok(String value);

		String title();

		String value();

	}

	@Init
	public void init() {
		action = (Action) Executions.getCurrent().getArg().get(PARAM_ACT);

		if (action != null) {
			this.title = action.title();
			this.value = action.value();
		}
	}

	@Command
	public void confirm(@BindingParam("dialogWin") Window dialogWin) {
		if (action != null) {
			action.ok(value);
		}

		dialogWin.detach();
	}

	public static void show(Action action) {
		Map<String, Object> params = ZKUtils.argBuilder().put(PARAM_ACT, action).build();
		Window window = (Window) Executions.getCurrent().createComponents(ZUL, null, params);
		window.doModal();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
