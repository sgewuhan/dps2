package com.bizvpm.dps.processor.msoffice;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class MSPowerPointConverter extends AbstractMSOfficeConverter {

	@Override
	public ActiveXComponent getActiveXComponent() throws Exception {
		return new ActiveXComponent("PowerPoint.Application");
	}

	@Override
	public Dispatch openDocument(ActiveXComponent app, String filename)
			throws Exception {
		Dispatch dis = app.getProperty("Presentations").toDispatch();
		return Dispatch.call(dis, "Open", filename, true, true, false)
				.toDispatch();
	}

	@Override
	public void convert(Dispatch dis, String toFilename) throws Exception {
		Dispatch.call(dis, "SaveAs", toFilename, 32);
	}

	@Override
	public void dispose(ActiveXComponent app, Dispatch dis) throws Exception {
		if (dis != null) {
			Dispatch.call(dis, "Close");
		}
		if (app != null) {
			app.invoke("Quit");
			app = null;
		}
	}

}
