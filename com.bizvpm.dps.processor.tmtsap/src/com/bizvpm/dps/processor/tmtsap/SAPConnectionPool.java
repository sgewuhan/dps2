package com.bizvpm.dps.processor.tmtsap;

import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Client;
import com.sap.mw.jco.JCO.Pool;

public class SAPConnectionPool {

	public static String POOL_NAME = "SAPJCO";

	public Client connSAP() {

		int maxConn = Activator.getDefault().getMaxconn();// 200;
		String client = Activator.getDefault().getClient();// "700"
		String userid = Activator.getDefault().getUserId();// "ITFSAP"
		String password = Activator.getDefault().getPassword();// "12392008"
		String lang = Activator.getDefault().getLang();// "ZH"
		String host = Activator.getDefault().getHost();// "172.16.9.74"
		String instance = Activator.getDefault().getInstance();// "00"
		Client mConnection = null;
		try {
			Pool pool = JCO.getClientPoolManager().getPool(POOL_NAME);
			if (pool == null) {
				JCO.addClientPool(POOL_NAME, // Alias for this pool
						maxConn, // Max. number of connections
						client, // SAP client
						userid, // userid
						password, // password
						lang, // language
						// "172.16.9.90", // host name
						// "01" );
						host, // host name
						instance);
			}
			mConnection = JCO.getClient(POOL_NAME);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return mConnection;
	}
}