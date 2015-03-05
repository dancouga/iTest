package com.cht.iTest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.util.StringUtils;

public class POIUtils {

	public static final String CONTENT_TYPE = "application/vnd.ms-excel";

	public static String getStringFromCell(Cell cell) {
		if (cell != null) {
			if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
			}

			return cell.getStringCellValue().trim();
		}

		return "";
	}

	public static List<String[]> xls2StringAryList(InputStream is, String sheetName, int cols) throws Exception {
		List<String[]> result = new ArrayList<String[]>();

		Workbook workbook = WorkbookFactory.create(is);
		Sheet sheet = workbook.getSheet(sheetName);
		Iterator<Row> iterator = sheet.iterator();
		Row row = null;
		String[] rowData = null;

		outerloop: while (iterator.hasNext()) {
			row = iterator.next();
			rowData = new String[cols];

			for (int j = 0; j < cols; j++) {
				rowData[j] = getStringFromCell(row.getCell(j));

				if (j == 0 && StringUtils.isEmpty(rowData[j])) {
					break outerloop;
				}
			}

			result.add(rowData);
		}

		workbook.close();
		return result;
	}

	public static Map<String, List<String[]>> xls2StrAryListMap(InputStream is, int cols) throws Exception {
		Workbook workbook = WorkbookFactory.create(is);

		Map<String, List<String[]>> map = new HashMap<String, List<String[]>>();
		List<String[]> result = null;
		Row row = null;
		String[] rowData = null;
		Sheet sheet = null;
		Iterator<Row> iterator = null;

		for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
			sheet = workbook.getSheetAt(s);
			iterator = sheet.iterator();
			result = new ArrayList<String[]>();

			outerloop: while (iterator.hasNext()) {
				row = iterator.next();
				rowData = new String[cols];

				for (int j = 0; j < cols; j++) {
					rowData[j] = getStringFromCell(row.getCell(j));

					if (j == 0 && StringUtils.isEmpty(rowData[j])) {
						break outerloop;
					}
				}

				result.add(rowData);
			}

			map.put(sheet.getSheetName(), result);
		}

		workbook.close();
		return map;
	}

	public static <T> byte[] toXlsByteAry(List<T> datas, Bean2XlsConvertor<T> convertor) throws IOException {

		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet(convertor.sheetName());
		CellStyle headStyle = createHeadStyle(workbook);
		CellStyle cellStyle = createCellStyle(workbook);
		int pos = 0;

		if (convertor.header() != null) {
			createRow(sheet, headStyle, convertor.header(), pos++);
		}

		for (T bean : datas) {
			createRow(sheet, cellStyle, convertor.process(bean), pos++);
		}

		for (short i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
			sheet.autoSizeColumn(i);
		}

		return toByteAry(workbook);
	}

	public static <T> byte[] toXlsByteAry(String[] tarTabName, List<List<T>> newValuesList, Bean2XlsConvertor<T> convertor) throws IOException {
		Workbook workbook = new HSSFWorkbook();
		int index = 0;
		for (List<T> newValues : newValuesList) {
			Sheet sheet = workbook.createSheet(tarTabName[index++]);
			CellStyle headStyle = createHeadStyle(workbook);
			CellStyle cellStyle = createCellStyle(workbook);

			int pos = 0;

			if (convertor.header() != null) {
				createRow(sheet, headStyle, convertor.header(), pos++);
			}

			for (T bean : newValues) {
				createRow(sheet, cellStyle, convertor.process(bean), pos++);
			}

			for (short i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}
		}

		return toByteAry(workbook);
	}

	public static <T> List<T> toBeanList(InputStream xlsIs, Xls2BeanConvertor<T> convertor) throws Exception {
		List<T> result = new ArrayList<T>();
		Workbook workbook = WorkbookFactory.create(xlsIs);
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> iterator = sheet.iterator();
		Row row = null;
		T bean = null;
		int flag = 0;

		while (iterator.hasNext()) {
			row = iterator.next();

			if (flag++ < convertor.startIndex()) {
				continue;
			}

			bean = convertor.process(row);

			if (bean == null) {
				break;
			} else {
				result.add(bean);
			}
		}

		workbook.close();
		return result;
	}

	public static <T> Map<String, List<T>> toBeanMap(InputStream xlsIs, Xls2BeanConvertor<T> convertor) throws Exception {
		Map<String, List<T>> map = new LinkedHashMap<String, List<T>>();
		Workbook workbook = WorkbookFactory.create(xlsIs);
		List<T> result = null;
		Iterator<Row> iterator = null;
		Row row = null;
		T bean = null;
		Sheet sheet = null;
		int flag = 0;

		for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
			sheet = workbook.getSheetAt(s);
			iterator = sheet.iterator();
			flag = 0;
			result = new ArrayList<T>();

			while (iterator.hasNext()) {
				row = iterator.next();

				if (flag++ < convertor.startIndex()) {
					continue;
				}
				
				bean = convertor.process(row);

				if (bean == null) {
					break;
				} else {
					result.add(bean);
				}

				map.put(sheet.getSheetName(), result);
			}
		}

		workbook.close();
		return map;
	}

	public static byte[] toXlsByteAry(String sheetName, String[] headers, List<String[]> datas) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet(sheetName);
		CellStyle headStyle = createHeadStyle(workbook);
		CellStyle cellStyle = createCellStyle(workbook);
		int rownum = 0;

		if (ArrayUtils.isNotEmpty(headers)) {
			createRow(sheet, headStyle, headers, rownum++);
		}

		for (String[] dataAry : datas) {
			createRow(sheet, cellStyle, dataAry, rownum++);
		}

		for (short i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
			sheet.autoSizeColumn(i);
		}

		return toByteAry(workbook);
	}

	public static byte[] generateXlsByteAry(Map<String, List<String[]>> shetDataMaps) throws IOException {
		Workbook workbook = new HSSFWorkbook();
		String sheetName = null;
		int rownum = 0;
		int cellnum = 0;

		for (Entry<String, List<String[]>> entry : shetDataMaps.entrySet()) {
			sheetName = entry.getKey();
			Sheet sheet = workbook.createSheet(sheetName);
			rownum = 0;

			for (String[] dataAry : entry.getValue()) {
				Row row = sheet.createRow(rownum++);
				cellnum = 0;

				for (String data : dataAry) {
					row.createCell(cellnum++).setCellValue(data);
				}
			}
		}

		return toByteAry(workbook);
	}

//	private static void addSheetRows(Sheet sheet, List<String[]> rows) throws IOException {
//		int rownum = 1;
//		int cellnum = 0;
//
//		for (String[] dataAry : rows) {
//			Row row = sheet.createRow(rownum++);
//			cellnum = 0;
//
//			for (String data : dataAry) {
//				row.createCell(cellnum++).setCellValue(data);
//			}
//		}
//	}
//
//	private static void addSheetHeaders(Sheet sheet, String[] headers) throws IOException {
//		int cellnum = 0;
//		Row row = sheet.createRow(0);
//
//		for (String data : headers) {
//			row.createCell(cellnum++).setCellValue(data);
//		}
//	}

	private static <T> byte[] toByteAry(Workbook workbook) throws IOException {
		ByteArrayOutputStream baos = null;
		byte[] res = null;

		try {
			baos = new ByteArrayOutputStream();
			workbook.write(baos);
			res = baos.toByteArray();
		} finally {
			IOUtils.closeQuietly(baos);
		}

		return res;
	}

	private static CellStyle createHeadStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font head = workbook.createFont();
		head.setColor(HSSFColor.WHITE.index);
		head.setFontName("Courier New");
		head.setFontHeightInPoints((short) 12);
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setFillForegroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		head.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setBorderBottom(CellStyle.BORDER_MEDIUM);
		style.setFont(head);
		return style;
	}

	private static CellStyle createCellStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		style.setWrapText(true);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		font.setFontName("Courier New");
		font.setFontHeightInPoints((short) 10);
		style.setFont(font);
		return style;
	}

	private static void createCell(Row row, CellStyle style, String value, int pos) {
		Cell cell = row.createCell(pos);
		cell.setCellStyle(style);
		cell.setCellValue(value);
	}

	private static void createRow(Sheet sheet, CellStyle style, String[] datas, int pos) {
		Row row = sheet.createRow(pos);
		int cellnum = 0;

		for (String data : datas) {
			createCell(row, style, data, cellnum++);
		}
	}

	public interface Xls2BeanConvertor<T> {

		T process(Row row);

		int startIndex();

	}

	public interface Bean2XlsConvertor<T> {

		String[] process(T bean);

		String[] header();

		String sheetName();

	}

}
