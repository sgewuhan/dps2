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
		store.setDefault(IRobotPreferenceConstants.IRobot_IP, "192.168.0.100");
		store.setDefault(IRobotPreferenceConstants.IRobot_PORT, "8080");
		store.setDefault(IRobotPreferenceConstants.IRobot_USERNAME, "admin");
		store.setDefault(IRobotPreferenceConstants.IRobot_USERPWD, "");
		store.setDefault(IRobotPreferenceConstants.IRobot_TIMEOUT, 3000);
		store.setDefault(IRobotPreferenceConstants.IRobot_JOBTIMEOUT, 600000);  // 默认Job超时设置为10分钟
		store.setDefault(IRobotPreferenceConstants.IRobot_MONITOR_INTERVAL, 60);
		store.setDefault(IRobotPreferenceConstants.IRobot_WORK_PATH, "D:\\Data\\I8Work");
		store.setDefault(IRobotPreferenceConstants.IRobot_QED_PATH, "D:\\Data\\I8export\\qedxml\\%pid%.xml");
		store.setDefault(IRobotPreferenceConstants.DB_CONNECT_TIMEOUT, 10000);
		store.setDefault(IRobotPreferenceConstants.DB_IP, "127.0.0.1");
		store.setDefault(IRobotPreferenceConstants.DB_PORT, 10001);
		store.setDefault(IRobotPreferenceConstants.DB, "dps");
		store.setDefault(IRobotPreferenceConstants.DB_CONNECTIONS_PER_HOST, 10);
		store.setDefault(IRobotPreferenceConstants.DB_MAX_WAIT_TIME, 120000);
		store.setDefault(IRobotPreferenceConstants.DB_SOCKET_TIMEOUT, 0);
		store.setDefault(IRobotPreferenceConstants.DB_THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER,
				5);
		store.setDefault(IRobotPreferenceConstants.PDM_IP, "127.0.0.1");
		store.setDefault(IRobotPreferenceConstants.PDM_PORT, "9158");
		store.setDefault(IRobotPreferenceConstants.PDM_DOMAIN, "MYDL_1E2TAH2FA");
	}

}
