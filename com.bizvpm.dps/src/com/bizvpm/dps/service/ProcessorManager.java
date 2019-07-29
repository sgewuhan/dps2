package com.bizvpm.dps.service;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.bizvpm.dps.Activator;
import com.bizvpm.dps.Log;
import com.bizvpm.dps.runtime.IProcessContext;
import com.sun.net.httpserver.HttpExchange;

@WebService(endpointInterface = "com.bizvpm.dps.service.IProcessorManager")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ProcessorManager extends PersistableProcessorManager implements IProcessorManager {

	private static final int STATUS_ONLINE = 1;

	private static final int STATUS_OFFLINE = 0;

	private static final String ERR_NO_PROCESSOR = "ERR_NO_PROCESSOR";

	private static final String ERR_NO_PROCESSOR_CONFIG = "ERR_NO_PROCESSOR_CONFIG";

	private static final String ILLEGAL_TASK_PROPRITY = "ILLEGAL_TASK_PROPRITY";

	private static final String ERR_RUNTIME_ERROR = "ERR_RUNTIME_ERROR";

	private static final String STATUS_TASK_DONE = "done";

	private static final String STATUS_TASK_ERROR = "error";

	private IPersistence persistence;

	private Map<String, ProcessCounter> counters;

	@Resource
	private WebServiceContext wsContext;

	private int status;

	public void setPersistence(IPersistence persistence) {
		this.persistence = persistence;
	}

	public void online() {
		registerProcessors();
		initProcessorGroup();
	}

	@Override
	public ProcessorList getProcessors() {
		return persistence.getProcessorList(STATUS_ONLINE);
	}

	private void registerProcessors() {
		List<PersistableProcessor> processorConfigs = getProcessorConfigs();

		IExtensionRegistry eReg = Platform.getExtensionRegistry();
		IExtensionPoint ePnt = eReg.getExtensionPoint(Activator.PLUGIN_ID, "processor");
		if (ePnt == null) {
			return;
		}
		IExtension[] exts = ePnt.getExtensions();
		for (int i = 0; i < exts.length; i++) {
			IConfigurationElement[] confs = exts[i].getConfigurationElements();
			for (int j = 0; j < confs.length; j++) {
				if ("processor".equals(confs[j].getName())) {
					ProcessorConfig processorConfig = new ProcessorConfig(confs[j]);
					try {
						processorConfig.startCheck();
						processorConfig.start();
					} catch (Exception e) {
						Log.logError(e);
					}
					processorConfigs.add(processorConfig);
				}
			}
		}
		this.status = STATUS_ONLINE;
		updateStatus();
	}

	private void updateStatus() {
		persistence.updateProcessManagerStatus(this, status);
	}

	private void initProcessorGroup() {
		counters = new HashMap<String, ProcessCounter>();
		List<PersistableProcessor> configs = getProcessorConfigs();
		for (int i = 0; i < configs.size(); i++) {
			ProcessorConfig conf = (ProcessorConfig) configs.get(i);
			int maxCount = conf.getMaxThreadCount();
			String processorTypeId = conf.getId();
			counters.put(processorTypeId, new ProcessCounter(maxCount));
		}
	}

	public void offline() {
		List<PersistableProcessor> configs = getProcessorConfigs();
		for (int i = 0; i < configs.size(); i++) {
			ProcessorConfig conf = (ProcessorConfig) configs.get(i);
			try {
				conf.stop();
			} catch (Exception e) {
				Log.logError(e);
			}
		}
		this.status = STATUS_OFFLINE;
		updateStatus();
	}

	@Override
	public void scheduleTask(Task task, String processorTypeId) throws ProcessException {
		IProcessorManager manager = lookupManager(processorTypeId, true);
		if (manager == null) {
			throw createException(ERR_NO_PROCESSOR, "No processor register for type " + processorTypeId);
		}
		manager.schedule(task, processorTypeId);
	}

	@Override
	public ParameterList lookupParameters(String processorTypeId) throws ProcessException {
		IProcessorManager manager = lookupManager(processorTypeId, true);
		if (manager == null) {
			throw createException(ERR_NO_PROCESSOR, "No processor register for type " + processorTypeId);
		}
		return manager.getParameters(processorTypeId);
	}

	@Override
	public ParameterList getParameters(String processorTypeId) throws ProcessException {
		ProcessorConfig config = getProcessorConfig(processorTypeId);
		if (config == null) {
			// 这种情况不应出现，记录到系统日志
			ProcessException e = createException(ERR_NO_PROCESSOR_CONFIG,
					"Lost processor config, processor type id:" + processorTypeId + ", Server:" + getHost());
			throw e;
		}
		return config.getParameterList();
	}

	@Override
	public void schedule(Task task, String processorTypeId) throws ProcessException {
		runInternal(task, processorTypeId, false);
	}

	@Override
	public Result run(Task task, String processorTypeId) throws ProcessException {
		return runInternal(task, processorTypeId, true);
	}

	private Result runInternal(final Task task, final String processorTypeId, boolean isblock) throws ProcessException {
		// 检测Task合法性
		int priority = task.getPriority();
		if (priority != Job.BUILD && priority != Job.DECORATE && priority != Job.INTERACTIVE && priority != Job.LONG
				&& priority != Job.SHORT) {
			throw createException(ILLEGAL_TASK_PROPRITY, "Illegal task proprity, only 10,20,30,40,50 can be used");
		}
		ProcessorConfig config = getProcessorConfig(processorTypeId);
		if (config == null) {
			// 这种情况不应出现，记录到系统日志
			ProcessException e = createException(ERR_NO_PROCESSOR_CONFIG,
					"Lost processor config, processor type id:" + processorTypeId + ", Server:" + getHost());
			Log.logError(e);
			throw e;
		}
		final String name = config.getName() + ": " + task.getName();

		// 获取客户端信息
		InetAddress address = getClientAddress();
		if (address != null) {
			String clientName = address.getHostName();
			String clientIp = address.getHostAddress();
			task.setClientName(clientName);
			task.setClientIp(clientIp);
		} else {
			task.setClientName("localhost");
			task.setClientIp("127.0.0.1");
		}

		final String taskId = persistence.createTask(getHost(), processorTypeId, task);
		task.setId(taskId);

		Log.logInfo("Received a task:" + name + ",id:" + taskId + ",type:" + processorTypeId);
		IProcessContext context = new ProcessContext(this, task, config);
		final ProcessJob job = new ProcessJob(name, context) {
			@Override
			public void done(IJobChangeEvent event) {
				IStatus result = getResult();
				Throwable error = result.getException();
				Result pr = getProcessResult();
				if (error != null) {
					String message = result.getMessage();
					if (message == null) {
						message = "Runtime Error.";
					}
					persistence.updateTaskStatusWithMessage(taskId, pr, STATUS_TASK_ERROR, message);
				} else {
					persistence.updateTaskStatus(taskId, pr, STATUS_TASK_DONE);
				}
				Log.logOK("Task done:" + name + ",id:" + taskId + ",type:" + processorTypeId);
				super.done(event);
			}
		};
		job.setPriority(priority);
		ProcessCounter counter = counters.get(processorTypeId);
		job.setCounter(counter);
		job.schedule();
		if (isblock) {
			try {
				job.join();
				IStatus result = job.getResult();
				Throwable error = result.getException();
				if (error != null) {
					String message = result.getMessage();
					ProcessException e = createException(ERR_RUNTIME_ERROR, "Runtime error: " + message
							+ ", processor type id:" + processorTypeId + ", Server:" + getHost());
					throw e;
				}
			} catch (InterruptedException e) {
				Log.logError(e);
			}
		}

		return job.getProcessResult();
	}

	private IProcessorManager lookupManager(String processorTypeId, boolean failover) {
		StringList hosts = persistence.getOnlineProcessManager(processorTypeId);
		if (hosts == null || hosts.getItems().size() == 0) {
			return null;
		}

		IProcessorManager bestChoice = null;
		double bestChoiceScore = 0f;

		List<String> hostsItem = hosts.getItems();
		List<String> faults = new ArrayList<String>();

		Map<IProcessorManager, ProcessorState> running = new HashMap<IProcessorManager, ProcessorState>();

		for (int i = 0; i < hostsItem.size(); i++) {
			String host = hostsItem.get(i);
			try {
				IProcessorManager manager = createProcessorManager(host);
				double score = manager.getProformenceScore();

				if (manager.isIdle()) {
					// 在空闲处理机上比较性能评分
					if (bestChoiceScore == 0f || score > bestChoiceScore) {
						bestChoice = manager;
						bestChoiceScore = score;
					}
				} else {
					ProcessorState state = manager.getProcessorState(processorTypeId);
					state.setScore(score);
					running.put(manager, state);
				}
			} catch (Exception e) {
				if (failover) {
					faults.add(host);
				}
				Log.logWarning("Service failed :" + host);
			}
		}
		if (bestChoice != null) {
			if (failover && !faults.isEmpty()) {
				failover(faults);
			}
			return bestChoice;
		}
		bestChoiceScore = 0f;

		// 取出等待
		Map<IProcessorManager, ProcessorState> buzy = new HashMap<IProcessorManager, ProcessorState>();
		Iterator<IProcessorManager> iter = running.keySet().iterator();
		while (iter.hasNext()) {
			IProcessorManager manager = iter.next();
			ProcessorState state = running.get(manager);
			double score = state.getScore();
			if (state.getMax() - state.getRunning() > 0) {
				// 在空闲处理机上比较性能评分
				if (bestChoiceScore == 0f || score > bestChoiceScore) {
					bestChoice = manager;
					bestChoiceScore = score;
				}
			} else {
				buzy.put(manager, state);
			}
		}

		if (bestChoice != null) {
			if (failover && !faults.isEmpty()) {
				failover(faults);
			}
			return bestChoice;
		}
		bestChoiceScore = 0f;

		// 取忙的
		Float bestRatio = null;
		iter = buzy.keySet().iterator();
		while (iter.hasNext()) {
			IProcessorManager manager = iter.next();
			ProcessorState state = buzy.get(manager);
			float ratio = 1f * state.getWaiting() / state.getMax();
			if (bestRatio == null || ratio <= bestRatio) {
				double score = state.getScore();
				if (bestChoiceScore == 0f || score > bestChoiceScore) {
					bestChoice = manager;
					bestChoiceScore = score;
				}
			}
		}

		if (failover && !faults.isEmpty()) {
			failover(faults);
		}
		return bestChoice;
	}

	private void failover(final List<String> fault) {
		Job job = new Job("Failover") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				StringList hostList = new StringList();
				hostList.getItems().addAll(fault);
				persistence.updateProcessManagerStatusByHosts(hostList, STATUS_OFFLINE);
				for (String faultHost : fault) {
					failover(faultHost);
				}
				return Status.OK_STATUS;
			}

			void failover(String faultHost) {
				TaskList list = persistence.getTaskList(faultHost);
				List<PersistableTask> tasks = list.getTasks();
				for (PersistableTask pTask : tasks) {
					String processorTypeId = pTask.getProcessorTypeId();
					Task task = new Task();
					task.setName(pTask.getName());
					task.setPriority(pTask.getPriority());
					task.getValues().addAll(pTask.getValues());
					try {
						IProcessorManager manager = lookupManager(processorTypeId, false);
						if (manager != null) {
							manager.scheduleTask(task, processorTypeId);
							persistence.removeTask(pTask.getId());
							Log.logInfo("Failover reschedule: " + pTask.getId());
						} else {
							Log.logError(createException(ERR_NO_PROCESSOR,
									"Failover failure, no processor: " + pTask.getId()));
						}
					} catch (ProcessException e) {
						Log.logError("Failover failure: " + pTask.getId(), e);
					}
				}
			}
		};
		job.schedule();
	}

	private IProcessorManager createProcessorManager(String host) throws MalformedURLException {
		if (host.equals(getHost())) {
			return this;
		}
		String url = "http://" + persistence.getHostAddress(host) + "/processor?wsdl";
		ProcessorManagerService service = new ProcessorManagerService(new URL(url));
		return service.getProcessorManagerPort();
	}

	@Override
	public Result runTask(Task task, String processorTypeId) throws ProcessException {
		IProcessorManager manager = lookupManager(processorTypeId, true);
		if (manager == null) {
			throw createException(ERR_NO_PROCESSOR, "No processor register for type " + processorTypeId);
		}
		return manager.run(task, processorTypeId);
	}

	private ProcessException createException(String code, String message) {
		ProcessFault fault = new ProcessFault();
		fault.setCode(code);
		fault.setMessage(message);
		ProcessException e = new ProcessException(message, fault);
		return e;
	}

	@Override
	public boolean isIdle() {
		return Job.getJobManager().isIdle();
	}

	@Override
	public double getProformenceScore() {
		return 0f;
//		Sigar sigar = new Sigar();
//
//		// 假定4G内存，100%CPU空闲为100分
//		try {
//			CpuPerc cpuPerc = sigar.getCpuPerc();
//			double cpuScore = 0.5 * cpuPerc.getIdle();
//
//			Mem mem = sigar.getMem();
//			long free = mem.getFree() / (1024 * 1024l);
//			double memScore;
//			if (free > 4096l) {
//				memScore = 0.5d;
//			} else {
//				memScore = 0.5d * free / 4096l;
//			}
//
//			return cpuScore + memScore;
//		} catch (SigarException e) {
//			Log.logError(e);
//			return 0f;
//		}
	}

	@Override
	public ProcessorState getProcessorState(String processorTypeId) throws ProcessException {
		int running = 0;
		int waiting = 0;
		int max = 0;
		Job[] jobs = Job.getJobManager().find(processorTypeId);
		for (int i = 0; i < jobs.length; i++) {
			if (Job.RUNNING == jobs[i].getState()) {
				running++;
			} else if (Job.WAITING == jobs[i].getState()) {
				waiting++;
			}
		}

		ProcessorConfig conf = getProcessorConfig(processorTypeId);
		if (conf == null) {
			throw createException(ERR_NO_PROCESSOR_CONFIG,
					"Lost processor config, processor type id:" + processorTypeId + ", Server:" + getHost());
		}
		max = conf.getMaxThreadCount();
		ProcessorState state = new ProcessorState();
		state.setRunning(running);
		state.setWaiting(waiting);
		state.setMax(max);
		return state;
	}

	public ProcessorConfig getProcessorConfig(String processorTypeId) {
		List<PersistableProcessor> configs = getProcessorConfigs();
		for (int i = 0; i < configs.size(); i++) {
			ProcessorConfig config = (ProcessorConfig) configs.get(i);
			if (config.id.equalsIgnoreCase(processorTypeId)) {
				return config;
			}
		}
		return null;
	}

	public InetAddress getClientAddress() {
		try {
			MessageContext mc = wsContext.getMessageContext();
			HttpExchange client = (HttpExchange) mc.get("com.sun.xml.internal.ws.http.exchange");
			return client.getRemoteAddress().getAddress();
		} catch (Exception e) {
			try {
				return InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				return null;
			}
		}
	}

	public void startProcessor(ProcessorConfig config) throws Exception {
		config.startCheck();
		config.start();
		updateStatus();
	}

	public void stopProcessor(ProcessorConfig config) throws Exception {
		config.stop();
		updateStatus();
	}

}
