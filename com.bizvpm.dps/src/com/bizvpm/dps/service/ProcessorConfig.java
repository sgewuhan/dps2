package com.bizvpm.dps.service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

import com.bizvpm.dps.runtime.IProcessorActivator;
import com.bizvpm.dps.runtime.IProcessorRunable;

@SuppressWarnings("restriction")
public class ProcessorConfig extends PersistableProcessor {

	private IConfigurationElement ce;

	private int maxThreadCount;

	private IProcessorActivator activater;

	public ProcessorConfig(IConfigurationElement ce) {
		this.ce = ce;
		setId(getPlugId() + ":" + ce.getAttribute("id"));
		setName(ce.getAttribute("name"));
		setDescritpion(ce.getAttribute("description"));

		String webpage = ce.getAttribute("webpage");
		if(webpage!=null){
			try {
				Bundle bundle = Platform.getBundle(ce.getNamespaceIdentifier());
				URL url = BundleUtility.find(bundle, webpage);
				String filePath = FileLocator.toFileURL(url).getFile();
				InputStream is = new FileInputStream(filePath);
				String result = IOUtils.toString(is, "utf-8");
				setDescritpion(result);
				is.close();
			} catch (Exception e) {
			}
		}
		
		maxThreadCount = Integer.parseInt(ce.getAttribute("maxThreadCount"));
		try {
			activater = (IProcessorActivator) ce.createExecutableExtension("processorActivater");
		} catch (Exception e) {
		}

		loadParameters(ce);
	}

	private void loadParameters(IConfigurationElement ce) {
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		IConfigurationElement[] children = ce.getChildren("parameter");
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Parameter parameter = ParameterHelper.createParamter(children[i]);
				parameters.add(parameter);
			}
		}
		ParameterList list = new ParameterList();
		list.setParameters(parameters);
		setParamterList(list);
	}

	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	public IProcessorRunable getProcessorRunable() {
		try {
			return (IProcessorRunable) ce.createExecutableExtension("runnable");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPlugId() {
		return ce.getDeclaringExtension().getContributor().getName();
	}

	public void startCheck() throws Exception {
		if (activater != null) {
			activater.startCheck();
		}
	}

	public void start() throws Exception {
		if (activater != null) {
			activater.start();
		}
		setOnline(true);
	}

	public void stop() throws Exception {
		if (activater != null) {
			activater.stop();
		}
		setOnline(false);
	}

	public String getCatalog() {
		return ce.getAttribute("catalog");
	}

}
