package com.bizvpm.dps.ui.view;

import java.util.Dictionary;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import com.bizvpm.dps.service.Parameter;
import com.bizvpm.dps.service.ParameterList;
import com.bizvpm.dps.service.ProcessorConfig;

public class ProcessorParametersView extends ViewPart implements ISelectionListener {

	private TableViewer viewer;

	private Label processorNameLable;
	private Font titleFont;
	private Font detailFont;
	private Text idText;
	private Text bundleText;
	private Text limitText;

	private Label limitLabel;

	private Label bundlerLabel;

	private Label idLabel;

	private Composite infoPane;

	private Browser descriptionText;

	public ProcessorParametersView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText("处理器信息");
		Control control = createProcessPage(folder);
		item.setControl(control);

		item = new CTabItem(folder, SWT.NONE);
		item.setText("说明");
		control = createProcessDescription(folder);
		item.setControl(control);

		folder.setSelection(0);

		getViewSite().getPage().addPostSelectionListener("dps.processors", this);

	}

	private Control createProcessDescription(Composite parent) {
		descriptionText = new Browser(parent, SWT.NONE);
		return descriptionText;
	}

	public Composite createProcessPage(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		composite.setLayout(new FormLayout());
		infoPane = createInformationPanel(composite);
		FormData fd = new FormData();
		infoPane.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.top = new FormAttachment();
		Control table = createTable(composite);
		fd = new FormData();
		table.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.top = new FormAttachment(infoPane, 10);
		fd.bottom = new FormAttachment(100);
		return composite;
	}

	private Control createTable(Composite parent) {
		viewer = new TableViewer(parent, SWT.FULL_SELECTION);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		// 1. name
		ColumnLabelProvider labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Parameter) element).getName();
			}
		};
		TableViewerColumn column = createColumn(viewer, SWT.LEFT, labelProvider);
		column.getColumn().setText("参数名称");
		column.getColumn().setWidth(120);

		labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Parameter) element).getType();
			}
		};
		column = createColumn(viewer, SWT.LEFT, labelProvider);
		column.getColumn().setText("参数类型");
		column.getColumn().setWidth(120);

		labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "" + ((Parameter) element).isOptional();
			}
		};
		column = createColumn(viewer, SWT.LEFT, labelProvider);
		column.getColumn().setText("可选");
		column.getColumn().setWidth(120);

		labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				List<Object> rest = ((Parameter) element).getRestrictions();
				if (rest != null && !rest.isEmpty()) {
					return rest.toString();
				}
				return "";
			}
		};
		column = createColumn(viewer, SWT.LEFT, labelProvider);
		column.getColumn().setText("限定范围");
		column.getColumn().setWidth(300);

		new LongTextEditingSupport(viewer) {
			protected Object getValue(Object element) {
				String result = "";
				List<Object> rest = ((Parameter) element).getRestrictions();
				for (int i = 0; i < rest.size(); i++) {
					result += rest.get(i) + "\n";
				}
				return result;
			}
		}.addToColumn(column);

		labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String desc = ((Parameter) element).getDescription();
				if (desc != null) {
					return desc;
				}
				return "";
			}
		};
		column = createColumn(viewer, SWT.LEFT, labelProvider);
		column.getColumn().setText("说明");
		column.getColumn().setWidth(300);

		new LongTextEditingSupport(viewer, labelProvider).addToColumn(column);
		return viewer.getControl();
	}

	private TableViewerColumn createColumn(TableViewer table, int style, ColumnLabelProvider labelProvider) {
		TableViewerColumn column = new TableViewerColumn(table, style);
		column.getColumn().setMoveable(true);
		column.getColumn().setResizable(true);
		column.setLabelProvider(labelProvider);
		new ColumnViewerComparator(table, column, labelProvider);
		return column;
	}

	private Composite createInformationPanel(Composite parent) {
		titleFont = new Font(parent.getDisplay(), "微软雅黑", 16, SWT.NORMAL);
		detailFont = new Font(parent.getDisplay(), "微软雅黑", 9, SWT.NORMAL);

		Composite pane = new Composite(parent, SWT.NONE);
		pane.setLayout(new FormLayout());
		FormData fd;

		processorNameLable = new Label(pane, SWT.NONE);
		processorNameLable.setFont(titleFont);
		processorNameLable.setForeground(pane.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));

		fd = new FormData();
		processorNameLable.setLayoutData(fd);
		fd.top = new FormAttachment(0, 10);
		fd.left = new FormAttachment(0, 10);
		fd.right = new FormAttachment(100, -10);

		idLabel = createLabel(pane, "");
		fd = new FormData();
		idLabel.setLayoutData(fd);
		fd.top = new FormAttachment(processorNameLable, 10);
		fd.left = new FormAttachment(0, 10);

		idText = createText(pane);
		fd = new FormData();
		idText.setLayoutData(fd);
		fd.top = new FormAttachment(processorNameLable, 10);
		fd.left = new FormAttachment(idLabel, 10);
		fd.right = new FormAttachment(100, -10);

		bundlerLabel = createLabel(pane, "");
		fd = new FormData();
		bundlerLabel.setLayoutData(fd);
		fd.top = new FormAttachment(idText);
		fd.left = new FormAttachment(0, 10);

		bundleText = createText(pane);
		fd = new FormData();
		bundleText.setLayoutData(fd);
		fd.top = new FormAttachment(idText);
		fd.left = new FormAttachment(bundlerLabel, 10);
		fd.right = new FormAttachment(100, -10);

		limitLabel = createLabel(pane, "");
		fd = new FormData();
		limitLabel.setLayoutData(fd);
		fd.top = new FormAttachment(bundleText);
		fd.left = new FormAttachment(0, 10);

		limitText = createText(pane);
		fd = new FormData();
		limitText.setLayoutData(fd);
		fd.top = new FormAttachment(bundleText);
		fd.left = new FormAttachment(limitLabel, 10);
		fd.right = new FormAttachment(100, -10);

		return pane;
	}

	private Text createText(Composite pane) {
		Text text = new Text(pane, SWT.NONE);
		text.setEditable(false);
		text.setForeground(pane.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		text.setFont(detailFont);
		text.setBackground(pane.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		return text;
	}

	private Label createLabel(Composite pane, String text) {
		Label label = new Label(pane, SWT.NONE);
		label.setText(text);
		label.setFont(detailFont);
		return label;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection == null || selection.isEmpty()) {
			cleanInfo();
		} else {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement instanceof ProcessorConfig) {
				ProcessorConfig pc = (ProcessorConfig) firstElement;
				ParameterList parameterList = pc.getParameterList();
				if (parameterList != null) {
					viewer.setInput(parameterList.getParameters());
				} else {
					viewer.setInput(new Object[0]);
				}
				processorNameLable.setText(pc.getName());

				Bundle bundle = Platform.getBundle(pc.getPlugId());
				Dictionary<String, String> header = bundle.getHeaders();
				idText.setText(pc.getId());
				bundleText.setText(pc.getPlugId() + " Version: " + bundle.getVersion() + " Bundle-Vendor: "
						+ header.get("Bundle-Vendor"));
				limitText.setText("" + pc.getMaxThreadCount());
				idLabel.setText("ID:");
				bundlerLabel.setText("Bundler:");
				limitLabel.setText("Concurrent Limit:");
				infoPane.layout();
				String descritpion = pc.getDescritpion();
				descritpion = descritpion == null ? "" : descritpion;
				descriptionText.setText(descritpion);
			} else {
				cleanInfo();
			}
		}
	}

	private void cleanInfo() {
		viewer.setInput(new Object[0]);
		processorNameLable.setText("");
		idText.setText("");
		bundleText.setText("");
		limitText.setText("");
		idLabel.setText("");
		bundlerLabel.setText("");
		limitLabel.setText("");
		infoPane.layout();
		descriptionText.setText("");
	}

	@Override
	public void dispose() {
		titleFont.dispose();
		detailFont.dispose();
		super.dispose();
	}
}
