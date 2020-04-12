package com.awesometech.dps.processor.irobot;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.awesometech.dps.processor.irobot.service.IRobotJobService;

public class IRobotJobMonitorJob implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("任务执行中");
		// TODO 这里最好不要直接启动处理，而是创建一个DPS任务（MonitorJob），将定时任务的处理状况暴露出来，而不是一直作为无法监控的后台任务
		IRobotJobService iroboJobService = new IRobotJobService();
		iroboJobService.handleIRobotsJobs();
	}

}
