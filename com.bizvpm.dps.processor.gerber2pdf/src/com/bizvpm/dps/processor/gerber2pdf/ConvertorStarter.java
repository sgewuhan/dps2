package com.bizvpm.dps.processor.gerber2pdf;

import com.bizvpm.dps.runtime.IProcessorActivator;

public class ConvertorStarter implements IProcessorActivator {

	public ConvertorStarter() {
	}

	@Override
	public void startCheck() throws Exception {
		String path = Activator.getDefault().getPreferenceStore().getString("convertorPath");
		if ("".equals(path)) {
			throw new Exception("Gerber转换器启动失败，请在属性页面中设置Gerber转换器路径的值");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
