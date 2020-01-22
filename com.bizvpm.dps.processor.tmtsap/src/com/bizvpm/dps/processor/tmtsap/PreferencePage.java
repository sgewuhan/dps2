package com.bizvpm.dps.processor.tmtsap;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("TMT SAP 接口设置\n" + "请填写SAP连接信息");
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
		IntegerFieldEditor intEditor = new IntegerFieldEditor(PreferenceConstacts.S_EAI_SAP_MAXCONN, "SAP客户端最大连接数",
				parent);
		intEditor.setEmptyStringAllowed(false);
		addField(intEditor);

		StringFieldEditor stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_CLIENT, "SAP客户端名称",
				parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_USERID, "SAP客户端用户ID", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_PASSWORD, "SAP用户登录密码", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_LANGUAGE, "SAP语言设置", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_HOST, "SAP主机IP", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_INSTANCENUMBER, "SAP实例编号", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}