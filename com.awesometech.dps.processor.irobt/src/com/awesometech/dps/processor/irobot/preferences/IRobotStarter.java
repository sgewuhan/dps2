package com.awesometech.dps.processor.irobot.preferences;

import com.awesometech.dps.processor.irobot.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class IRobotStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO 检查I8账号密码地址的配置
		String url = Activator.getDefault().getPreferenceStore()
				.getString(IRobotPreferenceConstants.URL);
		String userName = Activator.getDefault().getPreferenceStore()
				.getString(IRobotPreferenceConstants.USERNAME);
//		String userPwd = Activator.getDefault().getPreferenceStore()
//				.getString(IRobotPreferenceConstants.USERPWD);
		if("".equals(url)){
			throw new Exception("IRobot处理器启动失败，请在属性页面中设置URL的值");
		}
		if("".equals(userName)){
			throw new Exception("IRobot处理器启动失败，请在属性页面中设置UserName的值");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
