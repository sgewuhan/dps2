package com.bizvpm.dps.processor.topsreport;

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
	public Dispatch openDocument(ActiveXComponent app, String filename) throws Exception {
		Dispatch dis = app.getProperty("Documents").toDispatch();
		dis = Dispatch.invoke(dis, "Add", Dispatch.Method, new Object[0], new int[1]).toDispatch();
		Dispatch.invoke(app.getProperty("Selection").toDispatch(), "InsertFile", Dispatch.Method,
				new Object[] { filename, "", new Variant(false), new Variant(false), new Variant(false) }, new int[3]);
		return dis;
	}

	
	/*
	 * ����new Variant(16)
	 * word ����ʽ�����б�https://docs.microsoft.com/zh-cn/dotnet/api/microsoft.office.interop.word.wdsaveformat?view=word-pia
	 * ��MSDN�п���WdSaveFormat ���в�ѯ
	 */
	
	@Override
	public void convert(Dispatch dis, String toFilename) throws Exception {
		Dispatch.invoke(dis, "SaveAs", Dispatch.Method, new Object[] { toFilename, new Variant(16) }, new int[1]);
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
