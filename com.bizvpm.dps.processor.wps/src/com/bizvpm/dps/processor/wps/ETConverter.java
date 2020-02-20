package com.bizvpm.dps.processor.wps;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class ETConverter extends AbstractWPSConverter {

	@Override
	public ActiveXComponent getActiveXComponent() throws Exception {
		ActiveXComponent app = new ActiveXComponent("KET.Application");
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
