package dpsclient;

import java.io.File;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.DataObject;
import com.bizvpm.dps.client.IProcessorManager;
import com.bizvpm.dps.client.Result;
import com.bizvpm.dps.client.Task;

public class TestIRobot {

	private static final String serverUrl = "http://127.0.0.1:8199/dps/server?wsdl";
	private static String[] type = { "com.awesometech.dps.processor.irobot:IRobot.createjob"};
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
			task.setName("IRobot´´½¨Job");
			task.setPriority(Task.PRIORITY_1);
			task.setValue("fileType", "zip");
			task.setFileValue("engineeringFiles", new File("D:/I8input/Example/test.zip"));

			Result result = manager.runTask(task, type[0]);
			DataObject jobId = (DataObject) result.getValue("jobId");
			System.out.println(jobId.getIntValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
