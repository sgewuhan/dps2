package com.bizvpm.dps.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bizvpm.dps.ui.view.IRefreshable;

public class Refresh extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if(part instanceof IRefreshable){
			IRefreshable refreshable = (IRefreshable) part;
			refreshable.refresh();
		}
		return null;
	}

}
