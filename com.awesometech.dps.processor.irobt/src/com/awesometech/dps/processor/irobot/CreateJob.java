package com.awesometech.dps.processor.irobot;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import com.awesometech.dps.processor.irobot.preferences.IRobotPreferenceConstants;
import com.awesometech.dps.processor.irobot.service.HttpService;
import com.awesometech.dps.processor.irobot.service.IRobotJobService;
import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class CreateJob implements IProcessorRunable {

	private File inputFile;

	private String serverIp;
	
	private String serverPort;

	private String userName;

	private String userPwd;

	private int timeOut;
	
	private String rfqId;
	
	private boolean mockup;
	
	private String loginUrl = "http://<IP>:<PORT>/Login.do?LoginName=<USERNAME>&Password=<PASSWORD>&state=processLogin";
	
	private String submitUrl = "http://<IP>:<PORT>/QueueWorkflow.do?state=queueWorkflow&workflowClass=com.maniabarco.autoflow.workflow.JobProfilerWorkflow&action=upload"
			+ "&uploadFile=<UPLOADFILE>&I8_Customer=foobar";

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		// TODO Auto-generated method stub
		ProcessResult r = new ProcessResult();
		String jobId = "-1";
		String fileName = "";
		init(processTask);
		if (inputFile.exists()) {
			try {
				fileName = inputFile.getName();
				if (mockup) {
					jobId = String.valueOf(System.currentTimeMillis());
				}else {
					jobId = createJob();
				}
			}catch(Exception e) {
				throw e;
			}finally {
				// �ļ��ύ���������������ʱ�ļ������ڣ�����ɵ�(����ύ�ɹ��ģ���ʱ�ļ��ᱻI8�Լ������)
				if (inputFile.exists())
					inputFile.delete();
			}
		}
		new IRobotJobService().saveJob(rfqId,jobId,fileName,serverIp);
		r.put("jobId", jobId);
		return r;
	}

	private void init(ProcessTask processTask) throws Exception {

		long time = new Date().getTime();
		String pathName = DPSUtil.getTempDirector(getClass(), true);
		String fileType = (String) processTask.get("fileType");
		rfqId = (String) processTask.get("rfqId");
		inputFile = new File(pathName + time + "." + fileType);
		serverIp = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_IP);
		serverPort = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_PORT);
		userName = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_USERNAME);
		userPwd = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_USERPWD);
		timeOut = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.IRobot_TIMEOUT);
		mockup = Activator.getDefault().getPreferenceStore().getBoolean(IRobotPreferenceConstants.MOCKUP);
		processTask.writeToFile("engineeringFiles", inputFile);

	}

	// ����Job������jobid�����jobidΪ�գ�����-1
	private String createJob() throws Exception {
		String[] response = new String[2]; // �����洢http����ķ���ֵ��response[0]�Ƿ��ص����ݣ�response[1]�Ƿ��ص�cookie

		// ��¼�������ȡCookie
		loginUrl = loginUrl.replace("<IP>", serverIp).replace("<PORT>", serverPort).replace("<USERNAME>", userName).replace("<PASSWORD>", userPwd);
		try {
			HttpService.callIRobot(loginUrl, null, timeOut, (r, c) -> {
				response[0] = r;
				response[1] = c;
			});
		} catch (IOException e) {
			throw new Exception("Fail to get Cookie.");
		}

		// �ύ�ļ���IRobot����
		submitUrl = submitUrl.replace("<IP>", serverIp).replace("<PORT>", serverPort).replace("<UPLOADFILE>", inputFile.getAbsolutePath());
		try {
			HttpService.callIRobot(submitUrl, response[1], timeOut, (r, c) -> {
				response[0] = r;
				response[1] = c;
			});
		} catch (IOException e) {
			throw new Exception("Fail to submit Job.");
		}
		return getJobNumber(response[0]);
	}
	
	/**
	 * ��response��String�л�ȡ��Ӧ��JobNumber
	 * @param response
	 * @return
	 */
	// TODO ��Ҫ�õ���������Ľ�������ֱ�ӷ��ص���һ���ṹ�������ݣ����߾�ֱ�ӷ���һ��job��id��������������Ҫ����������߼���������
	private String getJobNumber(String response) {
		if(null == response) {
			return "-1";
		}
		String pattern = "(Queued workflow having JobNumber:\\s+\\d+</td>)";
		String pattern2 =  "\\d+";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(response);
		if (m.find()) {
			r = Pattern.compile(pattern2);
			m = r.matcher(m.group(0));
			if (m.find()) {
				return m.group(0);
			}
		} 
		return "-1";
	}

}
