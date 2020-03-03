package dpsclient;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.DataObject;
import com.bizvpm.dps.client.IProcessorManager;
import com.bizvpm.dps.client.Result;
import com.bizvpm.dps.client.Task;

public class Test7 {

	private static final String serverUrl = "http://127.0.0.1:8199/dps/server?wsdl";
	private static DPS dps;
	private static String type = "com.bizvpm.dps.processor.pmsvis:pms.visualservice";
	private static String type1 = "com.bizvpm.dps.processor.pmsvis:pms.pdfgen";

	private static String[] officeConvertor = new String[] { "com.bizvpm.dps.processor.wps:wps.wpsconverter",
			"com.bizvpm.dps.processor.openoffice:openoffice.converter",
			"com.bizvpm.dps.processor.msoffice:msoffice.msofficeconverter" };

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		dps = new DPS(serverUrl);
		String _id;
		_id = "5e58b84994ba520e4c56f5e0";// 209
		// testRun("生成PDF 209页速度测试 wps", officeConvertor[0], _id);
		// testRun("生成PDF 209页速度测试 wps", officeConvertor[0], _id);
		// testRun("生成PDF 209页速度测试 wps", officeConvertor[0], _id);
		//
		// testRun("生成PDF 209页速度测试 openoffice", officeConvertor[1],_id);
		// testRun("生成PDF 209页速度测试 openoffice", officeConvertor[1],_id);
		// testRun("生成PDF 209页速度测试 openoffice", officeConvertor[1],_id);
		//
		// testRun("生成PDF 209页速度测试 msoffice", officeConvertor[2],_id);
		// testRun("生成PDF 209页速度测试 msoffice", officeConvertor[2],_id);
		// testRun("生成PDF 209页速度测试 msoffice", officeConvertor[2],_id);
		//
		_id = "5e58bddd94ba520e4c56f5ff";// 84
		// testRun("生成PDF 84页速度测试 wps", officeConvertor[0], _id);
		// testRun("生成PDF 84页速度测试 wps", officeConvertor[0], _id);
		// testRun("生成PDF 84页速度测试 wps", officeConvertor[0], _id);
		//
		// testRun("生成PDF 84页速度测试 openoffice", officeConvertor[1],_id);
		// testRun("生成PDF 84页速度测试 openoffice", officeConvertor[1],_id);
		// testRun("生成PDF 84页速度测试 openoffice", officeConvertor[1],_id);
		//
		// testRun("生成PDF 84页速度测试 msoffice", officeConvertor[2],_id);
		// testRun("生成PDF 84页速度测试 msoffice", officeConvertor[2],_id);
		// testRun("生成PDF 84页速度测试 msoffice", officeConvertor[2],_id);
		//
		//
		_id = "5e58c2ef94ba520e4c56f602";// 30
		// testRun("生成PDF 30页速度测试 wps", officeConvertor[0], _id);
		// testRun("生成PDF 30页速度测试 wps", officeConvertor[0], _id);
		// testRun("生成PDF 30页速度测试 wps", officeConvertor[0], _id);
		//
		// testRun("生成PDF 30页速度测试 openoffice", officeConvertor[1],_id);
		// testRun("生成PDF 30页速度测试 openoffice", officeConvertor[1],_id);
		// testRun("生成PDF 30页速度测试 openoffice", officeConvertor[1],_id);
		//
		// testRun("生成PDF 30页速度测试 msoffice", officeConvertor[2],_id);
		// testRun("生成PDF 30页速度测试 msoffice", officeConvertor[2],_id);
		// testRun("生成PDF 30页速度测试 msoffice", officeConvertor[2],_id);
		//
		_id = "5e58c39494ba520e4c56f604";// 3
		// testRun("生成PDF 3页速度测试 wps", officeConvertor[0], _id);
		// testRun("生成PDF 3页速度测试 wps", officeConvertor[0], _id);
		// testRun("生成PDF 3页速度测试 wps", officeConvertor[0], _id);
		//
		// testRun("生成PDF 3页速度测试 openoffice", officeConvertor[1],_id);
		// testRun("生成PDF 3页速度测试 openoffice", officeConvertor[1],_id);
		// testRun("生成PDF 3页速度测试 openoffice", officeConvertor[1],_id);
		//
		// testRun("生成PDF 3页速度测试 msoffice", officeConvertor[2],_id);
		// testRun("生成PDF 3页速度测试 msoffice", officeConvertor[2],_id);
		// testRun("生成PDF 3页速度测试 msoffice", officeConvertor[2],_id);
		//
		 _id="5e58d07b94ba520e4c56f61f";//错误文档
		// testRun("错误文档 wps", officeConvertor[0],_id);
		// testRun("错误文档 openoffice", officeConvertor[1],_id);
		// testRun("错误文档 msoffice", officeConvertor[2],_id);
		//
		 _id = "5e58d25a94ba520e4c56f622";// 格式检查
		// testRun("格式检查 wps", officeConvertor[0], _id);
		// testRun("格式检查 openoffice", officeConvertor[1],_id);
		 testRun("格式检查 msoffice", officeConvertor[2],_id);
		 _id = "5e58dd4094ba5253ac70bb49";// 公式检查
		// testRun("格式检查 wps", officeConvertor[0], _id);
		// testRun("格式检查 openoffice", officeConvertor[1],_id);
		// testRun("格式检查 msoffice", officeConvertor[2], _id);
		//
		 _id="5e58cb6094ba520e4c56f619";//wps
		// testRun("wps2pdf wps", officeConvertor[0],_id);
		// //odt
		//
		 _id="5e58ca9494ba520e4c56f607";//pptx
		// testRun("pptx2pdf wps", officeConvertor[0],_id);
		// testRun("pptx2pdf openoffice", officeConvertor[1],_id);
		// testRun("pptx2pdf msoffice", officeConvertor[2],_id);
		 _id="5e58cad994ba520e4c56f60a";//ppt
		// testRun("ppt2pdf wps", officeConvertor[0],_id);
		// testRun("ppt2pdf openoffice", officeConvertor[1],_id);
		// testRun("ppt2pdf msoffice", officeConvertor[2],_id);
		 _id="5e58cae994ba520e4c56f60d";//dps
		// testRun("dps2pdf wps", officeConvertor[0],_id);
		// //odp
		//
		 _id="5e58cafb94ba520e4c56f610";//xlsx
		// testRun("xlsx2pdf wps", officeConvertor[0],_id);
		// testRun("xlsx2pdf openoffice", officeConvertor[1],_id);
		// testRun("xlsx2pdf msoffice", officeConvertor[2],_id);
		 _id="5e58cb1494ba520e4c56f613";//xls
		// testRun("xls2pdf wps", officeConvertor[0],_id);
		// testRun("xls2pdf openoffice", officeConvertor[1],_id);
		// testRun("xls2pdf msoffice", officeConvertor[2],_id);
		 _id="5e58cb5394ba520e4c56f616";//et
		// testRun("et2pdf wps", officeConvertor[0],_id);
		// //ods

	}

	/**
	 * 服务端同步调用
	 * 
	 * @param name
	 * @param officeConvertor
	 */
	public static void testRun(String name, String officeConvertor, String _id) {
		try {

			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			task.setValue("domain", "bvs_std");
			task.setValue("col", "contentvault_file");
			task.setValue("target", "contentvault_file_preview");
			task.setValue("_id", _id);
			task.setValue("officeConvertor", officeConvertor);

			final IProcessorManager manager = dps.getProcessorManager();
			Result result = manager.runTask(task, type1);
			result.writeToFile("result", new File("d:/" + name + ".pdf"));

			result = manager.runTask(task, type);
			DataObject msg = (DataObject) result.getValue("result");
			System.out.println(msg.getStringValue());
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
					// testRun(name + " T:" + Thread.currentThread());
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

			// Response<Result> response = manager.runTaskAsync(task, type);
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
