package com.cht.iTest.vm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.ListModelList;

import com.cht.iTest.def.Action;
import com.cht.iTest.def.FindMethod;
import com.cht.iTest.def.Snapshot;
import com.cht.iTest.def.StepDefine;
import com.cht.iTest.def.Sync;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.selenium.App;
import com.cht.iTest.util.POIUtils;
import com.cht.iTest.util.POIUtils.Xls2BeanConvertor;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class TestPlanViewModel implements Xls2BeanConvertor<TestStep>, Serializable {

	private static final long serialVersionUID = -3975437306120012044L;

	private List<ListModelList<TestStep>> testCaseModel = new ArrayList<ListModelList<TestStep>>();
	private List<String> caseNames = null;
	private AImage image;
	private TestStep selected;

	@Init
	public void init() {

	}

	@Command
	@NotifyChange({ "caseNames", "testCaseModel" })
	public void uploadTestCase(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent upload) throws Exception {
		Media media = upload.getMedia();
		Map<String, List<TestStep>> testCaseMap = POIUtils.toBeanMap(media.getStreamData(), this);
		caseNames = new ArrayList<String>(testCaseMap.keySet());

		for (List<TestStep> list : testCaseMap.values()) {
			testCaseModel.add(new ListModelList<TestStep>(list));
		}
	}

	@Command
	public void execute() throws Exception {
		if (caseNames == null) {
			return;
		}

		App app = App.getInstance();

		for (ListModelList<TestStep> model : testCaseModel) {
			app.addAll(model);
		}

		app.start().exit();
	}

	@Command
	@NotifyChange("image")
	public void viewSnapshot() throws Exception {
		image = null;
		
		if (selected != null && selected.getSnapshotImg() != null) {
			image = new AImage("snapshot", selected.getSnapshotImg());
		}
	}

	@Override
	public TestStep process(Row row) {
		TestStep testStep = new TestStep();
		Cell cell = null;
		String data = null;
		testStep.setCaseName(row.getSheet().getSheetName());

		for (StepDefine stepDefine : StepDefine.values()) {
			cell = row.getCell(stepDefine.ordinal());
			data = POIUtils.getStringFromCell(cell);

			switch (stepDefine) {
			case name:
				testStep.setName(data);
				break;
			case action:
				testStep.setAction(Action.valueOf(data));
				break;
			case sync:
				testStep.setSync(Sync.valueOf(data));
				break;
			case element:
				testStep.setElement(data);
				break;
			case findMethod:
				testStep.setFindMethod(FindMethod.valueOf(data));
				break;
			case inputValue:
				testStep.setInputValue(data);
				break;
			case snapshot:
				testStep.setSnapshot(Snapshot.valueOf(data));
				break;
			case description:
				testStep.setDescription(data);
				break;
			}
		}

		return testStep;
	}
	
	@Command
	@NotifyChange({ "caseNames", "testCaseModel" })
	public void changeExecSeq(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent drop){
		Component parent = drop.getDragged().getParent();
		
		int dragPos = parent.getChildren().indexOf(drop.getDragged());
		int dropPos = parent.getChildren().indexOf(drop.getTarget());
		
		caseNames.add(dropPos, caseNames.remove(dragPos));
		testCaseModel.add(dropPos, testCaseModel.remove(dragPos));
	}

	@Override
	public int startIndex() {
		return 1;
	}

	public List<String> getCaseNames() {
		return caseNames;
	}

	public void setCaseNames(List<String> caseNames) {
		this.caseNames = caseNames;
	}

	public List<ListModelList<TestStep>> getTestCaseModel() {
		return testCaseModel;
	}

	public void setTestCaseModel(List<ListModelList<TestStep>> testCaseModel) {
		this.testCaseModel = testCaseModel;
	}

	public AImage getImage() {
		return image;
	}

	public void setImage(AImage image) {
		this.image = image;
	}
	
	public TestStep getSelected() {
		return selected;
	}

	public void setSelected(TestStep selected) {
		this.selected = selected;
	}

}