package com.bizvpm.dps.processor.email.preferences;

import com.bizvpm.dps.processor.email.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class EmailProcessorStarter implements IProcessorActivator {

	public void startCheck() throws Exception {
		String hostName = Activator.getDefault().getPreferenceStore()
				.getString(EMailPreferenceConstants.EMAIL_HOSTNAME);
		if ("".equals(hostName)) {
			throw new Exception("Email转换器启动失败，请在属性页面中设置mail smtp host的值");
		}
		String senderAddress = Activator.getDefault().getPreferenceStore()
				.getString(EMailPreferenceConstants.EMAIL_AUTHUSER);
		if ("".equals(senderAddress)) {
			throw new Exception("Email转换器启动失败，请在属性页面中设置sender address的值");
		}
		String senderPassword = Activator.getDefault().getPreferenceStore()
				.getString(EMailPreferenceConstants.EMAIL_AUTHPASS);
		if ("".equals(senderPassword)) {
			throw new Exception("Email转换器启动失败，请在属性页面中设置sender password的值");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
