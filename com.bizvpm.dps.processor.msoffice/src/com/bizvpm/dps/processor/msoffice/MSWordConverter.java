package com.bizvpm.dps.processor.msoffice;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class MSWordConverter extends AbstractMSOfficeConverter {

	@Override
	public ActiveXComponent getActiveXComponent() throws Exception {
		ActiveXComponent app = new ActiveXComponent("Word.Application");
		app.setProperty("Visible", false);
		return app;
	}

	@Override
	public Dispatch openDocument(ActiveXComponent app, String filename)
			throws Exception {
		Dispatch dis = app.getProperty("Documents").toDispatch();
		dis = Dispatch.invoke(
				dis,
				"Open",
				Dispatch.Method,
				new Object[] { filename, new Variant(false), new Variant(true),
						new Variant(false) }, new int[1]).toDispatch();
		Dispatch.put(dis, "RemovePersonalInformation", false);
		return dis;
	}

	@Override
	public void convert(Dispatch dis, String toFilename) throws Exception {
		Dispatch.call(dis, "ExportAsFixedFormat", toFilename, 17);
	}

	@Override
	public void dispose(ActiveXComponent app, Dispatch dis) throws Exception {
		if (dis != null) {
			Dispatch.call(dis, "Close", false);
		}
		if (app != null) {
			app.invoke("Quit", 0);
			app = null;
		}
	}
}
