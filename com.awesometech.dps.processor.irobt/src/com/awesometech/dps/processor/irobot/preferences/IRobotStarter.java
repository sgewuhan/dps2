package com.awesometech.dps.processor.irobot.preferences;

import com.awesometech.dps.processor.irobot.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class IRobotStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO ���I8�˺������ַ������
		String url = Activator.getDefault().getPreferenceStore()
				.getString(IRobotPreferenceConstants.URL);
		String userName = Activator.getDefault().getPreferenceStore()
				.getString(IRobotPreferenceConstants.USERNAME);
//		String userPwd = Activator.getDefault().getPreferenceStore()
//				.getString(IRobotPreferenceConstants.USERPWD);
		if("".equals(url)){
			throw new Exception("IRobot����������ʧ�ܣ���������ҳ��������URL��ֵ");
		}
		if("".equals(userName)){
			throw new Exception("IRobot����������ʧ�ܣ���������ҳ��������UserName��ֵ");
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
