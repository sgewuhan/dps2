package dpsclient;

import java.io.File;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.IProcessorManager;
import com.bizvpm.dps.client.Result;
import com.bizvpm.dps.client.Task;

public class Test5 {

	private static final String serverUrl = "http://127.0.0.1:8199/dps/server?wsdl";
	private static String[] type = { "com.bizvpm.dps.processor.msoffice:msoffice.msofficeconverter",
			"com.bizvpm.dps.processor.wps:wps.wpsconverter" };
	private static DPS dps;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		dps = new DPS(serverUrl);

		// //Office
		try {
			IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName("Office");
			task.setPriority(Task.PRIORITY_1);
			task.setValue("sourceType", "doc");
			task.setValue("targetType", "pdf");
			task.setFileValue("file", new File("E:/VirtualBox/WPS/工程变更问题汇总.doc"));

			File file = new File("E:/VirtualBox/WPS/wordtest1.pdf");
			Result result = manager.runTask(task, type[0]);
			result.writeToFile("file", file);
		} catch (Exception e) {
			e.printStackTrace();
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

}
