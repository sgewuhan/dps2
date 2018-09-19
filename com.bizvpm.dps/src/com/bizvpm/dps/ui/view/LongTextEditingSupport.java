package com.bizvpm.dps.ui.view;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Table;

public class LongTextEditingSupport extends EditingSupport {
	private CellEditor editor;
	private ColumnLabelProvider labelProvider;

	public LongTextEditingSupport(TableViewer viewer, ColumnLabelProvider labelProvider) {
		super(viewer);
		this.labelProvider = labelProvider;
	}

	public LongTextEditingSupport(TableViewer viewer) {
		super(viewer);
	}

	@Override
	protected boolean canEdit(Object element) {
		return editor != null;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected void setValue(Object element, Object value) {
		doSetValue(element, value);
		getViewer().update(element, null);
	}

	protected void doSetValue(Object element, Object value){
		
	};
	
	
	public void addToColumn(TableViewerColumn column) {
		Table table = column.getColumn().getParent();
		TextAndDialogCellEditor cellEditor = new TextAndDialogCellEditor(table);
		cellEditor.setEditable(false);
		cellEditor.setDialogTitle(column.getColumn().getText());
		this.editor = cellEditor;
		column.setEditingSupport(this);
	}

	@Override
	protected Object getValue(Object element) {
		if(labelProvider!=null){
			return labelProvider.getText(element);
		}
		return null;
	}
}