package com.bizvpm.dps.processor.pm;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class IntegerFieldEditor2 extends IntegerFieldEditor {

	private int unsetValue;

	public IntegerFieldEditor2(String name, String labelText, Composite parent, int unsetValue) {
		super(name, labelText, parent);
		this.unsetValue = unsetValue;
	}

	@Override
	protected void doLoadDefault() {
		Text text = getTextControl();
		if (text != null) {
			int value = getPreferenceStore().getDefaultInt(getPreferenceName());
			if (value == unsetValue) {
				text.setText("");
			} else {
				text.setText("" + value);//$NON-NLS-1$
			}
		}
		valueChanged();
	}
}
