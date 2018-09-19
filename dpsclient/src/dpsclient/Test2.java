package dpsclient;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.ws.Response;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.IProcessorManager;
import com.bizvpm.dps.client.Parameter;
import com.bizvpm.dps.client.Result;
import com.bizvpm.dps.client.Task;

public class Test2 {

	private static final String serverUrl = "http://127.0.0.1:8199/dps/server?wsdl";
	private static String[] type = {
			"com.bizvpm.dps.processor.msoffice:msoffice.msofficeconverter",
			"com.bizvpm.dps.processor.openoffice:openoffice.openofficeconverter",
			"com.bizvpm.dps.processor.acmecad:acmecad.acmecadconverter", 
			"com.bizvpm.dps.processor.email:email.send",
			"com.bizvpm.dps.processor.birtchart:birtchart.report",
			"com.bizvpm.dps.processor.dispatch:dispatch.example" };
	private static DPS dps;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		dps = new DPS(serverUrl);

		// checkParameter();

		/*
		 * 测试计划任务
		 */
		// testSchedule("计划任务1");
		// testSchedule("计划任务2");
		// testSchedule("计划任务3");

		
//		testRun("阻断任务" + System.currentTimeMillis());

		// testMultiThread(30, "多线程测试");
		//
		// testRunAsync("异步任务" + System.currentTimeMillis());
		//
		// testRunFile("处理文件" + System.currentTimeMillis());
		//
		// testDispatchTask("dispatcher task");
		
		testRunReport("Report Task");
	}

	/**
	 * 服务端同步调用
	 * 
	 * @param name
	 */
	public static void testRun(String name) {
		
		//AutoCAD
//		try {
//			final IProcessorManager manager = dps.getProcessorManager();
//			Task task = new Task();
//			task.setName(name);
//			task.setPriority(Task.PRIORITY_1);
//			task.setValue("sourceType", "dwg");
//			task.setValue("targetType", "pdf");
//			task.setValue("autoZoomExtend", Boolean.TRUE);
//			task.setValue("rasterPixel", 1);
//			task.setValue("backgroundColor", 7);
//			task.setValue("lineWeight", 1);
//			task.setValue("autoSize", Boolean.TRUE);
//			task.setFileValue("file", new File("D:/officetest/TEMPLATE _A1.dwg"));
//
//			File file = new File("D:/officetest/TEMPLATE _A1.pdf");
//			Result result = manager.runTask(task, type[2]);
//			result.writeToFile("file", file);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
		
		
		//Office
		try {
			final IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			task.setValue("sourceType", new Date());
			List<Object> listTest = new ArrayList<Object>();
			listTest .add(1222);
			listTest.add(false);
			listTest.add(new File("D:/open.pdf"));
			listTest.add("Sssss");
			task.setValue("parameter2", listTest);
			task.setValue("parameter1", "pdf");
			task.setFileValue("file", new File("D:/open.pdf"));
//			File file = new File("D:/officetest/wordtest1.pdf");
			Result result = manager.runTask(task, "com.bizvpm.dps.processor.dispatch:dispatch.example");
			System.out.println(result);
//			result.writeToFile("file", file);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public static void checkParameter() {
		try {
			IProcessorManager manager = dps.getProcessorManager();
			List<Parameter> list = manager.lookupParameters(type[2]).getParameters();
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

	public static void testDispatchTask(String name) {
		try {
			final IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			task.setValue("parameter1", "abcd");
			task.setValue("parameter2", 1000.222d);
			task.setFileValue("file", new File("d:/open.pdf"));
			Result result = manager.runTask(task, type[type.length - 1]);
			result.writeToFile("newfile", new File("d:/" + name + ".pdf"));
			System.out.println(result.getValue("result"));
		} catch (Exception e1) {
			e1.printStackTrace();
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
	 * 服务端同步调用,传递文件
	 * 
	 * @param name
	 */
	public static void testRunFile(String name) {
		try {
			final IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			task.setValue("parameter2", 1000.222d);
			task.setValue("sourceType", "doc");
			task.setFileValue("file", new File("d:/open.pdf"));
			Result result = manager.runTask(task, type[0]);
			result.writeToFile("newfile", new File("d:/" + name + ".pdf"));
			System.out.println(result.getValue("result"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}
	
	
	static final String PARA_REPORT_DESIGN = "design";

	static final String PARA_REPORT_TASK_PARA = "task_parameter";

	static final String PARA_REPORT_DS_PARA = "datasource_parameter";

	static final String PARA_REPORT_OUTPUT = "output";

	static final String OUTPUT_HTML = "html";

	static final String OUTPUT_PDF = "pdf";

	static final String OUTPUT_DOCX = "docx";

	static final String OUTPUT_EXCEL = "excel";
	
	public static void testRunReport(String name) {
		try {
			final IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName(name);
			task.setValue(PARA_REPORT_DESIGN, new File("D:\\mars\\workspace\\test\\swr.rptdesign"));//"http://localhost:80/file/go?ftype=file&db=pm2&namespace=template_file&oid=55d1afe8a7f5f5b25a064846&filename=swr.rptdesign");
			task.setValue(PARA_REPORT_OUTPUT, "pptx");
			
			HashMap<String, String> map = new HashMap<String,String>();
			map.put("FILELIST", "http://127.0.0.1/xmlpo?id=55d47381a7f5eb86986dbcff&db=pm2&col=document");
			task.setValue(PARA_REPORT_DS_PARA,map); 
			Result result = manager.runTask(task, "com.bizvpm.dps.processor.report:birtreport");
			result.writeToFile("result", new File("h:/" + System.currentTimeMillis() + ".pptx"));
			System.out.println(result.getValue("result"));
		} catch (Exception e1) {
			e1.printStackTrace();
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
			task.setValue("sourceType", "doc");
			task.setFileValue("file", new File("d:/open.pdf"));
			Response<Result> response = manager.runTaskAsync(task, type[0]);
			// manager.runTaskAsync(task, processorTypeId, asyncHandler)
			while (!response.isDone()) {
				System.out.println(response.get().getValue("result"));
			}
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
			task.setValue("parameter1", "abcd");
			task.setValue("sourceType", "docx");
			task.setFileValue("file", new File("d:/open.pdf"));
			manager.scheduleTask(task, type[0]);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

}
