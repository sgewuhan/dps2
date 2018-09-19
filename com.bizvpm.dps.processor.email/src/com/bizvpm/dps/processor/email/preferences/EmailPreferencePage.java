package com.bizvpm.dps.processor.email.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import com.bizvpm.dps.processor.email.Activator;
import com.bizvpm.dps.runtime.ui.StringFieldEditor2;

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

public class EmailPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public EmailPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("邮件服务器设置");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new StringFieldEditor(EMailPreferenceConstants.EMAIL_HOSTNAME, "发送邮件服务器(SMTP)", parent));
		addField(new BooleanFieldEditor(EMailPreferenceConstants.EMAIL_SSLONCONNECT, "使用SSL连接服务器", parent));
		addField(new IntegerFieldEditor(EMailPreferenceConstants.EMAIL_SMTPPORT, "端口号", parent));
		addField(new StringFieldEditor(EMailPreferenceConstants.EMAIL_AUTHUSER, "帐户", parent));
		StringFieldEditor2 editor = new StringFieldEditor2(
				EMailPreferenceConstants.EMAIL_AUTHPASS, "密码",
				StringFieldEditor2.UNLIMITED,
				StringFieldEditor2.VALIDATE_ON_FOCUS_LOST,
				SWT.BORDER|SWT.PASSWORD,
				parent);
		addField(editor);
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