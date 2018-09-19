package com.bizvpm.dps.processor.acmecad.preferences;

import com.bizvpm.dps.processor.acmecad.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class AcmeCADStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		String cadConverterPath = Activator.getDefault().getPreferenceStore()
				.getString(AcmePreferenceConstants.CAD_CONVERTER_PATH);
		if("".equals(cadConverterPath)){
			throw new Exception("AutoCAD转换器启动失败，请在属性页面中设置CAD Converter Path的值");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
