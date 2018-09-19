package com.bizvpm.dps.ui.view;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.bizvpm.dps.Activator;
import com.bizvpm.dps.service.PersistableProcessor;
import com.bizvpm.dps.service.ProcessorConfig;
import com.bizvpm.dps.service.ProcessorManager;

public class ProcessorsView extends ViewPart implements IRefreshable {

	// private TableViewer viewer;

	private TreeViewer viewer;

	public ProcessorsView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ProcessorCatalogContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof ProcessorConfig) {
					if (((ProcessorConfig) element).isOnline()) {
						return Activator.getImage("server_16_on.png");
					} else {
						return Activator.getImage("server_16_off.png");
					}
				}else if(element instanceof ProcessorCatalog){
					return Activator.getImage("folder_16.png");
				}
				return null;
			}

			public String getText(Object element) {
				if(element instanceof ProcessorConfig){
					return ((ProcessorConfig) element).getName();
				}else {
					return element.toString();
				}
			}
		});

		getSite().setSelectionProvider(viewer);
		load();
		viewer.expandAll();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void load() {
		ProcessorManager manager = Activator.getDefault().getProcessorManager();
		List<PersistableProcessor> processors = manager.getProcessorConfigs();
		ProcessorCatalog root = new ProcessorCatalog();
		for (PersistableProcessor processor : processors) {
			String path = ((ProcessorConfig) processor).getCatalog();
			root.addProcessor((ProcessorConfig) processor,path);
		}
		viewer.setInput(root);
	}

	@Override
	public void refresh() {
		viewer.refresh();
	}

	@Override
	public void refresh(Object object) {
		viewer.refresh(object);
		viewer.setSelection(new StructuredSelection(object));
	}

}
