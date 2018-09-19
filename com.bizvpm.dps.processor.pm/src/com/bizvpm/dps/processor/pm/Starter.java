package com.bizvpm.dps.processor.pm;

import com.bizvpm.dps.runtime.IProcessorActivator;

public class Starter implements IProcessorActivator {

	@Override
	public void startCheck() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws Exception {
		Activator.getDefault().getDB();
	}


	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

}
