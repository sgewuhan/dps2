package com.bizvpm.dps.processor.openoffice;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.jodconverter.office.LocalOfficeManager;

import com.bizvpm.dps.processor.openoffice.preferences.PreferenceConstants;
import com.bizvpm.dps.runtime.IProcessorActivator;

public class Starter implements IProcessorActivator {

	public static Starter starter;
	private LocalOfficeManager manager;

	public Starter() {
	}

	@Override
	public void startCheck() throws Exception {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String officeHomeBin = store.getString(PreferenceConstants.OFFICE_HOME_BIN);
		if (officeHomeBin == null || !new File(officeHomeBin).isFile())
			throw new Exception("Open Office转换器启动失败，请在属性页面中选择正确的soffice.bin文件。");
		String workingDir = store.getString(PreferenceConstants.WORKING_DIR);
		if (workingDir == null || !new File(workingDir).isDirectory())
			throw new Exception("Open Office转换器启动失败，请在属性页面中设置正确的工作目录。");
		String templateProfileDir = store.getString(PreferenceConstants.TEMPLATE_PROFILE_DIR);
		if (templateProfileDir == null || !new File(templateProfileDir).isDirectory())
			throw new Exception("Open Office转换器启动失败，请在属性页面中设置正确模板文件目录。");

		String portNumbers = store.getString(PreferenceConstants.PORT_NUMBERS);
		if (portNumbers == null || portNumbers.trim().isEmpty())
			throw new Exception("Open Office转换器启动失败，请在属性页面中设置正确的端口号。");
		String[] str = portNumbers.split(" ");
		if (str.length == 0)
			throw new Exception("Open Office转换器启动失败，请在属性页面中设置正确的端口号。");
		for (int i = 0; i < str.length; i++) {
			try {
				Integer.parseInt(str[i].trim());
			} catch (Exception e) {
				throw new Exception("Open Office转换器启动失败，请在属性页面中设置正确的端口号。");
			}
		}

	}

	@Override
	public void start() throws Exception {
		starter = this;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String officeHomeBin = store.getString(PreferenceConstants.OFFICE_HOME_BIN);
		String officeHome = new File(officeHomeBin).getParentFile().getParent();
		String workingDir = store.getString(PreferenceConstants.WORKING_DIR);
//		String templateProfileDir = store.getString(PreferenceConstants.TEMPLATE_PROFILE_DIR);
		long processTimeout = store.getLong(PreferenceConstants.PROCESS_TIMEOUT);
		if (processTimeout == 0)
			processTimeout = 120000L;
		long processRetryInterval = store.getLong(PreferenceConstants.PROCESS_RETRY_INTERVAL);
		if (processRetryInterval == 0)
			processTimeout = 250L;
		long taskExecutionTimeout = store.getLong(PreferenceConstants.TASK_EXECUTION_TIMEOUT);
		if (taskExecutionTimeout == 0)
			taskExecutionTimeout = 120000L;
		int maxTasksPerProcess = store.getInt(PreferenceConstants.MAX_TASKS_PER_PROCESS);
		if (maxTasksPerProcess == 0)
			maxTasksPerProcess = 200;
		long taskQueueTimeout = store.getLong(PreferenceConstants.TASK_QUEUE_TIMEOUT);
		if (taskQueueTimeout == 0)
			taskQueueTimeout = 30000L;
		String _portNumbers = store.getString(PreferenceConstants.PORT_NUMBERS);
		String[] str = _portNumbers.split(" ");
		int[] portNumbers = new int[str.length];
		for (int i = 0; i < str.length; i++) {
			try {
				portNumbers[i] = Integer.parseInt(str[i].trim());
			} catch (Exception e) {
			}
		}

		manager = LocalOfficeManager.builder()//
				.officeHome(officeHome)//
				.portNumbers(portNumbers)//
				.workingDir(workingDir)//
//				.templateProfileDir(templateProfileDir)//
				.killExistingProcess(true)//
				.processTimeout(processTimeout)//
				.processRetryInterval(processRetryInterval)//
				.taskExecutionTimeout(taskExecutionTimeout)//
				.maxTasksPerProcess(maxTasksPerProcess)//
				.taskQueueTimeout(taskQueueTimeout).build();
		manager.start();
	}

	@Override
	public void stop() throws Exception {
		manager.stop();
	}

	public static LocalOfficeManager getManager() {
		return starter.manager;
	}

}
