package com.bizvpm.dps.ui;

import org.eclipse.core.expressions.PropertyTester;

import com.bizvpm.dps.service.ProcessorConfig;

public class ProcessorOnlineTester extends PropertyTester {

	public ProcessorOnlineTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(receiver instanceof ProcessorConfig){
			if("online".equals(property)){
				ProcessorConfig config = (ProcessorConfig) receiver;
				return new Boolean(config.isOnline()).equals(expectedValue);
			}
		}
		return false;
	}

}
