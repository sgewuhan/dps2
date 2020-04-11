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
 * 管理Job的服务类
 * 
 * @author Zhangjc
 *
 */
public class IRobotJobService {

	// 下方status 是I8官方文档提供的状态列表
	private static final String JOB_STATUS_NEW = "N";
	private static final String JOB_STATUS_AVAILABLE = "A";
	private static final String JOB_STATUS_COMPLETED = "C";
	private static final String JOB_STATUS_RUNNING = "R";
	private static final String JOB_STATUS_STOPPED = "S";
	private static final String JOB_STATUS_KILLED = "K";
	private static final String JOB_STATUS_SUSPENDED = "P";

	private static final String JOB_ABORT_OK = "OK"; // 任务abort成功后的返回值

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
		// 先处理job，再将处理结果返回到PDM的数据
		List<Document> jobDataList = activeJobs.stream().map(j ->handlerStatus(j)).collect(Collectors.toList());
		if (null != jobDataList && jobDataList.size() > 0) {
			String pUrl = pdmUrl.replace("<IP>", pdmIp).replace("<PORT>", pdmPort);
			PdmClient.pushToPDM(pUrl, domain, jobDataList);
			// TODO 需要考虑PDM的接收情况,如果推送失败，需要下次推送，但是job不需要做再解析
			jobDataList.forEach(j -> updateJob(j));
		} 
	}
	
	public void updateJob(Document doc) {
		Activator.db().getCollection("irobotJob").updateOne(new BasicDBObject("_id",doc.getObjectId("_id")), new BasicDBObject("$set",doc));
	}

	/**
	 * 获取处于活动状态下的job列表
	 * 
	 * @param hostId
	 * @return
	 */
	private List<Document> getActiveJobs(String hostId) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("hostId", hostId).append("status", new Document("$in",Arrays.asList(new String[] {JOB_STATUS_NEW, JOB_STATUS_AVAILABLE, JOB_STATUS_RUNNING})))));
		return Activator.db().getCollection("irobotJob").aggregate(pipeline).into(new ArrayList<Document>());
	}

	// 按不同的状态分别对job进行单独处理
	private Document handlerStatus(Document job) {
		// Mockup Data
		if (mockup) {
			Document irobotQed = AdapterTool.readJsonFile("MockUpIRobotQed.json");
			Document qed = AdapterTool.readJsonFile("MockUpQed.json");
			job.append("status", JOB_STATUS_COMPLETED);
			job.append("qedData", new Document().append("irobotQed", irobotQed).append("qed", qed));
			return job;
		} 
		
		// 用来存储http请求的返回值，response[0]是返回的数据，response[1]是返回的cookie
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
//			throw new Exception("Fail to get Job status.");  //后台任务，异常外抛也无法有效的处理，这里直接输出日志后返回
			e.printStackTrace();
		}
		String status = getJobStatus(response[0]);
		if ("".equals(status)) {
			// 如果当前状态无法识别，则尝试将任务直接终止
			String abortReuslt = abortJob(jobId);
			// FIXME
			// 这里存在一个风险：(1)如果只有在abort成功后修改状态为stopped，可能存在有任务一直abort不成功，每次都在轮询列表里面进行abort，并且如果任务已经不在I8列表中了，abort将永远不会成功；
			// (2)如果直接abort就将任务状态修改为stopped，则可能存在任务实际没有abort成功，还挂在I8上运行，并且可能阻塞在那里，始终占这license；
			// 考虑到dps为完全的后台任务，我们将采用方案(2),I8的任务阻塞将由I8自行解决
			job.append("status", JOB_STATUS_STOPPED);
			job.append("msg", abortReuslt);
		} else if (Arrays.asList(new String[] { JOB_STATUS_STOPPED, JOB_STATUS_KILLED, JOB_STATUS_SUSPENDED })
				.contains(status)) {
			// 状态为异常停止,不需要做任何处理
			job.append("status", status);
		} else if (Arrays.asList(new String[] { JOB_STATUS_NEW, JOB_STATUS_AVAILABLE, JOB_STATUS_RUNNING })
				.contains(status)) {
			// FIXME 状态为待处理或者处理中，将对其处理时间进行判断，判断是否处理超时,这里有两种判断方案：
			// (1)提交I8开始计算任务时间，
			// (2)以I8开始处理计算时间
			// 对于PDM来说，任务提交开始就开始进入任务等待计时了，所以目前按照方案(1)提交I8开始计算任务时间
			long jobTime = new Date().getTime() - job.getDate("createDate").getTime();
			// 任务超时，挂起任务
			if (jobTime >= jobTimeOut) {
				String abortReuslt = abortJob(jobId);
				job.append("status", JOB_STATUS_STOPPED);
				job.append("msg", abortReuslt);
			} else {
				job.append("status", status);
			}

		} else {
			// 剩下的为JOB_STATUS_COMPLETED状态
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

	// 状态为 NACRSKP中之一，否则将无法识别，并返回一个""
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
		// 用来存储http请求的返回值，response[0]是返回的数据，response[1]是返回的cookie
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
