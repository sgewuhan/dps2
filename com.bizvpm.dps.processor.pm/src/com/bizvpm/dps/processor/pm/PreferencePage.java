package com.bizvpm.dps.processor.pm;

import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.bizvpm.dps.processor.pm.tools.AnalysisExcel;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndReplaceOptions;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("BizVPM����Դ");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite panel = (Composite) super.createContents(parent);
		Button button = new Button(panel, SWT.PUSH);
		button.setText("��������WebService");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Activator.getDefault().startStandloneWebSerivce();
			}
		});
		return panel;
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new StringFieldEditor(PreferenceConstacts.IP, "IP��ַ", parent));
		addField(new IntegerFieldEditor2(PreferenceConstacts.PORT, "�˿ں�", parent, PreferenceConstacts.UNSET));
		addField(new StringFieldEditor(PreferenceConstacts.DB, "���ݿ�����", parent));

		addField(new IntegerFieldEditor2(PreferenceConstacts.CONNECT_TIMEOUT, "���ӳ�ʱ(����)", parent,
				PreferenceConstacts.UNSET));
		addField(new IntegerFieldEditor2(PreferenceConstacts.CONNECTIONS_PER_HOST, "����������", parent,
				PreferenceConstacts.UNSET));
		addField(new IntegerFieldEditor2(PreferenceConstacts.MAX_WAIT_TIME, "��ȴ�ʱ��(����)", parent,
				PreferenceConstacts.UNSET));
		addField(new IntegerFieldEditor2(PreferenceConstacts.SOCKET_TIMEOUT, "�׽��ֳ�ʱ((����))", parent,
				PreferenceConstacts.UNSET));
		addField(new IntegerFieldEditor2(PreferenceConstacts.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER,
				"ÿ��������߳���", parent, PreferenceConstacts.UNSET));

		addField(new BooleanFieldEditor(PreferenceConstacts.START_STANCELONE_WS, "��������WebSerivce", parent));
		addField(new StringFieldEditor(PreferenceConstacts.STAND_WS_URL, "����WebSerivce URL", parent));
		FileFieldEditor editor = new FileFieldEditor(PreferenceConstacts.PRJ_CATEGORY_FILE, "��Ŀ�����Ӧ��", true,
				StringButtonFieldEditor.VALIDATE_ON_FOCUS_LOST, parent);
		editor.setFileExtensions(new String[] { "*.xls", "*.xlsx" });
		editor.setErrorMessage("ѡ��Ĳ���һ�����õ�Excel�ļ�");
		editor.setEmptyStringAllowed(true);
		addField(editor);
	}

	@Override
	public boolean performOk() {
		boolean isok = super.performOk();
		MongoDatabase db = Activator.getDefault().getDB();
		MongoCollection<BasicDBObject> collection = db.getCollection("projectcategory",BasicDBObject.class);
		IPreferenceStore preferenceStore = getPreferenceStore();
		String prjCategory = preferenceStore.getString(PreferenceConstacts.PRJ_CATEGORY_FILE);
		String md5 = preferenceStore.getString("fileMd5");
		try {
			List<BasicDBObject> analysisExcel = AnalysisExcel.analysisExcel(prjCategory, md5);
			for (BasicDBObject doc : analysisExcel) {
				Object object = doc.get("code");
				BasicDBObject append = new BasicDBObject().append("code", object);
				collection.findOneAndReplace(append, doc,new FindOneAndReplaceOptions().upsert(true));
			}
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isok;
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