package com.bizvpm.dps.processor.msoffice;

import java.util.Map;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public abstract class AbstractMSOfficeConverter {

	private static final String[] WORD_FILE = new String[] { "doc", "docx" };
	private static final String[] EXCEL_FILE = new String[] { "xls", "xlsx" };
	private static final String[] PPT_FILE = new String[] { "ppt", "pptx" };
	private static final String[] HTML_FILE = new String[] { "html" };
	private static final String[] PDF_FILE = new String[] { "pdf" };
	public static final int FILETYPE_UNKONWN = 0;
	public static final int FILETYPE_WORD_FILE = 1;
	public static final int FILETYPE_EXCEL_FILE = 2;
	public static final int FILETYPE_PPT_FILE = 3;
	public static final int FILETYPE_HTML_FILE = 4;
	public static final int FILETYPE_PDF_FILE = 4;
	protected String sourceType;
	protected String targetType;

	public abstract ActiveXComponent getActiveXComponent() throws Exception;

	public abstract Dispatch openDocument(ActiveXComponent app, String filename, String templatePath) throws Exception;

	public abstract void convert(ActiveXComponent app, Dispatch dis, String fromFilename, String toFilename,
			Map<String, String> pics, Map<String, String> p) throws Exception;

	public abstract void dispose(ActiveXComponent app, Dispatch dis) throws Exception;

	public static AbstractMSOfficeConverter getInstance(String sourceType, String targetType) throws Exception {
		int fileType = getFileType(sourceType);

		if (FILETYPE_WORD_FILE == fileType) {
			return new MSWordConverter(sourceType, targetType);
		}
		if (FILETYPE_EXCEL_FILE == fileType) {
			return new MSExcelConverter(sourceType, targetType);
		}
		if (FILETYPE_PPT_FILE == fileType) {
			return new MSPowerPointConverter(sourceType, targetType);
		}
		if (FILETYPE_HTML_FILE == fileType) {
			fileType = getFileType(targetType);

			if (FILETYPE_WORD_FILE == fileType) {
				return new MSWordConverter(sourceType, targetType);
			}
			if (FILETYPE_EXCEL_FILE == fileType) {
				return new MSExcelConverter(sourceType, targetType);
			}
			if (FILETYPE_PPT_FILE == fileType) {
				return new MSPowerPointConverter(sourceType, targetType);
			}
		}
		throw new Exception("Unknown File Type");
	}

	protected static int getFileType(String sourceType) {
		sourceType = sourceType.toLowerCase();
		int type = checkFileType(sourceType, WORD_FILE, FILETYPE_WORD_FILE);
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(sourceType, EXCEL_FILE, FILETYPE_EXCEL_FILE);
		} else {
			return type;
		}
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(sourceType, PPT_FILE, FILETYPE_PPT_FILE);
		} else {
			return type;
		}
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(sourceType, HTML_FILE, FILETYPE_HTML_FILE);
		} else {
		}
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(sourceType, PDF_FILE, FILETYPE_PDF_FILE);
		} else {
		}
		return type;
	}

	private static int checkFileType(String sourceType, String[] p, int result) {
		for (int i = 0; i < p.length; i++) {
			if (sourceType.equals(p[i])) {
				return result;
			}
		}
		return FILETYPE_UNKONWN;
	}
}
