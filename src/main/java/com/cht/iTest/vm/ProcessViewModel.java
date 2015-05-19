package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.image.AImage;
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
import com.cht.iTest.def.DesktopResource;
import com.cht.iTest.def.Skip;
import com.cht.iTest.def.Status;
import com.cht.iTest.entity.TestCase;
import com.cht.iTest.entity.TestPlan;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.selenium.App;
import com.cht.iTest.util.ZKUtils;
import com.cht.iTest.vm.queue.TestStepExecuteAsyhcnronizer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ProcessViewModel implements Serializable {

	private static final long serialVersionUID = 5662850514701591440L;

	public static final String ZUL = "//process.zul";
	public static final String PARAM_TEST_PLAN = "testPlan";
	public static final String PARAM_DRIVER_TYPE = "driverType";
	public static final String PARAM_DRIVER_SIZE = "driverSize";
	public static final String PARAM_START_STEP = "startStep";
	public static final String PARAM_LAST_EXECVALS_CONTEXT = "PARAM_LAST_EXECVALS_CONTEXT";

	private App app;
	private TestStepExecuteAsyhcnronizer asyhcnronizer;
	private String driverSize;
	private String driverType;
	private int process = 0;
	private AImage image;
	private String errorMsg;
	private TestStep selected;
	private TestStep startStep;
	private Boolean run = Boolean.FALSE;
	private ListModelList<TestStep> queue = new ListModelList<TestStep>();
	private ListModelList<String[]> executeContextParam = new ListModelList<String[]>();
	private List<String> allExecVars = new ArrayList<String>();

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
		startStep = (TestStep) args.get(PARAM_START_STEP);

		Set<String> vars = new LinkedHashSet<String>();
		Set<String> getVars = new LinkedHashSet<String>();
		Map<String, String> replaceVars = new HashMap<String, String>();

		boolean isOk = (startStep == null);

		for (TestCase tc : testPlan.getTestCaseDetails()) {
			for (TestStep step : tc.getTestSteps()) {
				if (Skip.Y == step.getSkip()) {
					continue;
				}

				if (!isOk && step == startStep) {
					isOk = true;
				}

				if (isOk) {
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
		}

		vars.removeAll(getVars);

		@SuppressWarnings("unchecked")
		List<String[]> params = (List<String[]>) Executions.getCurrent().getDesktop().getAttribute(PARAM_LAST_EXECVALS_CONTEXT);

		if (startStep != null && params != null) {
			executeContextParam.addAll(params);
		} else {
			@SuppressWarnings("unchecked")
			Map<String, String> executeContext = (Map<String, String>) Executions.getCurrent().getDesktop().getAttribute("executeContext");

			for (String var : vars) {
				executeContextParam.add(new String[] { var, executeContext != null ? executeContext.get(var) : null });
			}

			List<String> temp = new ArrayList<String>(vars);

			if (startStep != null) {
				for (Entry<String, String> entry : replaceVars.entrySet()) {
					if (!vars.contains(entry.getKey())) {
						executeContextParam.add(new String[] { entry.getKey(), entry.getValue() });
					} else {
						executeContextParam.set(temp.indexOf(entry.getKey()), new String[] { entry.getKey(), entry.getValue() });
					}
				}
			}
		}

		allExecVars.addAll(vars);
	}

	public static void show(String driverType, String driverSize, TestPlan testPlan, TestStep startStep) {
		Window window = (Window) Executions.createComponents(ZUL, null, ZKUtils.argBuilder().put(PARAM_TEST_PLAN, testPlan).put(PARAM_START_STEP, startStep).build());
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

	@Listen("onProcessAsync=#processWin")
	public void processAsync(Event evt) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> executeContext = (Map<String, String>) evt.getData();

			for (TestStep step : queue) {
				step.setErrorMsg(null);
				step.setSnapshotImg(null);
				step.setExeStatus(Status.Ready);
			}

			run = true;
			Object obj = Executions.getCurrent().getDesktop().getAttribute(DesktopResource.SeleniumApp.name());

			if (obj != null) {
				app = (App) obj;
			} else {
				app = App.getInstance(driverType, driverSize);
				Executions.getCurrent().getDesktop().setAttribute(DesktopResource.SeleniumApp.name(), app);
			}

			app.setExecuteContext(executeContext).setSteps(queue).setProcess(0);

			if (startStep == null || !isExecuted()) {
				app.toIndex();
			}

			asyhcnronizer = new TestStepExecuteAsyhcnronizer("myqueue", "update", app);
			asyhcnronizer.startLongOperation();
		} catch (Exception ex) {
			Clients.clearBusy();
		}
	}

	private boolean isExecuted() {
		@SuppressWarnings("unchecked")
		List<String[]> params = (List<String[]>) Executions.getCurrent().getDesktop().getAttribute(PARAM_LAST_EXECVALS_CONTEXT);
		return CollectionUtils.isNotEmpty(params);
	}

	@Command
	public void exit() {
		if (process > 0) {
			Executions.getCurrent().getDesktop().setAttribute(PARAM_LAST_EXECVALS_CONTEXT, executeContextParam.getInnerList());
		}

		processWin.detach();
	}

	@Command
	@NotifyChange({ "run" })
	public void start() {
		Map<String, String> executeContext = new HashMap<String, String>();

		for (String[] args : executeContextParam) {
			if (StringUtils.isBlank(args[1])) {
				Messagebox.show(String.format("請輸入變數【%s】之值", args[0]));
				return;
			}

			executeContext.put(args[0], args[1]);
		}

		if (startStep == null || !isExecuted()) {
			Executions.getCurrent().getDesktop().setAttribute("executeContext", new HashMap<String, String>(executeContext));
		}

		Clients.showBusy("測試資料初始化... 請稍候...");
		Events.echoEvent("onProcessAsync", processWin, executeContext);
	}

	@Command
	public void start2() {
		Map<String, String> executeContext = new HashMap<String, String>();

		for (String[] args : executeContextParam) {
			if (StringUtils.isBlank(args[1])) {
				Messagebox.show(String.format("請輸入變數【%s】之值", args[0]));
				return;
			}

			executeContext.put(args[0], args[1]);
		}

		for (TestStep step : queue) {
			step.setErrorMsg(null);
			step.setSnapshotImg(null);
			step.setExeStatus(Status.Ready);
		}

		App.getInstance(driverType, driverSize).setExecuteContext(executeContext).setSteps(queue).toIndex().start().exit();
	}

	@Command
	@NotifyChange("run")
	public void stop() {
		run = false;
		app.stop();
	}

	@GlobalCommand("update")
	@NotifyChange({ "process", "run", "selected", "image", "errorMsg" })
	public void update(@BindingParam("process") Integer process, @BindingParam("execContextUpdateMap") Map<String, String> execContextUpdateMap) throws Exception {
		Clients.clearBusy();

		if (app.getTestStep() != null) {
			selected = app.getTestStep();
			viewSnapshot();
		}

		if (process != null) {
			this.process = process;
		}

		if (app.getIsStop()) {
			run = false;
		}

		if (MapUtils.isNotEmpty(execContextUpdateMap)) {
			int index = -1;

			for (Entry<String, String> entry : execContextUpdateMap.entrySet()) {
				index = allExecVars.indexOf(entry.getKey());

				if (index != -1) {
					executeContextParam.set(index, new String[] { entry.getKey(), entry.getValue() });
				} else {
					allExecVars.add(entry.getKey());
					executeContextParam.add(new String[] { entry.getKey(), entry.getValue() });
				}
			}
		}

	}

	@Command
	@NotifyChange({ "image", "errorMsg" })
	public void viewSnapshot() throws Exception {
		image = null;
		errorMsg = null;

		if (selected != null && selected.getSnapshotImg() != null) {
			image = new AImage("snapshot", selected.getSnapshotImg());
		}

		if (selected != null) {
			errorMsg = selected.getErrorMsg();
		}
	}

	public TestStep getSelected() {
		return selected;
	}

	public void setSelected(TestStep selected) {
		this.selected = selected;
	}

	public AImage getImage() {
		return image;
	}

	public void setImage(AImage image) {
		this.image = image;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getProcess() {
		return process;
	}

	public void setProcess(int process) {
		this.process = process;
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

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	public ListModelList<String[]> getExecuteContextParam() {
		return executeContextParam;
	}

	public void setExecuteContextParam(ListModelList<String[]> executeContextParam) {
		this.executeContextParam = executeContextParam;
	}

}
