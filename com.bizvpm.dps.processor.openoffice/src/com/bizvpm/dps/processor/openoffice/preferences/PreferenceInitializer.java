package com.bizvpm.dps.processor.openoffice.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.bizvpm.dps.processor.openoffice.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.OFFICE_HOME_BIN, "D:/OpenOffice 4/program/soffice.bin");
		store.setDefault(PreferenceConstants.PORT_NUMBERS, "8130 8131 8132 8133 8134 8135 8136 8138 8139");
		store.setDefault(PreferenceConstants.WORKING_DIR, "D:\\OpenOffice 4\\dps\\converting");
		store.setDefault(PreferenceConstants.TEMPLATE_PROFILE_DIR, "D:\\OpenOffice 4\\dps\\template");
		store.setDefault(PreferenceConstants.PROCESS_TIMEOUT, 120000l);
		store.setDefault(PreferenceConstants.PROCESS_RETRY_INTERVAL, 250l);
		store.setDefault(PreferenceConstants.TASK_EXECUTION_TIMEOUT, 120000l);
		store.setDefault(PreferenceConstants.MAX_TASKS_PER_PROCESS, 200);
		store.setDefault(PreferenceConstants.TASK_QUEUE_TIMEOUT, 3000l);
	}

}
