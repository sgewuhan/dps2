package com.bizvpm.dps.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bizvpm.dps.Activator;
import com.bizvpm.dps.service.ProcessorConfig;
import com.bizvpm.dps.ui.view.IRefreshable;

public class StartProcessor extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection != null && !selection.isEmpty()) {
			try {
				ProcessorConfig config = (ProcessorConfig) selection.getFirstElement();
				Activator.getDefault().getProcessorManager()
						.startProcessor(config);
				IWorkbenchPart part = HandlerUtil.getActivePart(event);
				if(part instanceof IRefreshable){
					((IRefreshable) part).refresh(config);
				}
			} catch (Exception e) {
				MessageDialog.open(MessageDialog.ERROR, HandlerUtil.getActiveShell(event), "´¦ÀíÆ÷Æô¶¯Ê§°Ü", e.getMessage(), SWT.SHEET);
			}
		}
		return null;
	}

}
