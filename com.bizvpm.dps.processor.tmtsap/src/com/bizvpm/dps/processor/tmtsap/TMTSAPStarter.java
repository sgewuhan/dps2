package com.bizvpm.dps.processor.tmtsap;

import com.bizvpm.dps.runtime.IProcessorActivator;

public class TMTSAPStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO ����ϸ����ʾ
		String value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_CLIENT);
		if ("".equals(value)) {
			throw new Exception("TMT SAP ����������ʧ�ܣ�����д��SAP�ͻ������ơ�");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_HOST);
		if ("".equals(value)) {
			throw new Exception("TMT SAP ����������ʧ�ܣ�����д��SAP����IP��");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_INSTANCENUMBER);
		if ("".equals(value)) {
			throw new Exception("TMT SAP ����������ʧ�ܣ�����д��SAPʵ����š�");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_LANGUAGE);
		if ("".equals(value)) {
			throw new Exception("TMT SAP ����������ʧ�ܣ�����д��SAP�����衣");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_PASSWORD);
		if ("".equals(value)) {
			throw new Exception("TMT SAP ����������ʧ�ܣ�����д��SAP�û���¼���롣");
		}
		value = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.S_EAI_SAP_USERID);
		if ("".equals(value)) {
			throw new Exception("TMT SAP ����������ʧ�ܣ�����д��SAP�ͻ����û�ID��");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
