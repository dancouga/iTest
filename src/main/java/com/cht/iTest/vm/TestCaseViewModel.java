package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class TestCaseViewModel implements Serializable {

	private static final long serialVersionUID = 6470200880627655629L;

	private DefaultTreeModel<String> treeModel;
	private String caseName;
	private String errorMsg;
	private boolean canAddLeaf = true;
	private boolean canAddTrunk = true;
	private boolean deleteAddNode = false;

	@Init
	public void init() {
		List<DefaultTreeNode<String>> children = new LinkedList<DefaultTreeNode<String>>();
		treeModel = new DefaultTreeModel<String>(new DefaultTreeNode<String>("Root", children));
	}

	@Command
	@NotifyChange({ "canAddLeaf", "canAddTrunk", "deleteAddNode" })
	public void addCase(@BindingParam("type") String type) {
		if (Strings.isBlank(caseName)) {
			Clients.showNotification("Please enter Case Name.");
			return;
		}

		boolean isNotSelect = treeModel.getSelection().isEmpty();
		boolean isLeaf = "leaf".equals(type);

		List<TreeNode<String>> selectLayer = isNotSelect ? treeModel.getRoot().getChildren() : treeModel.getSelection().iterator().next().getChildren();
		DefaultTreeNode<String> node = !isLeaf ? new DefaultTreeNode<String>(caseName, new LinkedList<DefaultTreeNode<String>>()) : new DefaultTreeNode<String>(caseName);
		selectLayer.add(node);

		if (!isLeaf) {
			treeModel.addOpenObject(node);
		}

		this.setCanAddLeaf(isNotSelect);
		this.setCanAddTrunk(isNotSelect);
		this.setDeleteAddNode(false);
	}

	@Command
	@NotifyChange({ "canAddLeaf", "canAddTrunk", "deleteAddNode" })
	public void deleteCase() {
		for (TreeNode<String> node : treeModel.getSelection()) {
			node.getParent().remove(node);
		}

		this.setCanAddLeaf(false);
		this.setCanAddTrunk(treeModel.getRoot().getChildren().isEmpty() ? true : false);
		this.setDeleteAddNode(false);
	}

	@Command
	@NotifyChange({ "canAddLeaf", "canAddTrunk", "deleteAddNode" })
	public void selectNode() {
		List<TreeNode<String>> selectLayer = null;
		Iterator<TreeNode<String>> iterator = treeModel.getSelection().iterator();

		while (iterator.hasNext()) {
			selectLayer = iterator.next().getChildren();
		}

		this.setCanAddLeaf(selectLayer != null);
		this.setCanAddTrunk(selectLayer != null);
		this.setDeleteAddNode(true);
	}

	public DefaultTreeModel<String> getTreeModel() {
		return treeModel;
	}

	public void setTreeModel(DefaultTreeModel<String> treeModel) {
		this.treeModel = treeModel;
	}

	public boolean isCanAddLeaf() {
		return canAddLeaf;
	}

	public void setCanAddLeaf(boolean canAddLeaf) {
		this.canAddLeaf = canAddLeaf;
	}

	public boolean isCanAddTrunk() {
		return canAddTrunk;
	}

	public void setCanAddTrunk(boolean canAddTrunk) {
		this.canAddTrunk = canAddTrunk;
	}

	public String getCaseName() {
		return caseName;
	}

	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}

	public boolean isDeleteAddNode() {
		return deleteAddNode;
	}

	public void setDeleteAddNode(boolean deleteAddNode) {
		this.deleteAddNode = deleteAddNode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
