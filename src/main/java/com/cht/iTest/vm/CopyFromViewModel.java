package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

import com.cht.iTest.entity.TestCase;
import com.cht.iTest.entity.TestNode;
import com.cht.iTest.entity.TestPlan;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.service.CommonService;
import com.cht.iTest.util.ObjectUtils;
import com.cht.iTest.util.ZKUtils;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CopyFromViewModel implements Serializable {

	private static final long serialVersionUID = 245186345756483665L;
	public static final String ZUL = "//copyFrom.zul";
	private static final String PARAM_TESTPLAN = "PARAM_TESTPLAN";
	private static final String PARAM_CONFIRM = "PARAM_CONFIRM";

	@WireVariable
	private CommonService myService;

	private DefaultTreeModel<? extends TestNode> srcTreeModel;
	private DefaultTreeModel<? extends TestNode> tarTreeModel;
	private ListModelList<String> testPlans = new ListModelList<String>();

	private TestPlan tar;
	private Integer selectAdd = 0;
	private String addName = "";
	private Confirm confirm;

	public interface Confirm {

		void ok(TestPlan plan);

	}

	private static DefaultTreeNode<TestNode> recusive(TestNode node, boolean clearId) {
		DefaultTreeNode<TestNode> treeNode = null;

		if (clearId) {
			node.setId(null);
		}

		if (CollectionUtils.isNotEmpty(node.getChildren())) {
			treeNode = new DefaultTreeNode<TestNode>(node, new LinkedList<DefaultTreeNode<TestNode>>());
			DefaultTreeNode<TestNode> temp = null;

			for (TestNode child : node.getChildren()) {
				temp = recusive(child, clearId);
				treeNode.getChildren().add(temp);
			}
		} else {
			treeNode = new DefaultTreeNode<TestNode>(node);
		}

		return treeNode;
	}

	private static DefaultTreeNode<TestNode> recusive(TestNode node) {
		return recusive(node, false);
	}

	@SuppressWarnings({ "unchecked" })
	@Command
	public void move(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent drop) {
		Component tar = drop.getTarget();
		Treeitem drag = (Treeitem) drop.getDragged();
		DefaultTreeNode<TestNode> drapNode = drag.getValue();

		if (!"self".equals(drag.getDraggable())) {
			drapNode = recusive(drapNode.getData(), true);
		}

		DefaultTreeNode<TestNode> dropNode = null;

		if (tar instanceof Tree) {
			dropNode = (DefaultTreeNode<TestNode>) tarTreeModel.getRoot();
			dropNode.add(drapNode);
		} else if (tar instanceof Treeitem) {
			Treeitem dropComp = (Treeitem) tar;
			dropNode = dropComp.getValue();

			if (drapNode.isLeaf() == dropNode.isLeaf()) {
				dropNode.getParent().insert(drapNode, dropNode.getParent().getIndex(dropNode));
			} else {
				dropNode.add(drapNode);
			}
		}
	}

	private static DefaultTreeModel<TestNode> createDefaultTreeModel(TestNode node) {
		DefaultTreeNode<TestNode> root = (node != null) ? recusive(node) : new DefaultTreeNode<TestNode>(node, new LinkedList<DefaultTreeNode<TestNode>>());
		DefaultTreeModel<TestNode> defaultTreeModel = new DefaultTreeModel<TestNode>(root);
		return defaultTreeModel;
	}

	@Init
	public void init() throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> params = (Map<String, Object>) Executions.getCurrent().getArg();
		confirm = (Confirm) params.get(CopyFromViewModel.PARAM_CONFIRM);
		tar = (TestPlan) params.get(CopyFromViewModel.PARAM_TESTPLAN);
		tar = ObjectUtils.deepCopy(tar);
		srcTreeModel = new DefaultTreeModel<TestNode>(new DefaultTreeNode<TestNode>(null, new LinkedList<DefaultTreeNode<TestNode>>()));
		tarTreeModel = createDefaultTreeModel(tar);
		testPlans.addAll(myService.findAllPlanNames());
	}

	@Command
	public void confirm(@BindingParam("dialogWin") Window dialogWin) {
		String name = tar.getName();
		tar = (TestPlan) dumpTestNode(tarTreeModel.getRoot());
		tar.setName(name);
		ListModelList<TestStep> lml = null;

		for (TestCase tc : tar.getTestCaseDetails()) {
			lml = new ListModelList<TestStep>(tc.getTestSteps());
			lml.setMultiple(true);
			tc.setTestSteps(lml);
		}

		confirm.ok(tar);
		dialogWin.detach();
	}

	private TestNode dumpTestNode(TreeNode<? extends TestNode> treeNode) {
		TestNode parentTestNode = treeNode.getData();
		TestNode childTestNode = null;

		if (!treeNode.isLeaf()) {
			int execOrder = 0;
			@SuppressWarnings("unchecked")
			List<TestNode> children = (List<TestNode>) parentTestNode.getChildren();
			children.clear();

			for (TreeNode<? extends TestNode> child : treeNode.getChildren()) {
				childTestNode = child.getData();
				childTestNode.setParent(parentTestNode);
				childTestNode.setExecOrder(execOrder++);
				children.add(dumpTestNode(child));
			}
		}

		return parentTestNode;
	}

	@Command
	public void selectPlan() {
		Iterator<String> iterator = testPlans.getSelection().iterator();

		if (iterator.hasNext()) {
			TestPlan src = myService.findTestPlan(iterator.next());
			srcTreeModel = createDefaultTreeModel(src);
			ZKUtils.vmRefresh(this, "srcTreeModel");
		}
	}

	@Command
	public void delete(@BindingParam("del") DefaultTreeNode<TestNode> del) {
		del.getParent().remove(del);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	@NotifyChange("addName")
	public void add() {
		if (StringUtils.isEmpty(addName)) {
			throw new WrongValueException("請輸入欲新增的節點名稱!");
		}

		boolean forCase = (selectAdd.intValue() == 0);
		TestNode addNode = forCase ? new TestCase() : new TestStep();
		addNode.setName(addName);
		DefaultTreeNode<TestNode> add = forCase ? new DefaultTreeNode<TestNode>(addNode, new ArrayList<DefaultTreeNode<TestNode>>()) : new DefaultTreeNode<TestNode>(addNode);

		if (!forCase && tarTreeModel.isSelectionEmpty()) {
			throw new WrongValueException("請選擇一個節點，以便新增步驟");
		}

		DefaultTreeNode<TestNode> node = null;
		boolean isRoot = tarTreeModel.isSelectionEmpty();

		if (!isRoot) {
			node = (DefaultTreeNode<TestNode>) tarTreeModel.getSelection().iterator().next();
		} else {
			node = (DefaultTreeNode<TestNode>) tarTreeModel.getRoot();
		}

		if (!isRoot && add.isLeaf() == node.isLeaf()) {
			DefaultTreeNode<TestNode> parent = (DefaultTreeNode<TestNode>) node.getParent();
			parent.insert(add, parent.getIndex(node));
			tarTreeModel.addOpenObject((TreeNode) node.getParent());
		} else {
			node.add(add);
			tarTreeModel.addOpenObject((TreeNode) node);
		}

		addName = null;
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

	public TestPlan getTar() {
		return tar;
	}

	public void setTar(TestPlan tar) {
		this.tar = tar;
	}

	public Integer getSelectAdd() {
		return selectAdd;
	}

	public void setSelectAdd(Integer selectAdd) {
		this.selectAdd = selectAdd;
	}

	public String getAddName() {
		return addName;
	}

	public void setAddName(String addName) {
		this.addName = addName;
	}

}
