package com.bizvpm.dps.processor.acmecad.preferences;

import com.bizvpm.dps.processor.acmecad.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class AcmeCADStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		String cadConverterPath = Activator.getDefault().getPreferenceStore()
				.getString(AcmePreferenceConstants.CAD_CONVERTER_PATH);
		if("".equals(cadConverterPath)){
			throw new Exception("AutoCADת��������ʧ�ܣ���������ҳ��������CAD Converter Path��ֵ");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
