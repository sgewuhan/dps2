package com.bizvpm.dps.runtime;

import com.bizvpm.dps.service.ProcessorConfig;

public interface IProcessContext {

	public ProcessorConfig getConfig();
	
	public void scheduleTask(ProcessTask task, String processorTypeId) throws Exception;
	
	public ProcessResult runTask(ProcessTask task, String processorTypeId) throws Exception;
	
	public ProcessTask getProcessTask();


}
