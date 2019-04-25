package com.bizvpm.dps.processor.topsreport;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class MSExcelConverter extends AbstractMSOfficeConverter {

	@Override
	public ActiveXComponent getActiveXComponent() throws Exception {
		ActiveXComponent app = new ActiveXComponent("Excel.Application");
		app.setProperty("Visible", false);
		return app;
	}

	@Override
	public Dispatch openDocument(ActiveXComponent app, String filename) throws Exception {
		Dispatch dis = app.getProperty("Workbooks").toDispatch();
		return Dispatch.invoke(dis, "Open", Dispatch.Method,
				new Object[] { filename, new Variant(false), new Variant(false) }, new int[9]).toDispatch();
	}

	@Override
	public void convert(Dispatch dis, String toFilename) throws Exception {
		// 原来的saveas只能打印活动或者指定的一个sheet内容
		// Dispatch.invoke(dis, "SaveAs", Dispatch.Method, new Object[] {
		// toFilename, new Variant(57), new Variant(false),
		// new Variant(57), new Variant(57), new Variant(false),
		// new Variant(true), new Variant(57), new Variant(false),
		// new Variant(true), new Variant(false) }, new int[1]);
		// 使用打印机也有问题
		// Dispatch.call(dis, "PrintOut", 1,100,1,false,"Adobe
		// PDF",true,false,toFilename,false);
		// 使用ExportAsFixedFormat，打印整个工作簿
		Dispatch.call(dis, "ExportAsFixedFormat", 0, toFilename);
	}

	@Override
	public void dispose(ActiveXComponent app, Dispatch dis) throws Exception {
		if (dis != null) {
			Dispatch.call(dis, "Close", new Variant(false));
		}
		if (app != null) {
			app.invoke("Quit", new Variant[] {});
			app = null;
		}
	}

}
