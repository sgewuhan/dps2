package com.bizvpm.dps.processor.email.preferences;

import com.bizvpm.dps.processor.email.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class EmailProcessorStarter implements IProcessorActivator {

	public void startCheck() throws Exception {
		String hostName = Activator.getDefault().getPreferenceStore()
				.getString(EMailPreferenceConstants.EMAIL_HOSTNAME);
		if ("".equals(hostName)) {
			throw new Exception("Emailת��������ʧ�ܣ���������ҳ��������mail smtp host��ֵ");
		}
		String senderAddress = Activator.getDefault().getPreferenceStore()
				.getString(EMailPreferenceConstants.EMAIL_AUTHUSER);
		if ("".equals(senderAddress)) {
			throw new Exception("Emailת��������ʧ�ܣ���������ҳ��������sender address��ֵ");
		}
		String senderPassword = Activator.getDefault().getPreferenceStore()
				.getString(EMailPreferenceConstants.EMAIL_AUTHPASS);
		if ("".equals(senderPassword)) {
			throw new Exception("Emailת��������ʧ�ܣ���������ҳ��������sender password��ֵ");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
