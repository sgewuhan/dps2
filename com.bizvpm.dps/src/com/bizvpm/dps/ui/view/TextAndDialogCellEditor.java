/*******************************************************************************
 * Copyright (c) 2006, 2014 Eric Rizzo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eric Rizzo - initial implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/
package com.bizvpm.dps.ui.view;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A CellEditor that is a blending of DialogCellEditor and TextCellEditor. The
 * user can either type directly into the Text or use the button to open a
 * Dialog for editing the cell's value.
 *
 */
public class TextAndDialogCellEditor extends DialogCellEditor {

	private Text textField;
	private String dialogMessage;
	private String dialogTitle;
	private boolean editable;

	public TextAndDialogCellEditor(Composite parent) {
		super(parent);
	}

	public void setDialogMessage(String dialogMessage) {
		this.dialogMessage = dialogMessage;
	}

	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@Override
	protected Control createContents(Composite cell) {
		textField = new Text(cell, SWT.NONE);
		textField.setEditable(editable);
		textField.setFont(cell.getFont());
		textField.setBackground(cell.getBackground());

		if (editable) {
			textField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent event) {
					setValueToModel();
				}
			});
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent event) {
					keyReleaseOccured(event);
				}
			});
		}

		return textField;
	}

	@Override
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.keyCode == SWT.CR || keyEvent.keyCode == SWT.KEYPAD_CR) {
			// Enter key
			setValueToModel();
		}
		super.keyReleaseOccured(keyEvent);
	}

	protected void setValueToModel() {
		String newValue = textField.getText();
		boolean newValidState = isCorrect(newValue);
		if (newValidState) {
			markDirty();
			doSetValue(newValue);
		} else {
			// try to insert the current value into the error message.
			setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { newValue.toString() }));
		}
	}

	@Override
	protected void updateContents(Object value) {
		if (textField == null) {
			return;
		}
		String text = "";
		if (value != null) {
			text = value.toString();
		}
		textField.setText(text);

	}

	@Override
	protected void doSetFocus() {
		// Overridden to set focus to the Text widget instead of the Button.
		textField.setFocus();
		textField.selectAll();
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		InputDialog dialog = new InputDialog(cellEditorWindow.getShell(), dialogTitle, dialogMessage,
				getDialogInitialValue(), null) {

			@Override
			protected Control createDialogArea(Composite parent) {
				Control control = super.createDialogArea(parent);
				GridData layoutData = new GridData(GridData.FILL_BOTH);
				layoutData.heightHint = 200;
				layoutData.widthHint = 400;
				getText().setLayoutData(layoutData);
				getText().setEditable(editable);
				return control;
			}

			protected int getInputTextStyle() {
				return SWT.MULTI | SWT.WRAP | SWT.BORDER;
			}

			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
				Text text = getText();
				text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				String value = getValue();
				if (value != null) {
					text.setText(value);
				}
			}

		};
		return (dialog.open() == Window.OK ? dialog.getValue() : null);
	}

	protected String getDialogInitialValue() {
		Object value = getValue();
		return (value == null ? null : value.toString());
	}
}
