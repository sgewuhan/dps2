package com.bizvpm.dps.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	/**
	 * The ID of the perspective as specified in the extension.
	 */

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
	}
}
