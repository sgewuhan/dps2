package com.bizvpm.dps.service;

import org.eclipse.core.runtime.Platform;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class ProcessContext implements IProcessContext {

	private Task task;

	private ProcessorConfig config;

	private ProcessorManager manager;

	private ProcessTask processTask;

	public ProcessContext(ProcessorManager manager, Task task,
			ProcessorConfig config) {
		this.manager = manager;
		this.task = task;
		this.config = config;
	}

	@Override
	public ProcessorConfig getConfig() {
		return config;
	}

	@Override
	public void scheduleTask(ProcessTask task, String processorTypeId)
			throws Exception {
		Task _task = (Task) Platform.getAdapterManager().getAdapter(task, Task.class);
		manager.scheduleTask(_task, processorTypeId);
	}

	@Override
	public ProcessResult runTask(ProcessTask task, String processorTypeId)
			throws Exception {
		Task _task = (Task) Platform.getAdapterManager().getAdapter(task, Task.class);
		Result result = manager.runTask(_task, processorTypeId);
		return (ProcessResult) Platform.getAdapterManager().getAdapter(result, ProcessResult.class);
	}

	@Override
	public ProcessTask getProcessTask() {
		if (this.processTask == null) {
			this.processTask = (ProcessTask) Platform.getAdapterManager()
					.getAdapter(task, ProcessTask.class);
		}
		return this.processTask;
	}

}
