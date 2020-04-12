package com.awesometech.dps.processor.irobot.preferences;

import com.awesometech.dps.processor.irobot.Activator;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class IRobotMonitorStarter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO ��Ϣ�����Ҫ��������
		String serverIp = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_IP);
		String serverPort = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_PORT);
		String userName = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_USERNAME);
//		String userPwd = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.USERPWD);
		int timeOut = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.IRobot_TIMEOUT);
		if ("".equals(serverIp)) {
			throw new Exception("IRobot����������ʧ�ܣ���������ҳ��������IRobot IP��ֵ");
		}
		// ��Ҫ��IP��ַ��־���ڷ����������Բ���������дlocalhost��127.0.0.1
		if("127.0.0.1".equals(serverIp) || "localhost".equals(serverIp)) {
			throw new Exception("IRobot����������ʧ�ܣ�IRobot IP����д��ǰ��������IP");
		}
		if ("".equals(serverPort)) {
			throw new Exception("IRobot����������ʧ�ܣ���������ҳ��������IRobot PORT��ֵ");
		}
		if ("".equals(userName)) {
			throw new Exception("IRobot����������ʧ�ܣ���������ҳ��������IRobot UserName��ֵ");
		}
		// TODO ��Ҫ��������һ��timeout������
		if (timeOut < 1) {
			throw new Exception("IRobot����������ʧ�ܣ�timeOut����Ϊ��ֵ");
		}
	}

	@Override
	public void start() throws Exception {
		Activator.getDefault().getDB();
		Activator.getDefault().createMonitorJob();
	}

	@Override
	public void stop() throws Exception {
		Activator.getDefault().stopMonitorJob();
//		Activator.getDefault().dbClientClose();  
	}

}
