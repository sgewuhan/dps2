package com.bizvpm.dps.processor.wps;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public abstract class AbstractWPSConverter {

	private static final String[] WORD_FILE = new String[] { "doc", "docx", "wps" };
	private static final String[] EXCEL_FILE = new String[] { "xls", "xlsx", "et" };
	private static final String[] PPT_FILE = new String[] { "ppt", "pptx", "dps" };
	public static final int FILETYPE_UNKONWN = 0;
	public static final int FILETYPE_WORD_FILE = 1;
	public static final int FILETYPE_EXCEL_FILE = 2;
	public static final int FILETYPE_PPT_FILE = 3;

	public abstract ActiveXComponent getActiveXComponent() throws Exception;

	public abstract Dispatch openDocument(ActiveXComponent app, String filename) throws Exception;

	public abstract void convert(Dispatch dis, String toFilename) throws Exception;

	public abstract void dispose(ActiveXComponent app, Dispatch dis) throws Exception;

	public static AbstractWPSConverter getInstance(String sourceType) throws Exception {
		int fileType = getFileType(sourceType);

		if (FILETYPE_WORD_FILE == fileType) {
			return new WPSConverter();
		}
		if (FILETYPE_EXCEL_FILE == fileType) {
			return new ETConverter();
		}
		if (FILETYPE_PPT_FILE == fileType) {
			return new DPSConverter();
		}
		throw new Exception("Unknown File Type");
	}

	private static int getFileType(String sourceType) {
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
