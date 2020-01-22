package com.bizvpm.dps.processor.tmtsap;

import com.bizvpm.dps.runtime.IProcessorActivator;

public class TMTSAPStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO 更详细的提示
		String value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_CLIENT);
		if ("".equals(value)) {
			throw new Exception("TMT SAP 处理器启动失败，请填写：SAP客户端名称。");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_HOST);
		if ("".equals(value)) {
			throw new Exception("TMT SAP 处理器启动失败，请填写：SAP主机IP。");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_INSTANCENUMBER);
		if ("".equals(value)) {
			throw new Exception("TMT SAP 处理器启动失败，请填写：SAP实例编号。");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_LANGUAGE);
		if ("".equals(value)) {
			throw new Exception("TMT SAP 处理器启动失败，请填写：SAP语言设。");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_PASSWORD);
		if ("".equals(value)) {
			throw new Exception("TMT SAP 处理器启动失败，请填写：SAP用户登录密码。");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_USERID);
		if ("".equals(value)) {
			throw new Exception("TMT SAP 处理器启动失败，请填写：SAP客户端用户ID。");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
