package com.awesometech.dps.processor.irobot;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import com.awesometech.dps.processor.irobot.preferences.IRobotPreferenceConstants;
import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class CreateJob implements IProcessorRunable {

	private File inputFile;

	private String serverUrl;

	private String userName;

	private String userPwd;

	private int timeOut;
	
	private String loginUrl = "Login.do?LoginName=<USERNAME>&Password=<PASSWORD>&state=processLogin";
	
	private String submitUrl = "QueueWorkflow.do?state=queueWorkflow&workflowClass=com.maniabarco.autoflow.workflow.JobProfilerWorkflow&action=upload"
			+ "&uploadFile=<UPLOADFILE>&I8_Customer=foobar";

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		// TODO Auto-generated method stub
		ProcessResult r = new ProcessResult();
		int jobId = -1;
		init(processTask);
		if (inputFile.exists()) {
			try {
				jobId = createJob();
			}catch(Exception e) {
				throw e;
			}finally {
				// 文件提交操作结束后，如果临时文件还存在，则将其干掉
				if (inputFile.exists())
					inputFile.delete();
			}

		}
		r.put("jobId", jobId);
		return r;
	}

	private void init(ProcessTask processTask) throws Exception {

		long time = new Date().getTime();
		String pathName = DPSUtil.getTempDirector(getClass(), true);
		String fileType = (String) processTask.get("fileType");
		inputFile = new File(pathName + time + "." + fileType);
		serverUrl = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.URL);
		userName = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.USERNAME);
		userPwd = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.USERPWD);
		timeOut = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.TIMEOUT);
		processTask.writeToFile("engineeringFiles", inputFile);

	}

	// TODO 创建Job，返回jobid，如果jobid为空，返回-1
	private int createJob() throws Exception {
		String[] response = new String[2]; // 用来存储http请求的返回值，response[0]是返回的数据，response[1]是返回的cookie

		// 登录，请求获取Cookie
		loginUrl = serverUrl + loginUrl.replace("<USERNAME>", userName).replace("<PASSWORD>", userPwd);
		try {
			HttpServices.callIRobot(loginUrl, null, timeOut, (r, c) -> {
				response[0] = r;
				response[1] = c;
			});
		} catch (IOException e) {
			throw new Exception("Fail to get Cookie.");
		}

		// 提交文件到IRobot处理
		submitUrl = serverUrl + submitUrl.replace("<UPLOADFILE>", inputFile.getAbsolutePath());
		try {
			HttpServices.callIRobot(submitUrl, response[1], timeOut, (r, c) -> {
				response[0] = r;
				response[1] = c;
			});
		} catch (IOException e) {
			throw new Exception("Fail to submit Job.");
		}
		return getJobNumber(response[0]);
	}
	
	/**
	 * 从response的String中获取对应的JobNumber
	 * @param response
	 * @return
	 */
	// TODO 需要让第三方软件改进，这里直接返回的是一个结构化的数据，或者就直接返回一个job的id，其他东西都不要，则下面的逻辑可以做简化
	private int getJobNumber(String response) {
		if(null == response) {
			return -1;
		}
		String pattern = "(Queued workflow having JobNumber:\\s+\\d+</td>)";
		String pattern2 =  "\\d+";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(response);
		if (m.find()) {
			r = Pattern.compile(pattern2);
			m = r.matcher(m.group(0));
			if (m.find()) {
				return Integer.valueOf(m.group(0));
			}
		} 
		return -1;
	}

}
