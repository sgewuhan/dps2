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
				throw new Exception("TMT SMSת��������ʧ�ܣ���������ҳ�������� WebService WSDL ��ֵ");
			}
			String user = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.WEBSERVICEUSER);
			if ("".equals(user)) {
				throw new Exception("TMT SMSת��������ʧ�ܣ���������ҳ�������� WebService User ��ֵ");
			}
			String pwd = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.WEBSERVICEPASSWORD);
			if ("".equals(pwd)) {
				throw new Exception(
						"TMT SMSת��������ʧ�ܣ���������ҳ�������� WebService Password ��ֵ");
			}
		} else {
			String ip = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.IP);
			if ("".equals(ip)) {
				throw new Exception("TMT SMSת��������ʧ�ܣ���������ҳ�������� Database IP ��ֵ");
			}
			int port = Activator.getDefault().getPreferenceStore()
					.getInt(TMTSmsPreferenceConstants.PORT);
			if (port == 0) {
				throw new Exception("TMT SMSת��������ʧ�ܣ���������ҳ�������� DataBase Port ��ֵ");
			}
			String user = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.DBUSER);
			if ("".equals(user)) {
				throw new Exception("TMT SMSת��������ʧ�ܣ���������ҳ�������� DataBase User ��ֵ");
			}
			String pwd = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.DBPASSWORD);
			if ("".equals(pwd)) {
				throw new Exception(
						"TMT SMSת��������ʧ�ܣ���������ҳ�������� DataBase Password ��ֵ");
			}
			String name = Activator.getDefault().getPreferenceStore()
					.getString(TMTSmsPreferenceConstants.NAME);
			if ("".equals(name)) {
				throw new Exception("TMT SMSת��������ʧ�ܣ���������ҳ�������� DataBase Name ��ֵ");
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
