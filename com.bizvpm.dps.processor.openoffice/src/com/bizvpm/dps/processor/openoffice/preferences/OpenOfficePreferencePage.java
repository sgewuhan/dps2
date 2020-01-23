package com.bizvpm.dps.processor.openoffice.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.bizvpm.dps.processor.openoffice.Activator;

import org.eclipse.ui.IWorkbench;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class OpenOfficePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public OpenOfficePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Open Office转换设置");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 * 
	 * public static final String OFFICE_HOME = "officeHome"; public static
	 * final String PORT_NUMBERS = "portNumbers"; public static final String
	 * WORKING_DIR = "workingDir"; public static final String
	 * TEMPLATE_PROFILE_DIR = "templateProfileDir"; public static final String
	 * PROCESS_TIMEOUT = "processTimeout"; public static final String
	 * PROCESS_RETRY_INTERVAL = "processRetryInterval"; public static final
	 * String TASK_EXECUTION_TIMEOUT = "taskExecutionTimeout"; public static
	 * final String MAX_TASKS_PER_PROCESS = "maxTasksPerProcess"; public static
	 * final String TASK_QUEUE_TIMEOUT = "taskQueueTimeout";
	 * 
	 */
	public void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceConstants.OFFICE_HOME_BIN, "选择Open Office soffice.bin文件:",
				getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PORT_NUMBERS, "端口号(空格分多个):", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.WORKING_DIR, "工作目录:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.TEMPLATE_PROFILE_DIR, "模板文件目录:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.PROCESS_TIMEOUT, "进程超时(毫秒，下同):", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.PROCESS_RETRY_INTERVAL, "进程重试间隔:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.TASK_EXECUTION_TIMEOUT, "任务执行超时:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.MAX_TASKS_PER_PROCESS, "每进程任务数:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.TASK_QUEUE_TIMEOUT, "任务队列超时:", getFieldEditorParent()));
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