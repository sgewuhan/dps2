package com.bizvpm.dps.ui.view;

import java.net.URL;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.bizvpm.dps.Activator;
import com.bizvpm.dps.service.IPersistence;
import com.bizvpm.dps.service.PersistenceService;

public class ServersView extends ViewPart {

	private class Server {

		private String address;
		private Long connectTime;
		private boolean isCurrent;

		public Server(String address) {
			this.address = address;
			String current = Activator.getDefault().getPreferenceStore()
					.getString("server");
			isCurrent = address.equals(current);
			long start = System.currentTimeMillis();
			try {
				String serverUrl = "http://" + address
						+ "/dps/persistence?wsdl";
				PersistenceService service;
				service = new PersistenceService(new URL(serverUrl));
				IPersistence persistence = service.getPort(IPersistence.class);
				persistence.ping();
				connectTime = System.currentTimeMillis() - start;
			} catch (Exception e) {
			}
		}

	}

	private TableViewer viewer;

	public ServersView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (((Server) element).connectTime == null) {
					return Activator.getImage("canceled_16.png");
				} else if (((Server) element).isCurrent) {
					return Activator.getImage("wip_16.png");
				} else {
					return Activator.getImage("canceled_16.png");
				}
			}

			@Override
			public String getText(Object element) {
				return ((Server) element).address
						+ " ["
						+ (((Server) element).connectTime == null ? "³¬Ê±"
								: ((Server) element).connectTime + "ms")
						+ "]";
			}

		});

		load();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void load() {
		List<String> serverAddressList = Activator.getDefault()
				.readServerList();
		Server[] serverList = new Server[serverAddressList.size()];
		for (int i = 0; i < serverList.length; i++) {
			serverList[i] = new Server(serverAddressList.get(i));
		}
		viewer.setInput(serverList);
	}
}