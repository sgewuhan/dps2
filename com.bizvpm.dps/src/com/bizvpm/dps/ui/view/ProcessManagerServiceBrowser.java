package com.bizvpm.dps.ui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.bizvpm.dps.Activator;

public class ProcessManagerServiceBrowser extends ViewPart {

	public static final String ID = "com.bizvpm.dps.view";
	private Browser b;

	public void createPartControl(Composite parent) {
		b = new Browser(parent, SWT.NONE);
		b.setUrl(Activator.getDefault().getUrl()+"?wsdl");
	}

	@Override
	public void setFocus() {
		b.setFocus();
	}

}
