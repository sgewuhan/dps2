package com.awesometech.dps.processor.irobot.preferences;

import com.awesometech.dps.processor.irobot.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class IRobotStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO ���I8�˺������ַ������
		String serverUrl = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.URL);
		String userName = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.USERNAME);
//		String userPwd = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.USERPWD);
		int timeOut = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.TIMEOUT);
		if ("".equals(serverUrl)) {
			throw new Exception("IRobot����������ʧ�ܣ���������ҳ��������URL��ֵ");
		}
		if ("".equals(userName)) {
			throw new Exception("IRobot����������ʧ�ܣ���������ҳ��������UserName��ֵ");
		}
		// TODO ��Ҫ��������һ��timeout������
		if (timeOut < 1) {
			throw new Exception("IRobot����������ʧ�ܣ�timeOut����Ϊ��ֵ");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
