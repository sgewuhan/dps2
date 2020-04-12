package com.awesometech.dps.processor.irobot.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.awesometech.dps.processor.irobot.Activator;
import com.awesometech.dps.processor.irobot.preferences.IRobotPreferenceConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

/**
 * ����Job�ķ�����
 * 
 * @author Zhangjc
 *
 */
public class IRobotJobService {

	// �·�status ��I8�ٷ��ĵ��ṩ��״̬�б�
	private static final String JOB_STATUS_NEW = "N";
	private static final String JOB_STATUS_AVAILABLE = "A";
	private static final String JOB_STATUS_COMPLETED = "C";
	private static final String JOB_STATUS_RUNNING = "R";
	private static final String JOB_STATUS_STOPPED = "S";
	private static final String JOB_STATUS_KILLED = "K";
	private static final String JOB_STATUS_SUSPENDED = "P";

	private static final String JOB_ABORT_OK = "OK"; // ����abort�ɹ���ķ���ֵ

	private String serverIp;

	private String serverPort;

	private String userName;

	private String userPwd;
	
	private String pdmIp;
	
	private String pdmPort;

	private int connectTimeOut;

	private int jobTimeOut;

	private String getStatusUrl = "http://<IP>:<PORT>/servlet/GetJobStatus?jobid=<JOBID>&user=<USERNAME>&password=<PASSWORD>";

	private String abortJobUrl = "http://<IP>:<PORT>/servlet/AbortJob?jobid=<JOBID>&user=<USERNAME>&password=<PASSWORD>";
	
	private String pdmUrl = "http://<IP>:<PORT>/services";
	
	private String qedWorkPath = "";
	
	private String domain;
	
	private boolean mockup; 
	
	public void saveJob(String rfq_id, String jobId, String fileName, String hostId) {
		Document job = new Document().append("jobId", jobId).append("status", JOB_STATUS_NEW).append("fileName", fileName)
				.append("createDate", new Date()).append("hostId", hostId).append("rfqId", rfq_id);
		Activator.db().getCollection("irobotJob").insertOne(job);
	}

	public void handleIRobotsJobs() {
		init();
		List<Document> activeJobs = getActiveJobs(serverIp);
		// �ȴ���job���ٽ����������ص�PDM������
		List<Document> jobDataList = activeJobs.stream().map(j ->handlerStatus(j)).collect(Collectors.toList());
		if (null != jobDataList && jobDataList.size() > 0) {
			String pUrl = pdmUrl.replace("<IP>", pdmIp).replace("<PORT>", pdmPort);
			PdmClient.pushToPDM(pUrl, domain, jobDataList);
			// TODO ��Ҫ����PDM�Ľ������,�������ʧ�ܣ���Ҫ�´����ͣ�����job����Ҫ���ٽ���
			jobDataList.forEach(j -> updateJob(j));
		} 
	}
	
	public void updateJob(Document doc) {
		Activator.db().getCollection("irobotJob").updateOne(new BasicDBObject("_id",doc.getObjectId("_id")), new BasicDBObject("$set",doc));
	}

	/**
	 * ��ȡ���ڻ״̬�µ�job�б�
	 * 
	 * @param hostId
	 * @return
	 */
	private List<Document> getActiveJobs(String hostId) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("hostId", hostId).append("status", new Document("$in",Arrays.asList(new String[] {JOB_STATUS_NEW, JOB_STATUS_AVAILABLE, JOB_STATUS_RUNNING})))));
		return Activator.db().getCollection("irobotJob").aggregate(pipeline).into(new ArrayList<Document>());
	}

	// ����ͬ��״̬�ֱ��job���е�������
	private Document handlerStatus(Document job) {
		// Mockup Data
		if (mockup) {
			Document irobotQed = AdapterTool.readJsonFile("MockUpIRobotQed.json");
			Document qed = AdapterTool.readJsonFile("MockUpQed.json");
			job.append("status", JOB_STATUS_COMPLETED);
			job.append("qedData", new Document().append("irobotQed", irobotQed).append("qed", qed));
			return job;
		} 
		
		// �����洢http����ķ���ֵ��response[0]�Ƿ��ص����ݣ�response[1]�Ƿ��ص�cookie
		String[] response = new String[2];
		String jobId = job.getString("jobId");
		String statusUrl = getStatusUrl.replace("<IP>", serverIp).replace("<PORT>", serverPort).replace("<JOBID>", jobId)
				.replace("<USERNAME>", userName).replace("<PASSWORD>", userPwd);
		QedService qedService = new QedService();
		try {
			HttpService.callIRobot(statusUrl, response[1], connectTimeOut, (r, c) -> {
				response[0] = r;
				response[1] = c;
			});
		} catch (IOException e) {
//			throw new Exception("Fail to get Job status.");  //��̨�����쳣����Ҳ�޷���Ч�Ĵ�������ֱ�������־�󷵻�
			e.printStackTrace();
		}
		String status = getJobStatus(response[0]);
		if ("".equals(status)) {
			// �����ǰ״̬�޷�ʶ�����Խ�����ֱ����ֹ
			String abortReuslt = abortJob(jobId);
			// FIXME
			// �������һ�����գ�(1)���ֻ����abort�ɹ����޸�״̬Ϊstopped�����ܴ���������һֱabort���ɹ���ÿ�ζ�����ѯ�б��������abort��������������Ѿ�����I8�б����ˣ�abort����Զ����ɹ���
			// (2)���ֱ��abort�ͽ�����״̬�޸�Ϊstopped������ܴ�������ʵ��û��abort�ɹ���������I8�����У����ҿ������������ʼ��ռ��license��
			// ���ǵ�dpsΪ��ȫ�ĺ�̨�������ǽ����÷���(2),I8��������������I8���н��
			job.append("status", JOB_STATUS_STOPPED);
			job.append("msg", abortReuslt);
		} else if (Arrays.asList(new String[] { JOB_STATUS_STOPPED, JOB_STATUS_KILLED, JOB_STATUS_SUSPENDED })
				.contains(status)) {
			// ״̬Ϊ�쳣ֹͣ,����Ҫ���κδ���
			job.append("status", status);
		} else if (Arrays.asList(new String[] { JOB_STATUS_NEW, JOB_STATUS_AVAILABLE, JOB_STATUS_RUNNING })
				.contains(status)) {
			// FIXME ״̬Ϊ��������ߴ����У������䴦��ʱ������жϣ��ж��Ƿ���ʱ,�����������жϷ�����
			// (1)�ύI8��ʼ��������ʱ�䣬
			// (2)��I8��ʼ�������ʱ��
			// ����PDM��˵�������ύ��ʼ�Ϳ�ʼ��������ȴ���ʱ�ˣ�����Ŀǰ���շ���(1)�ύI8��ʼ��������ʱ��
			long jobTime = new Date().getTime() - job.getDate("createDate").getTime();
			// ����ʱ����������
			if (jobTime >= jobTimeOut) {
				String abortReuslt = abortJob(jobId);
				job.append("status", JOB_STATUS_STOPPED);
				job.append("msg", abortReuslt);
			} else {
				job.append("status", status);
			}

		} else {
			// ʣ�µ�ΪJOB_STATUS_COMPLETED״̬
			job.append("status", status);
			try {
				Document doc = qedService.hanldeQed(jobId, "");
				job.append("qedData", doc);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return job;
	}
	

	private void init() {
		serverIp = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_IP);
		serverPort = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_PORT);
		userName = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_USERNAME);
		userPwd = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_USERPWD);
		connectTimeOut = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.IRobot_TIMEOUT);
		jobTimeOut = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.IRobot_JOBTIMEOUT);
		pdmIp = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.PDM_IP);
		pdmPort = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.PDM_PORT);
		domain = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.PDM_DOMAIN);
		mockup = Activator.getDefault().getPreferenceStore().getBoolean(IRobotPreferenceConstants.MOCKUP);
//		qedWorkPath = Activator.getDefault().getPreferenceStore().getString(IRobotPreferenceConstants.IRobot_QED_PATH);
	}

	// ״̬Ϊ NACRSKP��֮һ�������޷�ʶ�𣬲�����һ��""
	private String getJobStatus(String response) {
		if (null == response) {
			return "";
		}
		String pattern = "StatusCode\\s+:\\s+[NACRSKP]";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(response);
		if (m.find()) {
			return m.group(0).replaceAll(" ", "").replace("StatusCode:", "");
		}
		return "";
	}

	private String abortJob(String jobId) {
		String url = abortJobUrl.replace("<IP>", serverIp).replace("<PORT>", serverPort).replace("<JOBID>", jobId)
				.replace("<USERNAME>", userName).replace("<PASSWORD>", userPwd);
		// �����洢http����ķ���ֵ��response[0]�Ƿ��ص����ݣ�response[1]�Ƿ��ص�cookie
		String[] response = new String[2];
		try {
			HttpService.callIRobot(url, response[1], connectTimeOut, (r, c) -> {
				response[0] = r;
				response[1] = c;
			});
		} catch (IOException e) {
			return ("Fail to abort Job:" + e.getMessage());
		}
		return response[0];
	}
}
