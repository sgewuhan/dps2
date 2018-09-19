package com.tmt.dps.processor.sms.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.tmt.dps.processor.sms.Activator;

public class TMTSmsPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(TMTSmsPreferenceConstants.USERWEBSERVICE,
				Boolean.TRUE);
		store.setDefault(TMTSmsPreferenceConstants.PORT, 1433);
		store.setDefault(TMTSmsPreferenceConstants.MAXCONNECTION, 20);
	}

}
