package com.bizvpm.dps.service;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class ProcessJobRule implements ISchedulingRule {

	private String processorTypeId;
	private ProcessCounter counter;

	public ProcessJobRule(String processorTypeId, ProcessCounter counter) {
		this.processorTypeId = processorTypeId;
		this.counter = counter;
	}
	

	@Override
	public boolean contains(ISchedulingRule rule) {
		if( this == rule){
			return true;
		}
		if (rule instanceof ProcessJobRule){
			return this.processorTypeId.equals(((ProcessJobRule) rule).processorTypeId);
		}
		
		return false;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if(this == rule){
			return true;
		}
		if(rule instanceof ProcessJobRule){
			return !counter.isFree();
		}
		return false;
	}

}
