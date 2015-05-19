package com.cht.iTest.vm.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;

import com.cht.iTest.def.Action;
import com.cht.iTest.def.FindMethod;
import com.cht.iTest.def.Skip;
import com.cht.iTest.def.Snapshot;
import com.cht.iTest.def.StepDefine;
import com.cht.iTest.def.Sync;
import com.cht.iTest.entity.TestStep;
import com.cht.iTest.util.POIUtils;
import com.cht.iTest.util.POIUtils.Bean2XlsConvertor;
import com.cht.iTest.util.POIUtils.Xls2BeanConvertor;

@Service("testPlanExcelParser")
public class TestPlanExcelParser implements Xls2BeanConvertor<TestStep>, Bean2XlsConvertor<TestStep> {

	private static final String[] colums = new String[] { "Name", "Skip", "Action", "Sync", "Element", "FindMethod", "InputValue", "Snapshot", "Description" };

	@Override
	public TestStep process(Row row) {
		TestStep testStep = new TestStep();
		Cell cell = null;
		String data = null;

		boolean isNameBlank = true;

		for (StepDefine stepDefine : StepDefine.values()) {
			cell = row.getCell(stepDefine.ordinal());
			data = POIUtils.getStringFromCell(cell);

			if (StringUtils.isBlank(data)) {
				continue;
			}

			switch (stepDefine) {
			case name:
				isNameBlank = false;
				testStep.setName(data);
				break;
			case skip:
				data = StringUtils.upperCase(data);
				testStep.setSkip(Skip.valueOf(data));
				break;
			case action:
				data = StringUtils.lowerCase(data);
				testStep.setAction(Action.valueOf(data));
				break;
			case sync:
				data = StringUtils.upperCase(data);
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
				data = StringUtils.upperCase(data);
				testStep.setSnapshot(Snapshot.valueOf(data));
				break;
			case description:
				testStep.setDescription(data);
				break;
			}
		}

		if (isNameBlank) {
			return null;
		}

		return testStep;
	}

	@Override
	public int startIndex() {
		return 1;
	}

	@Override
	public String[] process(TestStep bean) {
		String[] result = new String[9];

		result[0] = bean.getName();

		if (bean.getSkip() != null) {
			result[1] = bean.getSkip().name();
		}

		if (bean.getAction() != null) {
			result[2] = bean.getAction().name();
		}

		if (bean.getSync() != null) {
			result[3] = bean.getSync().name();
		}

		result[4] = bean.getElement();

		if (bean.getFindMethod() != null) {
			result[5] = bean.getFindMethod().name();
		}

		result[6] = bean.getInputValue();

		if (bean.getSnapshot() != null) {
			result[7] = bean.getSnapshot().name();
		}

		result[8] = bean.getDescription();
		return result;
	}

	@Override
	public String[] header() {
		return colums;
	}

}
