package com.bizvpm.dps.ui.view;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;

public class ColumnViewerComparator extends ViewerComparator {

	public static final int ASC = 1;
	public static final int NONE = 0;
	public static final int DESC = -1;

	private int direction = 0;
	private TableViewerColumn column;
	private ColumnViewer viewer;
	private ColumnLabelProvider labelProvider;

	public ColumnViewerComparator(ColumnViewer viewer, TableViewerColumn column, ColumnLabelProvider labelProvider) {
		this.column = column;
		this.viewer = viewer;
		this.labelProvider = labelProvider;
		SelectionAdapter selectionAdapter = createSelectionAdapter();
		this.column.getColumn().addSelectionListener(selectionAdapter);
	}

	private SelectionAdapter createSelectionAdapter() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ColumnViewerComparator.this.viewer.getComparator() != null) {
					if (ColumnViewerComparator.this.viewer.getComparator() == ColumnViewerComparator.this) {
						int tdirection = ColumnViewerComparator.this.direction;
						if (tdirection == ASC) {
							setSorter(ColumnViewerComparator.this, DESC);
						} else if (tdirection == DESC) {
							setSorter(ColumnViewerComparator.this, NONE);
						}
					} else {
						setSorter(ColumnViewerComparator.this, ASC);
					}
				} else {
					setSorter(ColumnViewerComparator.this, ASC);
				}
			}
		};
	}

	public void setSorter(ColumnViewerComparator sorter, int direction) {
		Table columnParent = column.getColumn().getParent();
		if (direction == NONE) {
			columnParent.setSortColumn(null);
			columnParent.setSortDirection(SWT.NONE);
			viewer.setComparator(null);

		} else {
			columnParent.setSortColumn(column.getColumn());
			sorter.direction = direction;
			columnParent.setSortDirection(direction == ASC ? SWT.DOWN : SWT.UP);

			if (viewer.getComparator() == sorter) {
				viewer.refresh();
			} else {
				viewer.setComparator(sorter);
			}

		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return direction * doCompare(viewer, e1, e2);
	}

	protected int doCompare(Viewer viewer, Object e1, Object e2){
		String text1 = labelProvider.getText(e1);
		String text2 = labelProvider.getText(e2);
		//by number
		try {
			String _t1 = text1.replaceAll(",", "");
			String _t2 = text2.replaceAll(",", "");
			double d1 = Double.parseDouble(_t1);
			double d2 = Double.parseDouble(_t2);
			return Double.compare(d1, d2);
		} catch (Exception e) {
		}
		
		return text1.compareTo(text2);
		
	}
}
