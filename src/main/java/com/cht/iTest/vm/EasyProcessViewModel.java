package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MaximizeEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.cht.iTest.def.Action;
import com.cht.iTest.def.Skip;
import com.cht.iTest.def.Status;
import com.cht.iTest.entity.TestCase;
import com.cht.iTest.entity.TestPlan;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.selenium.App;
import com.cht.iTest.util.ZKUtils;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EasyProcessViewModel implements Serializable {

	private static final long serialVersionUID = 5662850514701591440L;

	public static final String ZUL = "//easyProcess.zul";
	public static final String PARAM_TEST_PLAN = "testPlan";
	public static final String PARAM_DRIVER_TYPE = "driverType";
	public static final String PARAM_DRIVER_SIZE = "driverSize";

	private String driverSize;
	private String driverType;
	private App app;
	private Boolean run = Boolean.FALSE;
	private ListModelList<TestStep> queue = new ListModelList<TestStep>();
	private ListModelList<String[]> executeContextParam = new ListModelList<String[]>();

	@Wire("#processWin")
	private Window processWin;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		Selectors.wireEventListeners(view, this);
	}

	@Init
	public void init() {
		Map<?, ?> args = Executions.getCurrent().getArg();
		TestPlan testPlan = (TestPlan) args.get(PARAM_TEST_PLAN);
		driverType = (String) args.get(PARAM_DRIVER_TYPE);
		driverSize = (String) args.get(PARAM_DRIVER_SIZE);
		Set<String> vars = new LinkedHashSet<String>();
		Set<String> getVars = new LinkedHashSet<String>();
		Map<String, String> replaceVars = new HashMap<String, String>();

		for (TestCase tc : testPlan.getTestCaseDetails()) {
			for (TestStep step : tc.getTestSteps()) {
				if (Skip.Y == step.getSkip()) {
					continue;
				}

				step.setErrorMsg(null);
				step.setSnapshotImg(null);
				step.setExeStatus(Status.Ready);
				queue.add(step);

				if (StringUtils.isBlank(step.getInputValue())) {
					continue;
				}

				if (step.getAction() == Action.get || step.getAction() == Action.get_title) {
					getVars.addAll(App.getVarsFromContent(step.getInputValue()));
					continue;
				} else if (step.getAction() == Action.replace) {
					Set<String> set = (App.getVarsFromContent(step.getElement()));

					if (!set.isEmpty()) {
						replaceVars.put(set.iterator().next(), step.getInputValue());
					}

					continue;
				}

				vars.addAll(App.getVarsFromContent(step.getInputValue()));
			}
		}

		vars.removeAll(getVars);
		for (String var : vars) {
			executeContextParam.add(new String[] { var,  null });
		}
	}

	@Command
	public void exit() {
		if (app != null) {
			app.exit();
		}

		processWin.detach();
	}

	@Listen("onProcessAsync=#processWin")
	public void processAsync(Event evt) {
		try {
			app.toIndex().start();
			executeContextParam.clear();

			for (Entry<String, String> entry : app.getExecuteContext().entrySet()) {
				executeContextParam.add(new String[] { entry.getKey(), entry.getValue() });
			}

			ZKUtils.vmRefresh(EasyProcessViewModel.this, "queue", "executeContextParam");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Clients.clearBusy();
		}
	}

	@Command
	public void start() {
		Map<String, String> executeContext = new HashMap<String, String>();

		for (String[] args : executeContextParam) {
			if (StringUtils.isBlank(args[1])) {
				Messagebox.show(String.format("請輸入變數【%s】之值", args[0]));
				return;
			}

			executeContext.put(args[0], args[1]);
		}
		
		if (app != null) {
			app.exit();
		}
		
		app = App.getInstance(driverType, driverSize).setExecuteContext(executeContext).setSteps(queue);
		Clients.showBusy("測試中.... 請稍候!");
		Events.echoEvent("onProcessAsync", processWin, executeContext);
	}

	public static void show(String driverType, String driverSize, TestPlan testPlan) {
		Map<String, Object> param = ZKUtils.argBuilder().put(PARAM_TEST_PLAN, testPlan).put(PARAM_DRIVER_TYPE, driverType).put(PARAM_DRIVER_SIZE, driverSize).build();
		
		Window window = (Window) Executions.createComponents(ZUL, null, param);
		window.addEventListener(Events.ON_MAXIMIZE, new EventListener<MaximizeEvent>() {
			@Override
			public void onEvent(MaximizeEvent evt) throws Exception {
				if (StringUtils.isBlank(evt.getHeight())) {
					window.setHeight(window.getMinheight() + "px");
					window.setWidth(window.getMinwidth() + "px");
				}
			}
		});
	}

	public Boolean getRun() {
		return run;
	}

	public void setRun(Boolean run) {
		this.run = run;
	}

	public ListModelList<TestStep> getQueue() {
		return queue;
	}

	public void setQueue(ListModelList<TestStep> queue) {
		this.queue = queue;
	}

	public ListModelList<String[]> getExecuteContextParam() {
		return executeContextParam;
	}

	public void setExecuteContextParam(ListModelList<String[]> executeContextParam) {
		this.executeContextParam = executeContextParam;
	}

}
