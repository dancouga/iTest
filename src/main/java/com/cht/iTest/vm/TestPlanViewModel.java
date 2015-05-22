package com.cht.iTest.vm;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.theme.Themes;

import com.cht.iTest.entity.TestCase;
import com.cht.iTest.entity.TestNode;
import com.cht.iTest.entity.TestPlan;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.initializer.DesktopResourceCleanup;
import com.cht.iTest.selenium.App;
import com.cht.iTest.service.CommonService;
import com.cht.iTest.util.Cache;
import com.cht.iTest.util.ExtractUtils;
import com.cht.iTest.util.POIUtils;
import com.cht.iTest.util.SpringUtils;
import com.cht.iTest.util.ZKUtils;
import com.cht.iTest.util.ZKUtils.MessageAction;
import com.cht.iTest.vm.CopyFromViewModel.Confirm;
import com.cht.iTest.vm.support.TestPlanExcelParser;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class TestPlanViewModel implements Serializable {

	private static final long serialVersionUID = -3975437306120012044L;
	private static final String EMBEDDED_ZUL = "/embeddedDB.zul";

	@WireVariable
	private CommonService myService;
	@WireVariable
	private TestPlanExcelParser testPlanExcelParser;

	@Wire("#startFromMe")
	private Menupopup startFromMe;
	@Wire("#tabs")
	private Tabs tabs;

	private String driverType = "Internet Explorer";
	private String driverSize = "FullScreen";
	private TestPlan testPlan;
	private TestCase tabName;
	private TestStep startStep;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		Selectors.wireEventListeners(view, this);
	}

	@Init
	public void init() {
		Cache.getSysCategory();
	}

	@Listen("onOpen=#startFromMe")
	public void prepareStartFromMe(OpenEvent evt) {
		if (evt.isOpen()) {
			int index = ((Listitem) evt.getReference()).getIndex();
			startStep = this.tabName.getTestSteps().get(index);
		} else {
			startStep = null;
		}
	}

	@Command
	@NotifyChange("driverType")
	public void selectDriverType(@BindingParam("type") String type) {
		driverType = type;
	}

	@Command
	@NotifyChange("driverSize")
	public void selectDriverSize(@BindingParam("size") String size) {
		driverSize = size;
	}

	@Command
	public void linkToH2() {
		Executions.getCurrent().createComponents(EMBEDDED_ZUL, null, null);
	}

	@Command
	public void selectPlan() {
		SelectTestPlanViewModel.show(new SelectTestPlanViewModel.Action() {
			@Override
			public void ok(TestPlan testPlan) {
				TestPlanViewModel.this.testPlan = testPlan;
				TestPlanViewModel.this.tabName = testPlan.getTestCaseDetails().get(0);
				ZKUtils.vmRefresh(TestPlanViewModel.this, "testPlan");
				tabs.invalidate();
			}
		});
	}

	@Command
	public void themeSwitch(@BindingParam("type") String themeName) {
		Themes.setTheme(Executions.getCurrent(), themeName);
		Executions.sendRedirect(null);
	}

	@Command
	@NotifyChange("testPlan")
	public void saveTestPlan() {
		int index = 0;

		if (tabName != null) {
			index = testPlan.getTestCaseDetails().indexOf(tabName);
		}

		if (testPlan.getId() == null) {
			if (myService.findAllPlanNames().contains(testPlan.getName())) {
				Clients.showNotification("計畫名稱【" + testPlan.getName() + "】已存在, 請修正當前計畫名稱!", true);
				return;
			}
		}

		testPlan = myService.saveOrUpdate(testPlan);
		ListModelList<TestStep> lml = null;

		for (TestCase tc : testPlan.getTestCaseDetails()) {
			lml = new ListModelList<TestStep>(tc.getTestSteps());
			lml.setMultiple(true);
			tc.setTestSteps(lml);
		}

		tabName = testPlan.getTestCaseDetails().get(index);
		Clients.showNotification("儲存成功!", true);
	}

	@Command
	public void modifyTestPlan(@BindingParam("modifyType") String modifyType) {
		DialogTextViewModel.show(new DialogTextViewModel.Action() {
			@Override
			public void ok(String value) {
				if ("create".equals(modifyType)) {
					testPlan = new TestPlan();
					TestCase tc = new TestCase();
					List<TestCase> list = testPlan.getTestCaseDetails();
					tc.setName("TestCase" + (list.size() + 1));
					tc.setTestPlan(testPlan);
					tc.setExecOrder(0);
					list.add(tc);
					ListModelList<TestStep> lml = new ListModelList<TestStep>();
					lml.setMultiple(true);
					tc.setTestSteps(lml);
					tabName = tc;
				}

				testPlan.setName(value);
				ZKUtils.vmRefresh(TestPlanViewModel.this, "testPlan");
			}

			@Override
			public String title() {
				return "請輸入【測試計畫】名稱,不得為空";
			}

			@Override
			public String value() {
				return "create".equals(modifyType) ? null : testPlan.getName();
			}
		});
	}

	@Command
	public void createTestStep() {
		DialogTextViewModel.show(new DialogTextViewModel.Action() {
			@Override
			public void ok(String value) {
				ListModelList<TestStep> lml = (ListModelList<TestStep>) tabName.getTestSteps();
				TestStep step = new TestStep();
				step.setName(value);
				step.setTestCase(tabName);
				step.setExecOrder(lml.size());
				lml.add(step);
			}

			@Override
			public String title() {
				return "請輸入【測試步驟】名稱, 不得為空";
			}

			@Override
			public String value() {
				return null;
			}
		});
	}

	@Command
	public void saveAsNewOne() {
		DialogTextViewModel.show(new DialogTextViewModel.Action() {
			@Override
			public void ok(String value) {
				int index = 0;

				if (tabName != null) {
					index = testPlan.getTestCaseDetails().indexOf(tabName);
				}

				if (myService.findAllPlanNames().contains(value)) {
					throw new WrongValueException("計畫名稱【" + testPlan.getName() + "】已存在, 請修正當前計畫名稱!");
				}

				testPlan.setName(value);
				testPlan.setId(null);

				for (TestCase tempcase : testPlan.getTestCaseDetails()) {
					tempcase.setId(null);

					for (TestStep step : tempcase.getTestSteps()) {
						step.setId(null);
					}
				}

				testPlan = myService.saveOrUpdate(testPlan);
				ListModelList<TestStep> lml = null;

				for (TestCase tc : testPlan.getTestCaseDetails()) {
					lml = new ListModelList<TestStep>(tc.getTestSteps());
					lml.setMultiple(true);
					tc.setTestSteps(lml);
				}

				tabName = testPlan.getTestCaseDetails().get(index);
				ZKUtils.vmRefresh(TestPlanViewModel.this, "testPlan");
				Clients.showNotification("另存新檔成功!", true);
			}

			@Override
			public String title() {
				return "請輸入 新的測試計畫名稱, 不得重複";
			}

			@Override
			public String value() {
				return null;
			}
		});
	}

	@Command
	public void deleteTestSteps() {
		ListModelList<TestStep> current = (ListModelList<TestStep>) tabName.getTestSteps();
		Set<TestStep> steps = current.getSelection();

		if (steps.isEmpty()) {
			Clients.showNotification("請勾選欲刪除之測試步驟", Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 3000);
			return;
		}

		current.removeAll(current.getSelection());
		refreshExecOrder(current);
	}

	@Command
	public void tabName(@BindingParam("testCase") TestCase testCase) {
		this.tabName = testCase;
	}

	@Command
	public void modifyTestCase(@BindingParam("modifyType") String modifyType) {
		DialogTextViewModel.show(new DialogTextViewModel.Action() {
			@Override
			public void ok(String value) {
				if ("create".equals(modifyType)) {
					List<TestCase> caseNames = testPlan.getTestCaseDetails();

					if (ExtractUtils.extract2List(caseNames, "name", String.class).contains(value)) {
						throw new WrongValueException("同一測試計畫中，【測試案例】名稱不可重複!");
					}

					tabName = new TestCase();
					tabName.setExecOrder(caseNames.size());
					tabName.setTestPlan(testPlan);
					caseNames.add(tabName);
					ListModelList<TestStep> lml = new ListModelList<TestStep>();
					lml.setMultiple(true);
					tabName.setTestSteps(lml);
				}

				tabName.setName(value);
				ZKUtils.vmRefresh(TestPlanViewModel.this, "testPlan");
			}

			@Override
			public String title() {
				return "請輸入【測試案例】名稱, 不得為空";
			}

			@Override
			public String value() {
				return "create".equals(modifyType) ? null : tabName.getName();
			}
		});
	}

	@Command
	public void parameterSetting() {
		ConfigViewModel.show();
	}

	@Command
	public void copyFrom() {
		CopyFromViewModel.show(testPlan, new Confirm() {
			@Override
			public void ok(TestPlan plan) {
				TestPlanViewModel.this.testPlan = plan;
				TestPlanViewModel.this.tabName = plan.getTestCaseDetails().get(0);
				ZKUtils.vmRefresh(TestPlanViewModel.this, "testPlan");
				tabs.invalidate();
			}
		});
	}

	@Command
	public void uploadTestCase() throws Exception {
		if (testPlan != null) {
			ZKUtils.msgYN("這項操作會清除當前頁面上所有質料，並顯示您的上傳結果，是否執行?", new MessageAction() {
				@Override
				public void doIfYes() {
					importCase();
				}
			});
		} else {
			importCase();
		}
	}

	@Command
	public void clearAllCase() {
		ZKUtils.msgYN("這項操作會清除當前頁面上所有資料，是否執行?", new MessageAction() {
			@Override
			public void doIfYes() {
				DesktopResourceCleanup.destroySeleniumApp(Executions.getCurrent().getDesktop());
				Executions.getCurrent().sendRedirect(null);
			}
		});
	}

	@Command
	public void exampleImport() throws Exception {
		if (testPlan != null) {
			ZKUtils.msgYN("這項操作會清除當前頁面上所有資料，並顯示測試範例，是否執行?", new MessageAction() {
				@Override
				public void doIfYes() throws Exception {
					parserXlsx(SpringUtils.getResource("classpath:testPlan.xls").getInputStream(), "testPlan");
				}
			});
		} else {
			parserXlsx(SpringUtils.getResource("classpath:testPlan.xls").getInputStream(), "testPlan");
		}
	}

	@Command
	public void exportTestPlan() throws IOException {
		Map<String, List<TestStep>> datas = new LinkedHashMap<String, List<TestStep>>();

		for (TestCase tabName : testPlan.getTestCaseDetails()) {
			datas.put(tabName.getName(), tabName.getTestSteps());
		}

		byte[] content = POIUtils.toXlsByteAry(datas, testPlanExcelParser);
		Filedownload.save(content, POIUtils.CONTENT_TYPE, testPlan.getName() + ".xls");
	}

	@Command
	public void reomveCase(@BindingParam("index") int index, @BindingParam("evt") Event evt) {
		evt.stopPropagation();
		ZKUtils.msgYN("這項操作會清除頁籤【" + testPlan.getTestCaseDetails().get(index).getName() + "】上所有資料，是否執行?", new MessageAction() {
			@Override
			public void doIfYes() throws Exception {
				List<TestCase> tcs = testPlan.getTestCaseDetails();
				tcs.remove(index);

				if (tcs.isEmpty()) {
					testPlan = null;
					tabName = null;
				} else {
					refreshExecOrder(tcs);
					tabName = tcs.get(0);
				}

				ZKUtils.vmRefresh(TestPlanViewModel.this, "testPlan", "tabName");
			}
		});
	}

	@Command
	public void execute() throws Exception {
		if (StringUtils.isBlank(Cache.getSysCateVal(App.INDEX)) || StringUtils.isBlank(Cache.getSysCateVal(App.SNANSHOT_PATH))) {
			Messagebox.show("請至【Config】/【 Parameter Setting】設定參數(index、snapshot path)之值");
			return;
		}

		ProcessViewModel.show(driverType, driverSize, testPlan, startStep);
	}

	@Command
	@NotifyChange({ "testPlan" })
	public void changeCaseExecSeq(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent dropEvt, @BindingParam("index") int index) {
		Component drag = dropEvt.getDragged();
		List<TestCase> caseNames = testPlan.getTestCaseDetails();

		if (drag instanceof Tab) {
			Component drop = dropEvt.getTarget();
			Component parent = drop.getParent();
			int dragPos = parent.getChildren().indexOf(drag);
			int dropPos = parent.getChildren().indexOf(drop);
			caseNames.add(dropPos, caseNames.remove(dragPos));
			refreshExecOrder(caseNames);
		} else {
			ListModelList<TestStep> lmml = (ListModelList<TestStep>) tabName.getTestSteps();
			tabName = caseNames.get(index);
			List<TestStep> dropSteps = tabName.getTestSteps();

			if (dropSteps.containsAll(lmml)) {
				return;
			}

			List<TestStep> dragSteps = new ArrayList<TestStep>(lmml.getSelection());
			lmml.removeAll(dragSteps);
			refreshExecOrder(lmml);
			int start = dropSteps.size();
			dropSteps.addAll(start, dragSteps);

			for (int startIdx = start; startIdx < dropSteps.size(); startIdx++) {
				TestStep testStep = dropSteps.get(startIdx);
				testStep.setExecOrder(startIdx);
				testStep.setTestCase(tabName);
				dropSteps.set(startIdx, testStep);
			}
		}
	}

	@Command
	public void changeStepExecSeq(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent drop, @BindingParam("dropObj") TestStep dropObj) {
		Component parent = drop.getDragged().getParent();
		int dragIndex = (parent.getChildren().indexOf(drop.getDragged()) - 1);
		ListModelList<TestStep> current = (ListModelList<TestStep>) tabName.getTestSteps();
		Set<TestStep> set = current.getSelection();

		if (set.isEmpty()) {
			current.addToSelection(current.get(dragIndex));
			set = current.getSelection();
		}

		if (set.contains(dropObj)) {
			return;
		} else {
			List<TestStep> selects = new ArrayList<TestStep>(set);
			current.removeAll(set);
			int dropIdx = current.indexOf(dropObj);
			current.addAll(dropIdx, selects);
			refreshExecOrder(current);
		}
	}

	private <T extends TestNode> void refreshExecOrder(List<T> model) {
		int pos = 0;
		List<T> innerList = new ArrayList<T>(model);

		for (T node : innerList) {
			node.setExecOrder(pos);
			model.set(pos++, node);
		}
	}

	private void parserXlsx(InputStream xlsIs, String testPlanName) throws Exception {
		Map<String, List<TestStep>> testCaseMap = POIUtils.toBeanMap(xlsIs, testPlanExcelParser);
		testPlan = new TestPlan();
		testPlan.setName(testPlanName);
		List<TestCase> caseNames = testPlan.getTestCaseDetails();
		TestCase tc = null;
		ListModelList<TestStep> lml = null;
		int tcIdx = 0;

		for (Entry<String, List<TestStep>> entry : testCaseMap.entrySet()) {
			tc = new TestCase();
			tc.setName(entry.getKey());
			tc.setTestPlan(testPlan);
			tc.setExecOrder(tcIdx++);
			lml = new ListModelList<TestStep>();
			lml.setMultiple(true);
			int index = 0;

			for (TestStep step : entry.getValue()) {
				step.setTestCase(tc);
				step.setExecOrder(index++);
				tc.getTestSteps().add(step);
				lml.add(step);
			}

			caseNames.add(tc);
			tc.setTestSteps(lml);
		}

		tabName = caseNames.get(0);
		ZKUtils.vmRefresh(TestPlanViewModel.this, "testPlan", "tabName");
	}

	private void importCase() {
		Fileupload.get(new EventListener<UploadEvent>() {
			@Override
			public void onEvent(UploadEvent upload) throws Exception {
				Media media = upload.getMedia();
				try {
					parserXlsx(media.getStreamData(), StringUtils.substringBefore(media.getName(), "."));
				} catch (Exception ex) {
					Clients.alert("請修正您輸入的資料後, 重新進行上傳! Error:" + ex.getMessage(), "資料解析失敗", Clients.NOTIFICATION_TYPE_ERROR);
				}
			}
		});
	}

	@Command
	public void easyStart() {
		EasyProcessViewModel.show(driverType, driverSize, testPlan);
	}

	public String getDriverSize() {
		return driverSize;
	}

	public void setDriverSize(String driverSize) {
		this.driverSize = driverSize;
	}

	public String getDriverType() {
		return driverType;
	}

	public void setDriverType(String driverType) {
		this.driverType = driverType;
	}

	public TestPlan getTestPlan() {
		return testPlan;
	}

	public void setTestPlan(TestPlan testPlan) {
		this.testPlan = testPlan;
	}

	public TestCase getTabName() {
		return tabName;
	}

	public void setTabName(TestCase tabName) {
		this.tabName = tabName;
	}

}