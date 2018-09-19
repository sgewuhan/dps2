package dpsclient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.IProcessorManager;
import com.bizvpm.dps.client.Parameter;
import com.bizvpm.dps.client.Result;
import com.bizvpm.dps.client.Task;

public class Test {

	private static final String serverUrl = "http://127.0.0.1:11000/dps/server?wsdl";
	private static String[] type = { "com.bizvpm.dps.processor.msoffice:msoffice.msofficeconverter",
			"com.bizvpm.dps.processor.openoffice:openoffice.openofficeconverter",
			"com.bizvpm.dps.processor.acmecad:acmecad.acmecadconverter", "com.bizvpm.dps.processor.email:email.send",
			"com.bizvpm.dps.processor.birtchart:birtchart.report", "com.bizvpm.dps.processor.dispatch:dispatch.example",
			"com.tmt.dps.processor.sms:tmtsms.sms" };
	private static DPS dps;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String cmdString = "D:\\AcmeCADConverterPortable\\Acme CAD Converter 2014\\AcmeCADConverter.exe "
				+ "/r /e /p 1 /b 7 /lw 1 /ad /f 104 \"D:\\officetest\\调高螺母.dwg\"";

		Process process = Runtime.getRuntime().exec(cmdString);
		process.waitFor();
		// dps = new DPS(serverUrl);

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
			task.setValue(PARA_REPORT_DESIGN,
					"http://localhost:8099/file/go?ftype=file&path=D:\\git\\pm2\\com.tmt.document\\template\\document.productproposal.kfzx.rptdesign&filename=document.productproposal.kfzx.rptdesign");
			task.setValue(PARA_REPORT_OUTPUT, OUTPUT_DOCX);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("FILELIST", "http://localhost:8099/xmlpo?id=56d533ad745fbc13545fb290&db=pm2&col=document");
			task.setValue(PARA_REPORT_DS_PARA, map);
			Result result = manager.runTask(task, "com.bizvpm.dps.processor.report:birtreport");
			result.writeToFile("result", new File("d:/" + name + ".pdf"));
			System.out.println(result.getValue("result"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * 服务端同步调用
	 * 
	 * @param name
	 */
	public static void testRun(String name) {

		// SMS
		try {
			IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			Map<String, String> map = new HashMap<String, String>();
			map.put("20132199", "15773386362");
			task.setValue("to", map);
			task.setValue("textMessage", "DPS 测试");
			task.setValue("sendTime", "");
			task.setValue("mobileCount", "1");

			Result result = manager.runTask(task, type[6]);
			System.out.println(result.getValue("results"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// // email
		// try {
		// IProcessorManager manager = dps.getProcessorManager();
		// Task task = new Task();
		// task.setName(name);
		// task.setPriority(Task.PRIORITY_1);
		// task.setValue("emailType", "imghtml");
		// task.setValue("useServerAddress", Boolean.FALSE);
		// List<String> to = new ArrayList<>();
		// to.add("yj@yaozheng.com.cn:杨骏");
		// // to.add("ycx@yaozheng.com.cn:易春晓");
		// task.setValue("to", to);
		// task.setValue("title", "测试");
		// task.setValue("message",
		// " <table align=\"center\" border=\"1\" cellpadding=\"1\""
		// + " cellspacing=\"1\" style=\"height:80px; width:800px\">"
		// + " <tbody> <tr> "
		// + "<td colspan=\"5\" style=\"background-color: #000033; "
		// + "height: 42px\"><span style=\"font-family:微软雅黑\"><strong>"
		// + "<span style=\"color:#d3d3d3\"><span style=\"font-size:16px\">"
		// + "通知: 测试邮件</span></span></strong></span></td> </tr>"
		// + " <tr> <td colspan=\"5\" "
		// + "style=\"background-color: #ffffff; height: 42px\">"
		// + " <p><span style=\"font-family:微软雅黑\">"
		// + "<span style=\"font-size:14px\"><strong>杨骏、易春晓</strong>"
		// + ", 您好：<br /> <strong><span style=\"color:#0000cd\">"
		// + "DPS系统</span></strong>发出了一条<strong><span style=\"color:#0000cd\">"
		// + "&nbsp;邮件测试 </span></strong>消息。<br /> 详细的内容如下:"
		// + "</span></span></p> </td> </tr> "
		// + "<tr> <td style=\"width: 20px\">&nbsp;</td> "
		// + "<td style=\"width: 100px\"><span style=\"font-family:微软雅黑\">系统"
		// + "</span></td> <td><span style=\"font-family:微软雅黑\">DPS"
		// + "</span></td> <td style=\"width: 60px\"><span "
		// + "style=\"font-family:微软雅黑\">发送人:</span></td> "
		// + "<td style=\"width: 120px\"><span style=\"font-family:微软雅黑\">"
		// + "DPS Center</span></td> </tr> <tr> "
		// + "<td>&nbsp;</td> <td><span style=\"font-family:微软雅黑\">"
		// + "事件类型</span></td> <td colspan=\"3\"><span "
		// + "style=\"font-family:微软雅黑\">测试邮件</span></td> "
		// + "</tr> <tr> <td colspan=\"5\" "
		// + "style=\"background-color: #cccccc\"> <p><span "
		// + "style=\"font-family:微软雅黑\">本消息由DPS系统自动发出，请勿回复，谢谢！"
		// + "<br /> <strong>2016/2/29 17:00:29</strong></span></p>"
		// + " </td> </tr> </tbody> </table>");
		//
		// List<Map<String, Object>> files = new ArrayList<Map<String,
		// Object>>();
		//
		// Map<String, Object> file = new HashMap<String, Object>();
		// file.put("filename", "TEMPLATE _A1.dwg");
		// file.put("file", new File("D:/officetest/TEMPLATE _A1.dwg"));
		// files.add(file);
		//
		// file = new HashMap<String, Object>();
		// file.put("filename", "wordtest1.docx");
		// file.put("file", new File("D:/officetest/wordtest1.docx"));
		// files.add(file);
		//
		// file = new HashMap<String, Object>();
		// file.put("filename", "asf_logo_wide1.gif");
		// file.put("file", new DataHandler(
		// new URL("http://www.apache.org/images/asf_logo_wide.gif")));
		// files.add(file);
		//
		// file = new HashMap<String, Object>();
		// file.put("filename", "asf_logo_wide2.gif");
		// file.put("file", "http://www.apache.org/images/asf_logo_wide.gif");
		// files.add(file);
		// task.setValue("attachment", files);
		//
		// task.setValue("smtpHost", "smtp.exmail.qq.com");
		// task.setValue("smtpPort", 465);
		// task.setValue("smtpUseSSL", Boolean.TRUE);
		// task.setValue("senderAddress", "yj@yaozheng.com.cn");
		// task.setValue("senderPassword", "lode123456");
		// task.setValue("fromName", "DPS Center");
		//
		// Result result = manager.runTask(task, type[3]);
		// System.out.println(result.getValue("results"));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// // AutoCAD
		// try {
		// IProcessorManager manager = dps.getProcessorManager();
		// Task task = new Task();
		// task.setName(name);
		// task.setPriority(Task.PRIORITY_1);
		// task.setValue("sourceType", "dwg");
		// task.setValue("targetType", "pdf");
		// task.setValue("autoZoomExtend", Boolean.TRUE);
		// task.setValue("rasterPixel", 1);
		// task.setValue("backgroundColor", 7);
		// task.setValue("lineWeight", 1);
		// task.setValue("autoSize", Boolean.TRUE);
		// task.setFileValue("file", new File("D:/officetest/TEMPLATE
		// _A1.dwg"));
		//
		// File file = new File("D:/officetest/TEMPLATE _A1.pdf");
		// Result result = manager.runTask(task, type[2]);
		// result.writeToFile("file", file);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// //Office
		try {
			IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName(name);
			task.setPriority(Task.PRIORITY_1);
			task.setValue("sourceType", "docx");
			task.setValue("targetType", "pdf");
			task.setFileValue("file", new File("D:/officetest/wordtest1.docx"));

			File file = new File("D:/officetest/wordtest1.pdf");
			Result result = manager.runTask(task, type[0]);
			result.writeToFile("file", file);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// //Office
		// try {
		// final IProcessorManager manager = dps.getProcessorManager();
		// Task task = new Task();
		// task.setName(name);
		// task.setPriority(Task.PRIORITY_1);
		// task.setValue("sourceType", "docx");
		// task.setValue("targetType", "pdf");
		// task.setFileValue("file", new File("D:/officetest/wordtest1.docx"));
		//
		// File file = new File("D:/officetest/wordtest1.pdf");
		// Result result = manager.runTask(task, type[0]);
		// result.writeToFile("file", file);
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }

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
			final Task task = new Task();
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
//			manager.runTaskAsync(task, type[0], asyncHandler);
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
