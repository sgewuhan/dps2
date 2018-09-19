package com.bizvpm.dps.client;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.AsyncHandler;

public class DPS {

	private String serviceUrl;
	private IProcessorManager manager;

	/**
	 * 
	 * @param serviceUrl
	 *            ·þÎñÆ÷URL
	 */
	public DPS(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public IProcessorManager getProcessorManager() throws Exception {
		if (manager == null) {
			DPServer server = new DPServerService(new URL(serviceUrl)).getDPServerPort();
			String procUrl = server.getProcessService();
			ProcessorManagerService service = new ProcessorManagerService(new URL(procUrl));
			manager = service.getProcessorManagerPort();
		}
		return manager;
	}

	public Map<String, Object> runTask(Task task, String processorTypeId) throws Exception {
		IProcessorManager manager = getProcessorManager();
		Result result = manager.runTask(task, processorTypeId);
		return getProcessResult(result);
	}

	public void scheduleTask(Task task, String processorTypeId) throws Exception {
		IProcessorManager manager = getProcessorManager();
		manager.scheduleTask(task, processorTypeId);
	}

	public void runAsyncTask(Task task, String processorTypeId, AsyncHandler<Result> asyncHandler) throws Exception {
		IProcessorManager manager = getProcessorManager();
		manager.runAsync(task, processorTypeId, asyncHandler);
	}

	private Map<String, Object> getProcessResult(DataSet dataSet) {
		ClientDataObjectConverter converter = new ClientDataObjectConverter();
		HashMap<String, Object> result = new HashMap<String, Object>();
		List<KeyValuePair> values = dataSet.getValues();
		for (KeyValuePair keyValuePair : values) {
			Object value = converter.getValue(keyValuePair.getValue());
			result.put(keyValuePair.getKey(), value);
		}

		return result;
	}
}
