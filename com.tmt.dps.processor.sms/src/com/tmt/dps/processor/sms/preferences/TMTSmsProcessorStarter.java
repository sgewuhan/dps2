package com.tmt.dps.processor.sms.preferences;

import com.bizvpm.dps.runtime.IProcessorActivator;
import com.tmt.dps.processor.sms.Activator;

public class TMTSmsProcessorStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		boolean userWebService = Activator.getDefault().getPreferenceStore()
				.getBoolean(TMTSmsPreferenceConstants.USERWEBSERVICE);
		if (userWebService) {
			String webServiceWSDL = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.WEBSERVICEWSDL);
			if ("".equals(webServiceWSDL)) {
				throw new Exception("TMT SMS转换器启动失败，请在属性页面中设置 WebService WSDL 的值");
			}
			String user = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.WEBSERVICEUSER);
			if ("".equals(user)) {
				throw new Exception("TMT SMS转换器启动失败，请在属性页面中设置 WebService User 的值");
			}
			String pwd = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.WEBSERVICEPASSWORD);
			if ("".equals(pwd)) {
				throw new Exception(
						"TMT SMS转换器启动失败，请在属性页面中设置 WebService Password 的值");
			}
		} else {
			String ip = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.IP);
			if ("".equals(ip)) {
				throw new Exception("TMT SMS转换器启动失败，请在属性页面中设置 Database IP 的值");
			}
			int port = Activator.getDefault().getPreferenceStore()
					.getInt(TMTSmsPreferenceConstants.PORT);
			if (port == 0) {
				throw new Exception("TMT SMS转换器启动失败，请在属性页面中设置 DataBase Port 的值");
			}
			String user = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.DBUSER);
			if ("".equals(user)) {
				throw new Exception("TMT SMS转换器启动失败，请在属性页面中设置 DataBase User 的值");
			}
			String pwd = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.DBPASSWORD);
			if ("".equals(pwd)) {
				throw new Exception(
						"TMT SMS转换器启动失败，请在属性页面中设置 DataBase Password 的值");
			}
			String name = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.NAME);
			if ("".equals(name)) {
				throw new Exception("TMT SMS转换器启动失败，请在属性页面中设置 DataBase Name 的值");
			}
		}
	}

	@Override
	public void start() throws Exception {
		String name = Activator.getDefault().getPreferenceStore()
				.getString(TMTSmsPreferenceConstants.NAME);

		String url = "jdbc:jtds:sqlserver://"
				+ Activator.getDefault().getPreferenceStore()
						.getString(TMTSmsPreferenceConstants.IP)
				+ ":" + Activator.getDefault().getPreferenceStore()
						.getInt(TMTSmsPreferenceConstants.PORT)
				+ "/" + name;

		String uid = Activator.getDefault().getPreferenceStore()
				.getString(TMTSmsPreferenceConstants.DBUSER);

		String pwd = Activator.getDefault().getPreferenceStore()
				.getString(TMTSmsPreferenceConstants.DBPASSWORD);

		int maxConn = Activator.getDefault().getPreferenceStore()
				.getInt(TMTSmsPreferenceConstants.MAXCONNECTION);
		Activator.getDefault().initDBConnectionPool(name, url, uid, pwd,
				maxConn);
	}

	@Override
	public void stop() throws Exception {
		Activator.getDefault().releaseDBConnectionPool();
	}

}
