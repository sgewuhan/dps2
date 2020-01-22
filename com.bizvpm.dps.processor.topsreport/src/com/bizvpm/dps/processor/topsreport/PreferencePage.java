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
		setDescription("TOPS����ת��������\n" + "TOPS Server��ַ�����ڴ˴�����TOPS����˵�ַ���磺http://rs.wisplanner.com:9158/services\n"
				+ "Wordģ���ļ�����ѡ��Word��ģ���ļ���*.dotx��");
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
		StringFieldEditor stringEditor = new StringFieldEditor(PreferenceConstacts.IP, "TOPS Server��ַ", parent);
		stringEditor.setEmptyStringAllowed(false);
		stringEditor.setErrorMessage("����дTOPS Server��ַ�������޷�����TOPS����ת����");
		addField(stringEditor);

		FileFieldEditor fileEditor = new FileFieldEditor(PreferenceConstacts.TEMPLATEPATH, "Wordģ���ļ�", parent);
		fileEditor.setFileExtensions(new String[] { "*.dotx" });
		fileEditor.setEmptyStringAllowed(false);
		fileEditor.setErrorMessage("��ѡ��Wordģ���ļ��������޷�����TOPS����ת����");
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