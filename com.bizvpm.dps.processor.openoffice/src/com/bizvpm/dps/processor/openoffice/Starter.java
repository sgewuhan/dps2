package com.bizvpm.dps.processor.openoffice;

import com.bizvpm.dps.processor.openoffice.preferences.PreferenceConstants;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class Starter implements IProcessorActivator {

	public Starter() {
	}

	@Override
	public void startCheck() throws Exception {
		if (!Boolean.TRUE
				.equals(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_BOOLEAN))) {
			throw new Exception("Officeת��������ʧ�ܣ���������ҳ��������booleanֵΪ��");
		}
	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

}
