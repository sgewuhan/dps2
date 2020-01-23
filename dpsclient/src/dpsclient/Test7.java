package dpsclient;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.IProcessorManager;
import com.bizvpm.dps.client.Result;
import com.bizvpm.dps.client.Task;

public class Test7 {

	private static final String serverUrl = "http://127.0.0.1:8199/dps/server?wsdl";
	private static DPS dps;
	private static String type = "com.bizvpm.dps.processor.pmsvis:pms.visualservice";
	private static String type1 = "com.bizvpm.dps.processor.pmsvis:pms.pdfgen";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		dps = new DPS(serverUrl);
		testRun("����PDF");

	}


	/**
	 * �����ͬ������
	 * 
	 * @param name
	 */
	public static void testRun(String name) {
		try {
			
			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			task.setValue("domain", "bvs_std");
			task.setValue("col", "docuFile");
			task.setValue("target", "docuFile_preview");
			task.setValue("_id", "5e251e3549acc94dec4f91e3");

			final IProcessorManager manager = dps.getProcessorManager();
			Result result = manager.runTask(task, type1);
			result.writeToFile("result", new File("d:/" + name + ".pdf"));

			
			result = manager.runTask(task, type);
			Object msg = result.getValue("result");
			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * ���Զ��߳�
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
	 * �����ͬ�����ã��ͻ����첽
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
	 * ������첽����
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
