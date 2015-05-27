package com.cht.iTest.vm.queue;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

import com.cht.iTest.def.Status;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.selenium.App;
import com.cht.iTest.util.Cache;
import com.cht.iTest.util.ZKUtils;

public class TestStepExecuteAsyhcnronizer {

	private String queueName;
	private String gcmdName;
	private App app;
	private Map<String, String> execContextUpdateMap = new HashMap<String, String>();

	public TestStepExecuteAsyhcnronizer(String queueName, String globalCommandName, App app) {
		this.queueName = queueName;
		this.gcmdName = globalCommandName;
		this.app = app;
		app.createIterator();
	}

	public void startLongOperation() {
		final String workingQueueName = "workingQueue" + System.currentTimeMillis();
		EventQueue<Event> eq = EventQueues.lookup(workingQueueName);

		eq.subscribe(new EventListener<Event>() {
			int times = Cache.getSysCateIntVal(App.RETRY_TIMES, "2").intValue();

			private void job() throws Exception {
				execContextUpdateMap.clear();
				app.doCurrentStep();
				String var[] = app.getCurrentGetVar();

				if (var != null) {
					execContextUpdateMap.put(var[0], var[1]);
				}
			}

			@Override
			public void onEvent(Event event) throws Exception {
				if (app.getTestStep() != null) {
					for (int i = 0; i <= times; i++) {
						try {
							job();
							break;
						} catch (Exception ex) {
							if (i == times) {
								ex.printStackTrace();
								app.getTestStep().setErrorMsg(ex.getMessage());
								app.getTestStep().setExeStatus(Status.Fail);
								app.stop();
							}
						}
					}

					app.setTestStep(null);
				} else if (app.hasNext()) {
					TestStep testStep = app.doIteratorNext();
					testStep.setExeStatus(Status.Playing);
					app.setTestStep(testStep);
				}
			}
		}, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				Map<String, Object> param = ZKUtils.argBuilder().put("process", app.getProcess()).put("execContextUpdateMap", execContextUpdateMap).build();
				BindUtils.postGlobalCommand(queueName, EventQueues.DESKTOP, gcmdName, param);

				if (!app.hasNext() && app.getTestStep() == null) {
					app.stop();
					EventQueues.remove(workingQueueName);
				} else {
					eq.publish(new Event("trigger"));
				}
			}
		});

		eq.publish(new Event("trigger"));
	}

}