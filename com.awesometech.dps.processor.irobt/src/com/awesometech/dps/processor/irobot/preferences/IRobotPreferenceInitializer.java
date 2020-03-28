package com.awesometech.dps.processor.irobot.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.awesometech.dps.processor.irobot.Activator;

/**
 * Class used to initialize default preference values.
 */
public class IRobotPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(IRobotPreferenceConstants.USERNAME, "admin");
		store.setDefault(IRobotPreferenceConstants.URL, "localhost");
		store.setDefault(IRobotPreferenceConstants.USERPWD, "");
	}

}
