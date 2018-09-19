package com.bizvpm.dps.processor.email;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class Activator extends AbstractUIPlugin {
	
	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	public static Activator getDefault() {
		return plugin;
	}

	
}
