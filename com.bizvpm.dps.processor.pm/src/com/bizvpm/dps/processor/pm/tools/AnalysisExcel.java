package com.bizvpm.dps.processor.pm.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;

import com.mongodb.BasicDBObject;

public class AnalysisExcel {

	public static final String[] FIELDS = new String[] { "code", "name", "name_en", "entry", "management_scope" };

	/**
	 * 解析Excel
	 * 
	 * @param newFileName
	 * @param md5
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static List<BasicDBObject> analysisExcel(String newFileName, String md5)
			throws IOException, InvalidFormatException {
		List<BasicDBObject> result = new ArrayList<BasicDBObject>();
		FileInputStream fis = new FileInputStream(newFileName);
		// 用poi先把文件流读进来
		POIFSFileSystem poifs = new POIFSFileSystem(fis);
		fis.close();
		// 读取Excel工作簿
		HSSFWorkbook hssfwb = new HSSFWorkbook(poifs);
		// 读取Excel 只取第一页
		HSSFSheet sheet = hssfwb.getSheetAt(0);
		// 从第二行开始取
		int rowIndex = 1;
		HSSFRow row = sheet.getRow(rowIndex);
		int physicalNumberOfCells = row.getPhysicalNumberOfCells();
		if (physicalNumberOfCells == 5) {
			while (row != null) {
				BasicDBObject document = new BasicDBObject();
				for (int i = 0; i < 5; i++) {
					HSSFCell cellAmount = row.getCell(i);
					Object value = getCellValue(cellAmount);
					document.put(FIELDS[i], value);
				}
				result.add(document);
				rowIndex++;
				row = sheet.getRow(rowIndex);
			}
		}
		return result;
	}

	private static Object getCellValue(HSSFCell cell) {
		if (cell == null) {
			return null;
		}
		int srcType = cell.getCellType();
		Object value;
		switch (srcType) {
		case Cell.CELL_TYPE_BLANK:
			value = null;
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			value = cell.getBooleanCellValue();
			break;
		case Cell.CELL_TYPE_ERROR:
			value = null;
			break;
		case Cell.CELL_TYPE_NUMERIC:
			value = cell.getNumericCellValue();
			break;
		case Cell.CELL_TYPE_STRING:
			value = cell.getStringCellValue();
			break;
		default:
			value = cell.getStringCellValue();
			break;
		}
		return value;
	}
}
