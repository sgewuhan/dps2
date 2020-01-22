package com.bizvpm.dps.processor.topsreport;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("TOPS报表转换器设置\n" + "TOPS Server地址：请在此处配置TOPS服务端地址。如：http://rs.wisplanner.com:9158/services\n"
				+ "Word模板文件：请选择Word的模板文件（*.dotx）");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite panel = (Composite) super.createContents(parent);
		return panel;
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI
	 * blocks needed to manipulate various types of preferences. Each field editor
	 * knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		StringFieldEditor stringEditor = new StringFieldEditor(PreferenceConstacts.IP, "TOPS Server地址", parent);
		stringEditor.setEmptyStringAllowed(false);
		stringEditor.setErrorMessage("请填写TOPS Server地址，否则无法启动TOPS报表转换器");
		addField(stringEditor);

		FileFieldEditor fileEditor = new FileFieldEditor(PreferenceConstacts.TEMPLATEPATH, "Word模板文件", parent);
		fileEditor.setFileExtensions(new String[] { "*.dotx" });
		fileEditor.setEmptyStringAllowed(false);
		fileEditor.setErrorMessage("请选择Word模板文件，否则无法启动TOPS报表转换器");
		addField(fileEditor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}