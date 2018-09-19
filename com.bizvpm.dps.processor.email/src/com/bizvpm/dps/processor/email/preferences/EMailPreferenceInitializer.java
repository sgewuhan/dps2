package com.bizvpm.dps.processor.email.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.bizvpm.dps.processor.email.Activator;

/**
 * Class used to initialize default preference values.
 */
public class EMailPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(EMailPreferenceConstants.EMAIL_SMTPPORT, 465);
	}

}
