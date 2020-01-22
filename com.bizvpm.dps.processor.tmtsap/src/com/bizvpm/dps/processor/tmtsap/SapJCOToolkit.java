package com.bizvpm.dps.processor.tmtsap;

import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Client;

public class SapJCOToolkit {
	
	public static Client getSAPClient() {
		SAPConnectionPool connPool = new SAPConnectionPool();
		Client sapClient = connPool.connSAP();
		return sapClient;
	}

	public static void releaseClient(Client sapClient) {
		JCO.releaseClient(sapClient);
	}
}
