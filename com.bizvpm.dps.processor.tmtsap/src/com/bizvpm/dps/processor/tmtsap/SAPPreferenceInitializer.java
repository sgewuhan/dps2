package com.bizvpm.dps.processor.tmtsap;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class SAPPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstacts.S_EAI_SAP_MAXCONN, 200);
		store.setDefault(PreferenceConstacts.S_EAI_SAP_CLIENT, "700");
		store.setDefault(PreferenceConstacts.S_EAI_SAP_USERID, "ITFSAP");
		store.setDefault(PreferenceConstacts.S_EAI_SAP_PASSWORD, "12392008");
		store.setDefault(PreferenceConstacts.S_EAI_SAP_LANGUAGE, "ZH");
		store.setDefault(PreferenceConstacts.S_EAI_SAP_HOST, "172.16.9.74");
		store.setDefault(PreferenceConstacts.S_EAI_SAP_INSTANCENUMBER, "00");
	}

}
