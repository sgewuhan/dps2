package com.bizvpm.dps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Endpoint;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.bizvpm.dps.service.DataSetAdapterFactory;
import com.bizvpm.dps.service.IDataSetAdaptable;
import com.bizvpm.dps.service.IPersistence;
import com.bizvpm.dps.service.PersistenceService;
import com.bizvpm.dps.service.ProcessorManager;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bizvpm.dps"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private IPersistence persistence;

	private Endpoint processorManagerService;

	private String url;

	private int port;

	private String hostName;

	private ProcessorManager processorManager;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Log.BUNDLE = this.getBundle();
		Log.PLUGIN_ID = PLUGIN_ID;

		resgisterAdapter();

		loadServerList();

	}

	private void resgisterAdapter() {
		Platform.getAdapterManager().registerAdapters(
				new DataSetAdapterFactory(), IDataSetAdaptable.class);
	}

	private void loadServerList() {
		List<String> serverList = readServerList();
		for (int i = 0; i < serverList.size(); i++) {
			String server = serverList.get(i);
			try {
				String serverUrl = "http://" + server + "/dps/persistence?wsdl";
				initPersistence(serverUrl);
				getPreferenceStore().setValue("server", server);
				Log.logOK("Server connected:" + server);
				return;
			} catch (Exception e) {
				Log.logError("Cannot connect " + server + ", try next.", e);
			}
		}
		if (persistence == null) {
			Log.logError(new Exception("Cannot connect all server"));
		}

	}

	public List<String> readServerList() {
		List<String> result = new ArrayList<String>();
		FileReader reader = null;
		BufferedReader br = null;
		try {
			String filePath = System.getProperty("user.dir") //$NON-NLS-1$
					+ "/configuration/dpfserverlist";
			reader = new FileReader(filePath);
			br = new BufferedReader(reader);
			String str = null;
			while ((str = br.readLine()) != null) {
				result.add(str);
			}
		} catch (Exception e) {
			Log.logError(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				Log.logError(e);
			}
		}

		return result;
	}

	private void initProcessorManagerService(String hostName, String hostIp,
			int port) {
		this.hostName = hostName;
		this.port = port;
		url = "http://" + hostIp + ":" + port + "/processor";
		processorManager = new ProcessorManager();
		processorManager.setHost(hostName);
		processorManager.setPersistence(persistence);

		processorManagerService = Endpoint.create(processorManager);
		processorManagerService.publish(url);
		processorManager.online();
		Log.logOK("Process manager service online: " + url);
	}

	public ProcessorManager getProcessorManager() {
		return processorManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		if (processorManagerService != null) {
			ProcessorManager manager = (ProcessorManager) processorManagerService
					.getImplementor();
			manager.offline();
			processorManagerService.stop();
		}
		Log.logInfo("Process manager service offline: " + url);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getImageRegistry().getDescriptor(key);
	}

	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		// 注册image目录下的所有文件
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		if (BundleUtility.isReady(bundle)) {
			URL fullPathString = BundleUtility.find(bundle, "icons");
			try {
				File folder = new File(FileLocator.toFileURL(fullPathString)
						.getFile());
				File[] files = folder.listFiles();
				ImageDescriptor imgd;
				String key;
				for (File f : files) {
					key = f.getName();
					imgd = AbstractUIPlugin.imageDescriptorFromPlugin(
							PLUGIN_ID, "icons/" + key); //$NON-NLS-1$
					reg.put(key, imgd);
				}
			} catch (Exception e) {
			}
		}

		super.initializeImageRegistry(reg);
	}

	private void initPersistence(String serverUrl) throws Exception {
		PersistenceService service = new PersistenceService(new URL(serverUrl));
		persistence = service.getPort(IPersistence.class);
	}

	public String getUrl() {
		return url;
	}

	public void signin(String localHostName, String localHostIp,
			int localHostPort) {
		initProcessorManagerService(localHostName, localHostIp, localHostPort);
	}

	public static IPersistence getServer() {
		return plugin.persistence;
	}

	public static IPreferenceStore getStore() {
		return plugin.getPreferenceStore();
	}

	public static int getPort() {
		return plugin.port;
	}

	public static String getHostName() {
		return plugin.hostName;
	}
}
