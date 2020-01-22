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
		setDescription("TMT SAP �ӿ�����\n" + "����дSAP������Ϣ");
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
		IntegerFieldEditor intEditor = new IntegerFieldEditor(PreferenceConstacts.S_EAI_SAP_MAXCONN, "SAP�ͻ������������",
				parent);
		intEditor.setEmptyStringAllowed(false);
		addField(intEditor);

		StringFieldEditor stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_CLIENT, "SAP�ͻ�������",
				parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_USERID, "SAP�ͻ����û�ID", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_PASSWORD, "SAP�û���¼����", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_LANGUAGE, "SAP��������", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_HOST, "SAP����IP", parent);
		stringEditor.setEmptyStringAllowed(false);
		addField(stringEditor);

		stringEditor = new StringFieldEditor(PreferenceConstacts.S_EAI_SAP_INSTANCENUMBER, "SAPʵ�����", parent);
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