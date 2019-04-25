package com.bizvpm.dps.processor.topsreport;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public abstract class AbstractMSOfficeConverter {

	public abstract ActiveXComponent getActiveXComponent() throws Exception;

	public abstract Dispatch openDocument(ActiveXComponent app, String filename) throws Exception;

	public abstract void convert(Dispatch dis, String toFilename) throws Exception;

	public abstract void dispose(ActiveXComponent app, Dispatch dis) throws Exception;

	public static AbstractMSOfficeConverter getInstance() throws Exception {
		return new MSWordConverter();
	}

}
