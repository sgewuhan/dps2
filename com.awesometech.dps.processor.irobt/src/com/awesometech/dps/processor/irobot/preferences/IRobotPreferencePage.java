package com.awesometech.dps.processor.irobot.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
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
		addField(new StringFieldEditor(IRobotPreferenceConstants.URL,
				"URL:", getFieldEditorParent()));
		addField(new StringFieldEditor(IRobotPreferenceConstants.USERNAME,
				"UserName:", getFieldEditorParent()));
		addField(new StringFieldEditor(IRobotPreferenceConstants.USERPWD,
				"UserPassword:", getFieldEditorParent()));
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
