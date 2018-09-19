package com.bizvpm.dps.processor.pm;

import javax.xml.ws.Endpoint;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.bizvpm.dps.processor.pm.ws.PMServer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.client.MongoDatabase;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.bizvpm.dps.processors.pm"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	public static MongoDatabase database;

	public MongoClient client;

	private Endpoint endpoint;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store.getBoolean(PreferenceConstacts.START_STANCELONE_WS)) {
			startStandloneWebSerivce();
		}
	}

	public void startStandloneWebSerivce() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String url = store.getString(PreferenceConstacts.STAND_WS_URL);
		if (!url.isEmpty()) {
			endpoint = Endpoint.create(new PMServer());
			endpoint.publish(url);
			System.out.println(url);
		} else {
			throw new IllegalArgumentException("没有设置独立启动的URL");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (client != null) {
			client.close();
		}
		if (endpoint != null) {
			endpoint.stop();
		}
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

	public MongoClient getClient() {
		if (client == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			int connsPerHost = store.getInt(PreferenceConstacts.CONNECTIONS_PER_HOST);
			int maxWaitTime = store.getInt(PreferenceConstacts.MAX_WAIT_TIME);
			int sockerTimeout = store.getInt(PreferenceConstacts.SOCKET_TIMEOUT);
			int connTimeout = store.getInt(PreferenceConstacts.CONNECT_TIMEOUT);
			int threadsAllowedToBlockForConnectionMultiplier = store
					.getInt(PreferenceConstacts.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER);

			// requied
			int port = store.getInt(PreferenceConstacts.PORT);
			if (port == PreferenceConstacts.UNSET) {
				throw new IllegalArgumentException("缺少端口号设置");
			}

			String host = store.getString(PreferenceConstacts.IP);
			if (host.isEmpty()) {
				throw new IllegalArgumentException("缺少数据库主机设置");
			}

			Builder builder = MongoClientOptions.builder();
			if (connsPerHost != 0) {
				builder.connectionsPerHost(connsPerHost); // $NON-NLS-1$
			}
			if (maxWaitTime != 0) {
				builder.maxWaitTime(maxWaitTime); // $NON-NLS-1$
			}
			if (sockerTimeout != 0) {
				builder.socketTimeout(sockerTimeout); // $NON-NLS-1$
			}
			if (connTimeout != 0) {
				builder.connectTimeout(connTimeout); // $NON-NLS-1$
			}
			if (threadsAllowedToBlockForConnectionMultiplier != 0) {
				builder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier); // $NON-NLS-1$
			}

			ServerAddress address = new ServerAddress(host, port);
			client = new MongoClient(address, builder.build());
		}
		return client;
	}

	public MongoDatabase getDB() {
		if (database == null) {
			client = getClient();
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			String dbname = store.getString(PreferenceConstacts.DB);
			if (dbname.isEmpty()) {
				throw new IllegalArgumentException("缺少数据库名称设置");
			}
			database = client.getDatabase(dbname);
		}
		return database;
	}

	public static MongoDatabase db() {
		return getDefault().getDB();
	}

}
