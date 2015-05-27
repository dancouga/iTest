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
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MinimizeEvent;
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
	public static final String PARAM_BEFORE_EXIT = "beforeExit";
	public static final String PARAM_INTERACT = "Interact";

	private TestPlan testPlan;
	private App app;
	private Boolean run = Boolean.FALSE;
	private Integer startFrom;
	private Boolean fail = Boolean.FALSE;
	private ListModelList<TestStep> queue = new ListModelList<TestStep>();
	private ListModelList<String[]> executeContextParam = new ListModelList<String[]>();
	private Interact interact;

	@Wire("#processWin")
	private Window processWin;

	public interface Interact {

		void toExit();

	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		Selectors.wireEventListeners(view, this);
	}

	@Init
	public void init() {
		Map<?, ?> args = Executions.getCurrent().getArg();
		testPlan = (TestPlan) args.get(PARAM_TEST_PLAN);
		interact = (Interact) args.get(PARAM_INTERACT);

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
			executeContextParam.add(new String[] { var, null });
		}
	}

	@Command
	public void exit() {
		if (app != null) {
			app.exit();
		}

		interact.toExit();
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

			this.startFrom = null;

			if (app.getTestStep() != null && app.getTestStep().getErrorMsg() != null) {
				this.fail = Boolean.TRUE;
			} else {
				this.fail = Boolean.FALSE;
			}

			ZKUtils.vmRefresh(EasyProcessViewModel.this, "fail", "queue", "executeContextParam");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Clients.clearBusy();
		}
	}

	@Listen("onContinueFromSelect=#processWin")
	public void continueFromSelect(Event evt) {
		try {
			app.continueFromFail(startFrom);
			executeContextParam.clear();

			for (Entry<String, String> entry : app.getExecuteContext().entrySet()) {
				executeContextParam.add(new String[] { entry.getKey(), entry.getValue() });
			}

			this.startFrom = null;

			if (app.getTestStep() != null && app.getTestStep().getErrorMsg() != null) {
				this.fail = Boolean.TRUE;
			} else {
				this.fail = Boolean.FALSE;
			}

			ZKUtils.vmRefresh(EasyProcessViewModel.this, "fail", "queue", "executeContextParam");
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

		app = App.getInstance(testPlan.getDriverType(), testPlan.getDriverSize()).setExecuteContext(executeContext).setSteps(queue);
		Clients.showBusy("測試中.... 請稍候!");
		Events.echoEvent("onProcessAsync", processWin, executeContext);
	}

	@Command
	public void startFromSelect() {
		Map<String, String> executeContext = new HashMap<String, String>();

		for (String[] args : executeContextParam) {
			if (StringUtils.isBlank(args[1])) {
				Messagebox.show(String.format("請輸入變數【%s】之值", args[0]));
				return;
			}

			executeContext.put(args[0], args[1]);
		}

		Clients.showBusy("測試中.... 請稍候!");
		Events.echoEvent("onContinueFromSelect", processWin, executeContext);
	}

	public static Window show(TestPlan testPlan, Interact interact) {
		Map<String, Object> param = ZKUtils.argBuilder().put(PARAM_INTERACT, interact).put(PARAM_TEST_PLAN, testPlan).build();

		Window window = (Window) Executions.createComponents(ZUL, null, param);
		window.setMode(Window.Mode.MODAL);
		window.setPosition("center");
		window.addEventListener(Events.ON_MINIMIZE, new EventListener<MinimizeEvent>() {
			@Override
			public void onEvent(MinimizeEvent evt) throws Exception {
				if (evt.isMinimized()) {
					window.doOverlapped();
					window.setVisible(false);
				}
			}
		});

		return window;
	}

	@Command
	@NotifyChange("startFrom")
	public void selectQueue() {
		int index = queue.isSelectionEmpty() ? -1 : queue.indexOf(queue.getSelection().iterator().next());
		this.setStartFrom(index);

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

	public Boolean getFail() {
		return fail;
	}

	public void setFail(Boolean fail) {
		this.fail = fail;
	}

	public Integer getStartFrom() {
		return startFrom;
	}

	public void setStartFrom(Integer startFrom) {
		this.startFrom = startFrom;
	}

}
