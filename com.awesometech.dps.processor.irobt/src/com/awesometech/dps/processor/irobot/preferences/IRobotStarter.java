package com.awesometech.dps.processor.irobot.preferences;

import com.awesometech.dps.processor.irobot.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class IRobotStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO 信息检查需要补充完善
		String serverIp = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_IP);
		String serverPort = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_PORT);
		String userName = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_USERNAME);
//		String userPwd = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.USERPWD);
		int timeOut = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.IRobot_TIMEOUT);
		if ("".equals(serverIp)) {
			throw new Exception("IRobot处理器启动失败，请在属性页面中设置IRobot IP的值");
		}
		if ("".equals(serverPort)) {
			throw new Exception("IRobot处理器启动失败，请在属性页面中设置IRobot PORT的值");
		}
		if ("".equals(userName)) {
			throw new Exception("IRobot处理器启动失败，请在属性页面中设置IRobot UserName的值");
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
