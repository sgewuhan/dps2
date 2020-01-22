package com.bizvpm.dps.processor.msoffice;

import java.util.Map;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class MSPowerPointConverter extends AbstractMSOfficeConverter {
	
	public MSPowerPointConverter(String sourceType, String targetType) {
		super.sourceType = sourceType;
		super.targetType = targetType;
	}

	@Override
	public ActiveXComponent getActiveXComponent() throws Exception {
		return new ActiveXComponent("PowerPoint.Application");
	}

	@Override
	public Dispatch openDocument(ActiveXComponent app, String filename, String templatePath) throws Exception {
		Dispatch dis = app.getProperty("Presentations").toDispatch();
		return Dispatch.call(dis, "Open", filename, true, true, false).toDispatch();
	}

	@Override
	public void convert(ActiveXComponent app, Dispatch dis, String fromFilename, String toFilename,Map<String, String> pics) throws Exception {
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
