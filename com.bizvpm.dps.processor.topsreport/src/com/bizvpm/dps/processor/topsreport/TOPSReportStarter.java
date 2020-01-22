package com.bizvpm.dps.processor.topsreport;

import com.bizvpm.dps.runtime.IProcessorActivator;

public class TOPSReportStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		String ip = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.IP);
		if ("".equals(ip)) {
			throw new Exception("TOPS����ת��������ʧ�ܣ���������ҳ��������TOPS Server�ĵ�ַ");
		}
		String templatePath = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.TEMPLATEPATH);
		if ("".equals(templatePath)) {
			throw new Exception("TOPS����ת��������ʧ�ܣ���������ҳ��������Wordģ���ļ�");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
