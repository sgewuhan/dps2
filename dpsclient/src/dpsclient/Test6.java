package dpsclient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.IProcessorManager;
import com.bizvpm.dps.client.Parameter;
import com.bizvpm.dps.client.Result;
import com.bizvpm.dps.client.Task;

public class Test6 {

	private static final String serverUrl = "http://127.0.0.1:8199/dps/server?wsdl";
	private static String type = "com.bizvpm.dps.processor.pm:costsummary.byprojectgroup";
	private static DPS dps;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		dps = new DPS(serverUrl);
		testRun("获取实际成本");

		// checkParameter();

		/*
		 * 测试计划任务
		 */
		// testSchedule("计划任务1");
		// testSchedule("计划任务2");
		// testSchedule("计划任务3");

		// testRun("阻断任务" + System.currentTimeMillis());

		// testMultiThread(30, "多线程测试");
		//
		// testRunAsync("异步任务" + System.currentTimeMillis());
		//
		// testRunFile("处理文件" + System.currentTimeMillis());
		//
		// testDispatchTask("dispatcher task");

		// testRunReport("Report Task");
	}


	/**
	 * 服务端同步调用
	 * 
	 * @param name
	 */
	public static void testRun(String name) {
		try {
			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			task.setValue("year", "2016");
			task.setValue("month", "2");

			Map<String, Object> result = dps.runTask(task, type);
			Object dbo = result.get("results");
			System.out.println(dbo);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void checkParameter() {
		try {
			IProcessorManager manager = dps.getProcessorManager();
			List<Parameter> list = manager.lookupParameters(type).getParameters();
			for (int i = 0; i < list.size(); i++) {
				System.out.println("Check Parameter............................");
				Parameter parameter = list.get(i);
				System.out.println("name:" + parameter.getName());
				System.out.println("type:" + parameter.getType());
				System.out.println("optional:" + parameter.isOptional());
				System.out.println("restriction:" + parameter.getRestrictions());
			}
			System.out.println("........................................");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 测试多线程
	 * 
	 * @param count
	 * @param name
	 */
	public static void testMultiThread(int count, final String name) {
		for (int i = 0; i < count; i++) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					testRun(name + " T:" + Thread.currentThread());
				}
			});
			t.start();
		}
	}


	/**
	 * 服务端同步调用，客户端异步
	 * 
	 * @param name
	 */
	public static void testRunAsync(String name) {
		try {
			final IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			task.setValue("year", "2016");
			task.setValue("month", "2");

//			Response<Result> response = manager.runTaskAsync(task, type);
			AsyncHandler<Result> asyncHandler = new AsyncHandler<Result>() {

				@Override
				public void handleResponse(Response<Result> res) {
					try {
						Result result = res.get();
						result.getValue("results");
						System.out.println(result.getValue("results"));
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			};
			manager.runTaskAsync(task, type, asyncHandler);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * 服务端异步调用
	 * 
	 * @param name
	 */
	public static void testSchedule(String name) {
		try {
			final IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setValue("year", "2016");
			task.setValue("month", "2");
			manager.scheduleTask(task, type);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

}
