package com.awesometech.dps.processor.irobot;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.awesometech.dps.processor.irobot.preferences.IRobotPreferenceConstants;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.client.MongoDatabase;

public class Activator extends AbstractUIPlugin {

	private static Activator plugin;

	public static MongoDatabase database;

	public MongoClient client;

	private SchedulerFactory schedulerFactory;

	private Scheduler scheduler;

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	public static Activator getDefault() {
		return plugin;
	}

	public MongoClient getClient() {
		if (client == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			int connsPerHost = store.getInt(IRobotPreferenceConstants.DB_CONNECTIONS_PER_HOST);
			int maxWaitTime = store.getInt(IRobotPreferenceConstants.DB_MAX_WAIT_TIME);
			int sockerTimeout = store.getInt(IRobotPreferenceConstants.DB_SOCKET_TIMEOUT);
			int connTimeout = store.getInt(IRobotPreferenceConstants.DB_CONNECT_TIMEOUT);
			int threadsAllowedToBlockForConnectionMultiplier = store
					.getInt(IRobotPreferenceConstants.DB_THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER);

			// requied
			int port = store.getInt(IRobotPreferenceConstants.DB_PORT);
			if (port == IRobotPreferenceConstants.DB_UNSET) {
				throw new IllegalArgumentException("缺少端口号设置");
			}

			String host = store.getString(IRobotPreferenceConstants.DB_IP);
			if (host.isEmpty()) {
				throw new IllegalArgumentException("缺少数据库主机设置");
			}

			Builder builder = MongoClientOptions.builder();
			if (connsPerHost != 0) {
				builder.connectionsPerHost(connsPerHost); // $NON-NLS-1$
			}
			if (maxWaitTime != 0) {
				builder.maxWaitTime(maxWaitTime); // $NON-NLS-1$
			}
			if (sockerTimeout != 0) {
				builder.socketTimeout(sockerTimeout); // $NON-NLS-1$
			}
			if (connTimeout != 0) {
				builder.connectTimeout(connTimeout); // $NON-NLS-1$
			}
			if (threadsAllowedToBlockForConnectionMultiplier != 0) {
				builder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier); // $NON-NLS-1$
			}

			ServerAddress address = new ServerAddress(host, port);
			client = new MongoClient(address, builder.build());
		}
		return client;
	}

	public MongoDatabase getDB() {
		if (database == null) {
			client = getClient();
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			String dbname = store.getString(IRobotPreferenceConstants.DB);
			if (dbname.isEmpty()) {
				throw new IllegalArgumentException("缺少数据库名称设置");
			}
			database = client.getDatabase(dbname);
		}
		return database;
	}

	public static MongoDatabase db() {
		return getDefault().getDB();
	}

	public void dbClientClose() {
		getDefault().getClient().close();
	}
	
	// TODO 还是需要考虑将执行状态写入数据库，后台任务无法监听，如果挂掉了将无人知晓
	public void createMonitorJob() throws SchedulerException {
		int monotorInterval = Activator.getDefault().getPreferenceStore().getInt(IRobotPreferenceConstants.IRobot_MONITOR_INTERVAL);
		schedulerFactory = new StdSchedulerFactory();
	    scheduler = schedulerFactory.getScheduler();
		JobDetail jobDetail = JobBuilder.newJob(IRobotJobMonitorJob.class)
                .withIdentity("irobotJobMonitor", "irobotJob").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("monitorTrigger", "monitorTriggerGroup").startNow()//立即生效
				.withSchedule(SimpleScheduleBuilder.simpleSchedule()
				.withIntervalInSeconds(monotorInterval) 
				.repeatForever()).build();
		
		scheduler.scheduleJob(jobDetail, trigger);
		System.out.println("--------scheduler start ! ------------");
		scheduler.start();
	}
	
	public void stopMonitorJob() throws SchedulerException {
		if(scheduler.isStarted()) {
			scheduler.shutdown();
		}
	}
}