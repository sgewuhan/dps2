package com.bizvpm.dps.ui.view;

import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bizvpm.dps.Activator;
import com.bizvpm.dps.service.DataObject;
import com.bizvpm.dps.service.DataObjectConverter;
import com.bizvpm.dps.service.IPersistence;
import com.bizvpm.dps.service.KeyValuePair;
import com.bizvpm.dps.service.Parameter;
import com.bizvpm.dps.service.ParameterHelper;
import com.bizvpm.dps.service.ParameterList;
import com.bizvpm.dps.service.PersistableTask;
import com.bizvpm.dps.service.ProcessorConfig;
import com.bizvpm.dps.service.StringList;
import com.bizvpm.dps.service.Task;
import com.bizvpm.dps.service.TaskList;

public class ProcessorTasksView extends ViewPart implements ISelectionListener, IRefreshable, IRemoveable {

	private static final int UNKNOWN = 9;
	private static final int INFO = 2;
	private static final int WARNING = 1;
	private static final int ERROR = 0;
	private TableViewer taskList;
	private ProcessorConfig selection;
	private TableViewer inputList;
	private TableViewer outputList;

	public ProcessorTasksView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		SashForm sf = new SashForm(parent, SWT.HORIZONTAL);
		createTaskList(sf);
		createTaskInputOutputList(sf);
		taskList.addPostSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection == null || selection.isEmpty()) {
					selectTask(null);
				} else {
					Object object = ((IStructuredSelection) selection).getFirstElement();
					if (object instanceof PersistableTask) {
						selectTask((PersistableTask) object);
					}
				}

			}

			private void selectTask(PersistableTask task) {
				if (task == null) {
					inputList.setInput(new Object[0]);
					outputList.setInput(new Object[0]);
				} else {
					inputList.setInput(task.getValues());
					outputList.setInput(task.getResult());
				}
			}
		});
		;
		sf.setWeights(new int[] { 3, 1 });
		getViewSite().getPage().addPostSelectionListener("dps.processors", this);
		getSite().setSelectionProvider(taskList);
	}

	private Control createTaskList(Composite parent) {
		taskList = new TableViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		taskList.getTable().setLinesVisible(true);
		taskList.getTable().setHeaderVisible(true);
		taskList.setContentProvider(ArrayContentProvider.getInstance());
		// 1. name
		ColumnLabelProvider labelProvider = new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				String status = ((PersistableTask) element).getStatus();
				if ("received".equals(status)) {
					return Activator.getImage("work_16.gif");
				} else if ("error".equals(status)) {
					return Activator.getImage("work_cancel_16.gif");
				} else if ("done".equals(status)) {
					return Activator.getImage("work_finish_16.png");
				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				return ((PersistableTask) element).getName();
			}
		};
		TableViewerColumn 
		column = createColumn(taskList, SWT.LEFT, labelProvider);
		column.getColumn().setText("任务名称");
		column.getColumn().setWidth(240);

		// 2.状态
		labelProvider = new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				return ((PersistableTask) element).getStatus();
			}
		};
		column = createColumn(taskList, SWT.LEFT, labelProvider);
		column.getColumn().setText("状态");
		column.getColumn().setWidth(48);

		// 3.优先级
		labelProvider = new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				return "" + ((PersistableTask) element).getPriority();
			}
		};
		column = createColumn(taskList, SWT.RIGHT, labelProvider);
		column.getColumn().setText("优先级");
		column.getColumn().setWidth(48);

		// 4.
		labelProvider = new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				XMLGregorianCalendar receivedDate = ((PersistableTask) element).getReceivedDate();
				if (receivedDate != null) {
					return String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS", receivedDate.toGregorianCalendar());
				} else {
					return "";
				}
			}
		};
		column = createColumn(taskList, SWT.LEFT, labelProvider);
		column.getColumn().setText("接收");
		column.getColumn().setWidth(128);

		labelProvider = new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				XMLGregorianCalendar date = ((PersistableTask) element).getDoneDate();
				if (date != null) {
					return String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS", date.toGregorianCalendar());
				} else {
					date = ((PersistableTask) element).getErrorDate();
					if (date != null) {
						return String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS", date.toGregorianCalendar());
					} else {
						return "";
					}
				}
			}
		};
		column = createColumn(taskList, SWT.LEFT, labelProvider);
		column.getColumn().setText("完成/中止");
		column.getColumn().setWidth(128);

		labelProvider = new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				XMLGregorianCalendar date = ((PersistableTask) element).getDoneDate();
				if (date == null) {
					date = ((PersistableTask) element).getErrorDate();
				}
				if (date == null) {
					return "";
				}
				long finished = date.toGregorianCalendar().getTimeInMillis();

				date = ((PersistableTask) element).getReceivedDate();
				if (date == null) {
					return "";
				}

				long received = date.toGregorianCalendar().getTimeInMillis();

				return "" + (finished - received);
			}
		};
		column = createColumn(taskList, SWT.RIGHT, labelProvider);
		column.getColumn().setText("历时");
		column.getColumn().setWidth(60);

		labelProvider = new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				String text = ((PersistableTask) element).getClientIp();
				return text == null ? "" : text;
			}
		};
		column = createColumn(taskList, SWT.LEFT, labelProvider);
		column.getColumn().setText("请求者IP");
		column.getColumn().setWidth(80);

		labelProvider = new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				String text = ((PersistableTask) element).getClientName();
				return text == null ? "" : text;
			}
		};
		column = createColumn(taskList, SWT.LEFT, labelProvider);
		column.getColumn().setText("请求者主机");
		column.getColumn().setWidth(90);

		labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String text = ((PersistableTask) element).getMessage();
				return text == null ? "" : text;
			}
		};
		column = createColumn(taskList, SWT.LEFT, labelProvider);
		column.getColumn().setText("消息");
		column.getColumn().setWidth(240);
		new LongTextEditingSupport(taskList, labelProvider).addToColumn(column);

		Table table = taskList.getTable();
		return table;
	}

	private TableViewerColumn createColumn(TableViewer table, int style, ColumnLabelProvider labelProvider) {
		TableViewerColumn column = new TableViewerColumn(table, style);
		column.getColumn().setMoveable(true);
		column.getColumn().setResizable(true);
		column.setLabelProvider(labelProvider);
		new ColumnViewerComparator(table, column, labelProvider);
		return column;
	}

	@Override
	public void setFocus() {
		taskList.getControl().setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection == null || selection.isEmpty()) {
			setSelection(null);
		} else {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement instanceof ProcessorConfig) {
				ProcessorConfig pc = (ProcessorConfig) firstElement;
				setSelection(pc);
			} else {
				setSelection(null);
			}
		}
	}

	private void setSelection(ProcessorConfig pc) {
		this.selection = pc;
		if (pc == null) {
			taskList.setInput(new Object[0]);
		} else {
			IPersistence server = Activator.getServer();
			TaskList input = server.getTaskListByProcessorTypeId(Activator.getHostName(), pc.getId());
			taskList.setInput(input.getTasks());
		}
	}

	@Override
	public void refresh() {
		setSelection(selection);
	}

	@Override
	public void removeSelection() {
		IStructuredSelection selection = (IStructuredSelection) taskList.getSelection();
		if (selection == null || selection.isEmpty()) {
			return;
		}
		Iterator<?> iter = selection.iterator();
		StringList taskIdList = new StringList();
		while (iter.hasNext()) {
			Task task = (Task) iter.next();
			taskIdList.getItems().add(task.getId());
		}
		Activator.getServer().removeTaskList(taskIdList);
		refresh();
	}

	@Override
	public void clean() {
		if (selection == null) {
			return;
		}
		String id = selection.getId();
		Activator.getServer().removeTaskListByProcessorTypeId(id);
		refresh();
	}

	private void createTaskInputOutputList(Composite parent) {
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText("输入参数");
		inputList = createDataSetTable(folder);
		item.setControl(inputList.getControl());

		item = new CTabItem(folder, SWT.NONE);
		item.setText("输出结果");
		outputList = createDataSetTable(folder);
		item.setControl(outputList.getControl());

		folder.setSelection(0);
	}

	private TableViewer createDataSetTable(Composite parent) {
		TableViewer taskDataList = new TableViewer(parent, SWT.FULL_SELECTION);
		taskDataList.getTable().setHeaderVisible(true);
		taskDataList.getTable().setLinesVisible(true);
		taskDataList.setContentProvider(ArrayContentProvider.getInstance());

		ColumnLabelProvider labelProvider = new ColumnLabelProvider() {

			@Override
			public Image getImage(Object element) {
				int i = checkParameter(((KeyValuePair) element));
				if (i == ERROR) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				} else if (i == WARNING) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				} else {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
				}
			}

			@Override
			public String getText(Object element) {
				return ((KeyValuePair) element).getKey();
			}
		};
		TableViewerColumn 
		column = createColumn(taskDataList, SWT.LEFT, labelProvider);
		column.getColumn().setWidth(80);
		column.getColumn().setText("属性");

		labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DataObject data = ((KeyValuePair) element).getValue();
				DataObjectConverter cov = new DataObjectConverter();
				Object value = cov.getValue(data);
				if (value instanceof DataHandler) {
					return "[" + ((DataHandler) value).getContentType() + "]";
				} else {
					return "" + value;
				}
			}
		};
		column = createColumn(taskDataList, SWT.LEFT, labelProvider);
		column.getColumn().setWidth(160);
		column.getColumn().setText("值");

		labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DataObject data = ((KeyValuePair) element).getValue();
				DataObjectConverter cov = new DataObjectConverter();
				Object value = cov.getValue(data);
				return value == null ? "" : value.getClass().getSimpleName();
			}
		};
		column = createColumn(taskDataList, SWT.LEFT, labelProvider);
		column.getColumn().setWidth(80);
		column.getColumn().setText("类型");

		return taskDataList;
	}

	protected int checkParameter(KeyValuePair keyValuePair) {
		if (selection == null) {
			return UNKNOWN;
		}
		ParameterList list = selection.getParameterList();
		if (list == null || list.getParameters() == null || list.getParameters().isEmpty()) {
			return UNKNOWN;
		}

		List<Parameter> paras = list.getParameters();
		for (int i = 0; i < paras.size(); i++) {
			Parameter para = paras.get(i);
			if (para.getName().equals(keyValuePair.getKey())) {
				boolean b = ParameterHelper.isValid(para, keyValuePair);
				if (b) {
					return INFO;
				} else {
					return ERROR;
				}
			}
		}
		return WARNING;
	}

	@Override
	public void refresh(Object object) {
		taskList.refresh(object);
	}

}
