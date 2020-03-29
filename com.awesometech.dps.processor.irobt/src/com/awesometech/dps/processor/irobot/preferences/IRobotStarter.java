package com.awesometech.dps.processor.irobot.preferences;

import com.awesometech.dps.processor.irobot.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class IRobotStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO 检查I8账号密码地址的配置
		String serverUrl = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.URL);
		String userName = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.USERNAME);
//		String userPwd = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.USERPWD);
		int timeOut = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.TIMEOUT);
		if ("".equals(serverUrl)) {
			throw new Exception("IRobot处理器启动失败，请在属性页面中设置URL的值");
		}
		if ("".equals(userName)) {
			throw new Exception("IRobot处理器启动失败，请在属性页面中设置UserName的值");
		}
		// TODO 需要考虑设置一个timeout的上限
		if (timeOut < 1) {
			throw new Exception("IRobot处理器启动失败，timeOut必须为正值");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
