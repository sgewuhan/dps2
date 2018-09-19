package com.bizvpm.dps.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class ProcessJob extends Job implements IJobChangeListener {

	private Result processResult;
	private final ProcessorConfig config;
	private ProcessCounter counter;
	private IProcessContext context;

	public ProcessJob(String name, IProcessContext context) {
		super(name);
		config = context.getConfig();
		this.context = context;
		addJobChangeListener(this);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IProcessorRunable runable = config.getProcessorRunable();
		try {
			ProcessTask input = context.getProcessTask();
			checkInput(input);
			ProcessResult output = runable.run(input, monitor, context);
			processResult = (Result) Platform.getAdapterManager().getAdapter(
					output, Result.class);
			processResult.append("_taskid", input.getId());
		} catch (Exception e) {
			return new Status(IStatus.ERROR, config.getPlugId(), 0,
					e.getMessage(), e);
		}
		return Status.OK_STATUS;
	}

	private void checkInput(ProcessTask input) throws Exception {
		ParameterList plist = config.getParameterList();
		List<Parameter> parameters = plist.getParameters();
		if(parameters==null){
			return;
		}
			
		for (int i = 0; i < parameters.size(); i++) {
			Parameter parameter = parameters.get(i);
			String name = parameter.getName();
			Object inputValue = input.get(name);
			if(inputValue == null){
				if(!parameter.isOptional()){
					throw new Exception("Parameter: "+name+" no-null value required.");
				}else{
					return;
				}
			}
			String type = parameter.getType();
			if("String".equals(type)){
				if(!(inputValue instanceof String)){
					throw new Exception("Parameter: "+name+" type error, String value required.");
				}
			}else if("Long".equals(type)){
				if(!(inputValue instanceof Long)){
					throw new Exception("Parameter: "+name+" type error, Long value required.");
				}
			}else if("Double".equals(type)){
				if(!(inputValue instanceof Double)){
					throw new Exception("Parameter: "+name+" type error, Double value required.");
				}
			}else if("Integer".equals(type)){
				if(!(inputValue instanceof Integer)){
					throw new Exception("Parameter: "+name+" type error, Integer value required.");
				}
			}else if("Float".equals(type)){
				if(!(inputValue instanceof Float)){
					throw new Exception("Parameter: "+name+" type error, Float value required.");
				}
			}else if("Boolean".equals(type)){
				if(!(inputValue instanceof Boolean)){
					throw new Exception("Parameter: "+name+" type error, Boolean value required.");
				}
			}else if("Date".equals(type)){
				if(!(inputValue instanceof Date)){
					throw new Exception("Parameter: "+name+" type error, Date value required.");
				}
			}else if("List".equals(type)){
				if(!(inputValue instanceof List)){
					throw new Exception("Parameter: "+name+" type error, List value required.");
				}
			}else if("Map".equals(type)){
				if(!(inputValue instanceof Map)){
					throw new Exception("Parameter: "+name+" type error, Map value required.");
				}
			}
			
			List<Object> restrictions = parameter.getRestrictions();
			if(!restrictions.isEmpty()){
				if(!restrictions.contains(inputValue)){
					throw new Exception("Parameter: "+name+" value restricted in "+restrictions);
				}
			}
		}
	}

	public Result getProcessResult() {
		return processResult;
	}

	@Override
	public boolean belongsTo(Object family) {
		return config.getId().equals(family);
	}

	public void setCounter(ProcessCounter counter) {
		this.counter = counter;
		String processorTypeId = config.getId();
		ProcessJobRule rule = new ProcessJobRule(processorTypeId, counter);
		setRule(rule);
	}

	@Override
	public void aboutToRun(IJobChangeEvent event) {
		counter.add(1);
	}

	@Override
	public void awake(IJobChangeEvent event) {
	}

	@Override
	public void done(IJobChangeEvent event) {
		counter.add(-1);
	}

	@Override
	public void running(IJobChangeEvent event) {
	}

	@Override
	public void scheduled(IJobChangeEvent event) {
	}

	@Override
	public void sleeping(IJobChangeEvent event) {
	}

}
