package com.bizvpm.dps.processor.gerber2pdf;

import com.bizvpm.dps.runtime.IProcessorActivator;

public class ConvertorStarter implements IProcessorActivator {

	public ConvertorStarter() {
	}

	@Override
	public void startCheck() throws Exception {
		String path = Activator.getDefault().getPreferenceStore().getString("convertorPath");
		if ("".equals(path)) {
			throw new Exception("Gerberת��������ʧ�ܣ���������ҳ��������Gerberת����·����ֵ");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
