package com.bizvpm.dps.processor.tmtsap;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		plugin = this;
	}

	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	public int getMaxconn() {
		return plugin.getPreferenceStore().getInt(PreferenceConstacts.S_EAI_SAP_MAXCONN);
	}

	public String getClient() {
		return plugin.getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_CLIENT);
	}

	public String getUserId() {
		return plugin.getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_USERID);
	}

	public String getPassword() {
		return plugin.getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_PASSWORD);
	}

	public String getLang() {
		return plugin.getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_LANGUAGE);
	}

	public String getHost() {
		return plugin.getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_HOST);
	}

	public String getInstance() {
		return plugin.getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_INSTANCENUMBER);
	}
}
