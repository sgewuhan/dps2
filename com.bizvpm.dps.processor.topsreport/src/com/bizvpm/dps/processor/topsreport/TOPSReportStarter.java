package com.bizvpm.dps.processor.topsreport;

import com.bizvpm.dps.runtime.IProcessorActivator;

public class TOPSReportStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		String ip = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.IP);
		if ("".equals(ip)) {
			throw new Exception("TOPS报告转换器启动失败，请在属性页面中设置TOPS Server的地址");
		}
		String templatePath = Activator.getDefault().getPreferenceStore().getString(PreferenceConstacts.TEMPLATEPATH);
		if ("".equals(templatePath)) {
			throw new Exception("TOPS报告转换器启动失败，请在属性页面中设置Word模板文件");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
