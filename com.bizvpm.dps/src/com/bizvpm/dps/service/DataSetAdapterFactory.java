package com.bizvpm.dps.service;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;

import com.bizvpm.dps.runtime.ProcessDataSet;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class DataSetAdapterFactory implements IAdapterFactory {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == Task.class) {
			if (adaptableObject instanceof ProcessTask) {
				return getTask((ProcessTask) adaptableObject);
			}else{
				return null;
			}
		}

		if (adapterType == ProcessTask.class) {
			if (adaptableObject instanceof Task) {
				return getProcessTask((Task) adaptableObject);
			}else{
				return null;
			}
		}

		if (adapterType == Result.class) {
			if (adaptableObject instanceof ProcessResult) {
				return getResult((ProcessResult) adaptableObject);
			}else{
				return null;
			}
		}

		if (adapterType == ProcessResult.class) {
			if (adaptableObject instanceof Result) {
				return getProcessResult((Result) adaptableObject);
			}else{
				return null;
			}
		}
		
		return null;
	}

	private ProcessResult getProcessResult(Result result) {
		ProcessResult processResult = new ProcessResult();
		setProcessDataSetValue(result, processResult);
		return processResult;
	}

	private Result getResult(ProcessResult processResult) {
		Result result = new Result();
		setDataSetValue(processResult, result);
		return result;
	}

	private ProcessTask getProcessTask(Task task) {
		ProcessTask processTask = new ProcessTask();
		processTask.setId(task.getId());
		processTask.setParentId(task.getParentId());
		processTask.setName(task.getName());
		processTask.setPriority(task.getPriority());
		setProcessDataSetValue(task, processTask);
		return processTask;
	}
	
	private Task getTask(ProcessTask processTask) {
		Task task = new Task();
		task.setId(processTask.getId());
		task.setParentId(processTask.getParentId());
		task.setName(processTask.getName());
		task.setPriority(processTask.getPriority());
		setDataSetValue(processTask, task);
		return task;
	}

	private void setDataSetValue(ProcessDataSet processDataSet, DataSet dataSet) {
		DataObjectConverter converter = new DataObjectConverter();
		List<KeyValuePair> values = dataSet.getValues();
		Iterator<String> iter = processDataSet.getValues().keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Object value = processDataSet.get(key);
			KeyValuePair kv = new KeyValuePair();
			kv.setKey(key);
			DataObject dataObject = converter.getDataObject(value);
			kv.setValue(dataObject);
			values.add(kv);
		}
	}

	private void setProcessDataSetValue(DataSet dataSet,ProcessDataSet processDataSet){
		DataObjectConverter converter = new DataObjectConverter();
		List<KeyValuePair> values = dataSet.getValues();
		for (KeyValuePair keyValuePair : values) {
			Object value = converter.getValue(keyValuePair.getValue());
			processDataSet.put(keyValuePair.getKey(), value);
		}
	}


	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class[] getAdapterList() {
		return new Class[] { Task.class,ProcessTask.class,Result.class,ProcessResult.class };
	}

}
