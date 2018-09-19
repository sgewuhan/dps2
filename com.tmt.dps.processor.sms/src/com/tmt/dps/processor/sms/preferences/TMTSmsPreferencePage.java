package com.tmt.dps.processor.sms.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import com.tmt.dps.processor.sms.Activator;

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

public class TMTSmsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public TMTSmsPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("…Ë÷√TMT∂Ã–≈≈‰÷√");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(
				new BooleanFieldEditor(TMTSmsPreferenceConstants.USERWEBSERVICE,
						"user WebService:", getFieldEditorParent()));
		addField(new StringFieldEditor(TMTSmsPreferenceConstants.WEBSERVICEWSDL,
				"WebService WSDL:", getFieldEditorParent()));
		addField(new StringFieldEditor(TMTSmsPreferenceConstants.WEBSERVICEUSER,
				"WebService User:", getFieldEditorParent()));
		addField(new StringFieldEditor(
				TMTSmsPreferenceConstants.WEBSERVICEPASSWORD,
				"WebService Password:", getFieldEditorParent()));
		addField(new StringFieldEditor(TMTSmsPreferenceConstants.IP,
				"DataBase IP:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(TMTSmsPreferenceConstants.PORT,
				"DataBase Port:", getFieldEditorParent()));
		addField(new StringFieldEditor(TMTSmsPreferenceConstants.DBUSER,
				"DataBase User:", getFieldEditorParent()));
		addField(new StringFieldEditor(TMTSmsPreferenceConstants.DBPASSWORD,
				"DataBase Password:", getFieldEditorParent()));
		addField(new StringFieldEditor(TMTSmsPreferenceConstants.NAME,
				"DataBase Name:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(TMTSmsPreferenceConstants.MAXCONNECTION,
				"DataBase Max Connection:", getFieldEditorParent()));
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