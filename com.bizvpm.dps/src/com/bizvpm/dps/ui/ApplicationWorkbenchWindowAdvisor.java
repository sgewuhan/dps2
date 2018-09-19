package com.bizvpm.dps.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.bizvpm.dps.Activator;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	protected TrayItem trayItem;

	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 600));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
	}

	@Override
	public void postWindowOpen() {
		final IWorkbenchWindow window = getWindowConfigurer().getWindow();
		window.getShell().setMaximized(true);
		window.getShell().setText("DPS Service");
		final Tray sysTray = Display.getCurrent().getSystemTray();
		if (sysTray != null) {
			window.getShell().addShellListener(new ShellAdapter() {
				@Override
				public void shellIconified(ShellEvent e) {
					window.getShell().setVisible(false);
					if(trayItem == null){
						createTrayItem(sysTray,window);
					}
				}
			});
		}
		super.postWindowOpen();

	}

	protected void createTrayItem(Tray sysTray, final IWorkbenchWindow window) {
		trayItem = new TrayItem(sysTray, SWT.NONE);
		ImageDescriptor imageDescriptor = Activator
				.getImageDescriptor("dps.png");
		final Image image = imageDescriptor.createImage();
		trayItem.setImage(image);
		trayItem.setText("DPS Service");
		trayItem.setToolTipText("DPS ON " + Activator.getHostName()
				+ "[" + Activator.getPort() + "]");
		trayItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				window.getShell().setVisible(true);
				window.getShell().setMaximized(true);
				trayItem.dispose();
				trayItem = null;
				image.dispose();
				super.widgetSelected(e);
			}
		});
	}

}
