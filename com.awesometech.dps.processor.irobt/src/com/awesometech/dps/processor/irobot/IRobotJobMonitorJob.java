package com.awesometech.dps.processor.irobot;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.awesometech.dps.processor.irobot.service.IRobotJobService;

public class IRobotJobMonitorJob implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("����ִ����");
		// TODO ������ò�Ҫֱ�������������Ǵ���һ��DPS����MonitorJob��������ʱ����Ĵ���״����¶������������һֱ��Ϊ�޷���صĺ�̨����
		IRobotJobService iroboJobService = new IRobotJobService();
		iroboJobService.handleIRobotsJobs();
	}

}
