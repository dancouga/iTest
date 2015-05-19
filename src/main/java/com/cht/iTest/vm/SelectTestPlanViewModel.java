package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

import com.cht.iTest.entity.TestCase;
import com.cht.iTest.entity.TestPlan;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.service.MyService;
import com.cht.iTest.util.ZKUtils;
import com.cht.iTest.util.ZKUtils.MessageAction;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SelectTestPlanViewModel implements Serializable {

	private static final long serialVersionUID = -1721368170673453483L;
	private static final String ZUL = "/selectTestPlan.zul";
	private static final String PARAM_ACTION = "ACTION";
	private ListModelList<String> testPlans = new ListModelList<String>();

	@WireVariable
	private MyService myService;

	private Action action = null;

	public interface Action {

		void ok(TestPlan testPlan);

	}

	@Init
	public void init() {
		action = (Action) Executions.getCurrent().getArg().get(PARAM_ACTION);
		List<String> plans = myService.findAllPlanNames();

		if (!plans.isEmpty()) {
			testPlans.addAll(plans);
			testPlans.addToSelection(testPlans.get(0));
		}
	}

	@Command
	public void confirm(@BindingParam("win") Window planSelectWin) {
		Iterator<String> iterator = testPlans.getSelection().iterator();
		ListModelList<TestStep> lml = null;

		if (iterator.hasNext()) {
			TestPlan testPlan = myService.findTestPlan(iterator.next());

			for (TestCase tc : testPlan.getTestCaseDetails()) {
				lml = new ListModelList<TestStep>(tc.getTestSteps());
				lml.setMultiple(true);
				tc.setTestSteps(lml);
			}

			action.ok(testPlan);
		} else {
			return;
		}

		planSelectWin.detach();
	}
	
	@Command
	public void delete(){
		Iterator<String> iterator = testPlans.getSelection().iterator();
		
		if (iterator.hasNext()) {
			String planName = iterator.next();
			
			ZKUtils.msgYN("這項操作會刪除【" + planName + "】上所有資料，是否執行?", new MessageAction() {
				@Override
				public void doIfYes() throws Exception {
					myService.deleteTestPlan(planName);
					testPlans.remove(planName);
				}
			});
		} else {
			return;
		}
	}

	public ListModelList<String> getTestPlans() {
		return testPlans;
	}

	public void setTestPlans(ListModelList<String> testPlans) {
		this.testPlans = testPlans;
	}

	static void show(Action action) {
		Executions.getCurrent().createComponents(ZUL, null, ZKUtils.argBuilder().put(PARAM_ACTION, action).build());
	}

}