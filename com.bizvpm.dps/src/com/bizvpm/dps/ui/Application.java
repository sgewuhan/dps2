package com.bizvpm.dps.ui;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.bizvpm.dps.Activator;
import com.bizvpm.dps.service.IPersistence;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		Shell shell = new Shell(display);

		IPersistence server = Activator.getServer();
		if(server == null){
			MessageBox message = new MessageBox(shell, SWT.ICON_ERROR|SWT.OK);
			message.setMessage("无法连接dps服务器，请检查您的配置。");
			message.open();
			return IApplication.EXIT_OK;
		}
		
		SigninDialog signin = new SigninDialog(shell);
		if(Dialog.OK!=signin.open()){
			return IApplication.EXIT_OK;
		}
		String hostName = signin.getHostName();
		String ip = signin.getHostIp();
		int port = signin.getHostPort();
		Activator.getDefault().signin(hostName,ip,port);
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
