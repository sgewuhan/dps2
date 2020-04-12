package com.awesometech.dps.processor.irobot.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.awesometech.dps.processor.irobot.Activator;


public class IRobotPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public IRobotPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("设置IRobot处理配置信息");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new StringFieldEditor(IRobotPreferenceConstants.IRobot_IP,
				"IRobot IP:", parent));
		addField(new StringFieldEditor(IRobotPreferenceConstants.IRobot_PORT,
				"IRobot PORT:", parent));
		addField(new StringFieldEditor(IRobotPreferenceConstants.IRobot_USERNAME,
				"IRobot User Name:", parent));
		addField(new StringFieldEditor(IRobotPreferenceConstants.IRobot_USERPWD,
				"IRobot User Password:", parent));
		addField(new IntegerFieldEditor(IRobotPreferenceConstants.IRobot_TIMEOUT,
				"IRobot Connect TimeOut:", parent));
		addField(new IntegerFieldEditor(IRobotPreferenceConstants.IRobot_JOBTIMEOUT,
				"IRobot JOB TimeOut:", parent));
		addField(new IntegerFieldEditor(IRobotPreferenceConstants.IRobot_MONITOR_INTERVAL,
				"IRobot JOB Monitor Interval:", parent));
		addField(new StringFieldEditor(IRobotPreferenceConstants.IRobot_WORK_PATH,
				"IRobot Preference Path:", parent));
		addField(new StringFieldEditor(IRobotPreferenceConstants.IRobot_QED_PATH,
				"IRobot Preference QED:", parent));
		addField(new StringFieldEditor(IRobotPreferenceConstants.DB_IP, "DB IP", parent));
		addField(new IntegerFieldEditor(IRobotPreferenceConstants.DB_PORT, "DB Port", parent, IRobotPreferenceConstants.DB_UNSET));
		addField(new StringFieldEditor(IRobotPreferenceConstants.DB, "DB Name", parent));

		addField(new IntegerFieldEditor(IRobotPreferenceConstants.DB_CONNECT_TIMEOUT, "DB Connect Timeout", parent,
				IRobotPreferenceConstants.DB_UNSET));
		addField(new IntegerFieldEditor(IRobotPreferenceConstants.DB_CONNECTIONS_PER_HOST, "DB Connections per Host", parent,
				IRobotPreferenceConstants.DB_UNSET));
		addField(new IntegerFieldEditor(IRobotPreferenceConstants.DB_MAX_WAIT_TIME, "DB Max Wait Time", parent,
				IRobotPreferenceConstants.DB_UNSET));
		addField(new IntegerFieldEditor(IRobotPreferenceConstants.DB_SOCKET_TIMEOUT, "DB Socket Timeout", parent,
				IRobotPreferenceConstants.DB_UNSET));
		addField(new IntegerFieldEditor(IRobotPreferenceConstants.DB_THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER,
				"DB Allows Threads Per Connections", parent, IRobotPreferenceConstants.DB_UNSET));
		addField(new StringFieldEditor(IRobotPreferenceConstants.PDM_IP,
				"PDM IP:", parent));
		addField(new StringFieldEditor(IRobotPreferenceConstants.PDM_PORT,
				"PDM PORT:", parent));
		addField(new StringFieldEditor(IRobotPreferenceConstants.PDM_DOMAIN,
				"PDM DOMAIN:", parent));
		addField(new BooleanFieldEditor(IRobotPreferenceConstants.MOCKUP,
				"MOCKUP:", parent));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
