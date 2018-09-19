package com.bizvpm.dps.processor.pm;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstacts.CONNECT_TIMEOUT, PreferenceConstacts.UNSET);
		store.setDefault(PreferenceConstacts.PORT, PreferenceConstacts.UNSET);
		store.setDefault(PreferenceConstacts.CONNECTIONS_PER_HOST, PreferenceConstacts.UNSET);
		store.setDefault(PreferenceConstacts.MAX_WAIT_TIME, PreferenceConstacts.UNSET);
		store.setDefault(PreferenceConstacts.SOCKET_TIMEOUT, PreferenceConstacts.UNSET);
		store.setDefault(PreferenceConstacts.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER,
				PreferenceConstacts.UNSET);
		store.setDefault(PreferenceConstacts.START_STANCELONE_WS, "http://[ip:port]/dpssl/pm");
		store.setDefault(PreferenceConstacts.PRJ_CATEGORY_FILE, "");
	}

}
