package com.sg.sqldb;

import java.sql.Connection;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DDB implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.sg.sqldb"; //$NON-NLS-1$

	// The shared instance
	private static DDB plugin;

	private ConnectionManager connMgr;

	public ConnectionManager getConnMgr() {
		return connMgr;
	}

	/**
	 * The constructor
	 */
	public DDB() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		initConnection(null);
	}
	
	public void initConnection(Properties properties){
		connMgr = ConnectionManager.getInstance(properties);
	}

	public Connection getConnection(String dataSourceName) {
		return connMgr.getConnection(dataSourceName);
	}

	public Connection createConnection(String dataSourceName) {
		return connMgr.createConnection(dataSourceName);
	}

	public void freeConnection(String poolName, Connection connection) {
		connMgr.freeConnection(poolName, connection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		destoryConnection();
	}

	private void destoryConnection() {
		connMgr.release();
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static DDB getDefault() {
		return plugin;
	}

}
