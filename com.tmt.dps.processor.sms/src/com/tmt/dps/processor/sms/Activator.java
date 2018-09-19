package com.tmt.dps.processor.sms;

import java.sql.Connection;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	private static Activator plugin;
	private static DBConnectionPool pool;

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	public static Activator getDefault() {
		return plugin;
	}

	public Connection getConnection() throws Exception {
		if (pool != null) {
			return pool.getConnection();
		} else {
			throw new Exception("无法连接数据库!");
		}
	}

	public void freeConnection(Connection conn) throws Exception {
		pool.freeConnection(conn);
	}

	public void initDBConnectionPool(String name, String url, String uid,
			String pwd, int maxConn) {
		pool = new DBConnectionPool(name, url, uid, pwd, maxConn);
	}

	public void releaseDBConnectionPool() throws Exception {
		if(pool!= null){
			pool.release();
		}
	}

}
