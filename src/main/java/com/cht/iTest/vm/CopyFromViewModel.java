package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

import com.cht.iTest.entity.TestNode;
import com.cht.iTest.entity.TestPlan;
import com.cht.iTest.service.MyService;
import com.cht.iTest.util.ZKUtils;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CopyFromViewModel implements Serializable {

	private static final long serialVersionUID = 245186345756483665L;
	public static final String ZUL = "//copyFrom.zul";
	private static final String PARAM_TESTPLAN = "PARAM_TESTPLAN";
	private static final String PARAM_CONFIRM = "PARAM_CONFIRM";

	@WireVariable
	private MyService myService;

	private DefaultTreeModel<? extends TestNode> srcTreeModel;
	private ListModelList<String> testPlans = new ListModelList<String>();
	private DefaultTreeModel<? extends TestNode> tarTreeModel;
	private TestPlan src;
	private TestPlan tar;
	private Confirm confirm;

	public interface Confirm {

		void ok(TestPlan plan);

	}

	private static DefaultTreeNode<TestNode> recusive(TestNode node, List<DefaultTreeNode<TestNode>> allNodes) {
		DefaultTreeNode<TestNode> treeNode = null;

		if (CollectionUtils.isNotEmpty(node.getChildren())) {
			treeNode = new DefaultTreeNode<TestNode>(node, new LinkedList<DefaultTreeNode<TestNode>>());
			DefaultTreeNode<TestNode> temp = null;

			for (TestNode child : node.getChildren()) {
				temp = recusive(child, allNodes);
				treeNode.getChildren().add(temp);
				allNodes.add(temp);
			}
		} else {
			treeNode = new DefaultTreeNode<TestNode>(node);
		}

		return treeNode;
	}

	private static DefaultTreeModel<TestNode> createDefaultTreeModel(TestNode node) {
		List<DefaultTreeNode<TestNode>> allNodes = new ArrayList<DefaultTreeNode<TestNode>>();
		DefaultTreeNode<TestNode> root = node != null ? recusive(node, allNodes) : new DefaultTreeNode<TestNode>(node, new LinkedList<DefaultTreeNode<TestNode>>());
		DefaultTreeModel<TestNode> defaultTreeModel = new DefaultTreeModel<TestNode>(root);

		for (DefaultTreeNode<TestNode> child : allNodes) {
			defaultTreeModel.addOpenObject(child);
		}

		return defaultTreeModel;
	}

	@Init
	public void init() {
		@SuppressWarnings("unchecked")
		Map<String, Object> params = (Map<String, Object>) Executions.getCurrent().getArg();
		confirm = (Confirm) params.get(CopyFromViewModel.PARAM_CONFIRM);
		tar = (TestPlan) params.get(CopyFromViewModel.PARAM_TESTPLAN);
		srcTreeModel = new DefaultTreeModel<TestNode>(new DefaultTreeNode<TestNode>(src, new LinkedList<DefaultTreeNode<TestNode>>()));
		tarTreeModel = createDefaultTreeModel(tar);
		testPlans.addAll(myService.findAllPlanNames());
	}

	@Command
	public void confirm() {
		confirm.ok(tar);
	}

	@Command
	public void selectPlan() {
		Iterator<String> iterator = testPlans.getSelection().iterator();

		if (iterator.hasNext()) {
			src = myService.findTestPlan(iterator.next());
			srcTreeModel = createDefaultTreeModel(src);
			ZKUtils.vmRefresh(this, "srcTreeModel");
		}
	}

	public static void show(TestPlan plan, Confirm confirm) {
		Map<String, Object> param = ZKUtils.argBuilder().put(PARAM_TESTPLAN, plan).put(PARAM_CONFIRM, confirm).build();
		Window window = (Window) Executions.getCurrent().createComponents(ZUL, null, param);
		window.doModal();
	}

	public DefaultTreeModel<? extends TestNode> getSrcTreeModel() {
		return srcTreeModel;
	}

	public void setSrcTreeModel(DefaultTreeModel<? extends TestNode> srcTreeModel) {
		this.srcTreeModel = srcTreeModel;
	}

	public ListModelList<String> getTestPlans() {
		return testPlans;
	}

	public void setTestPlans(ListModelList<String> testPlans) {
		this.testPlans = testPlans;
	}

	public DefaultTreeModel<? extends TestNode> getTarTreeModel() {
		return tarTreeModel;
	}

	public void setTarTreeModel(DefaultTreeModel<? extends TestNode> tarTreeModel) {
		this.tarTreeModel = tarTreeModel;
	}

	public TestPlan getSrc() {
		return src;
	}

	public void setSrc(TestPlan src) {
		this.src = src;
	}

	public TestPlan getTar() {
		return tar;
	}

	public void setTar(TestPlan tar) {
		this.tar = tar;
	}

}
