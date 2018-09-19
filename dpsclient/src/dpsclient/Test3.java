package dpsclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.IProcessorManager;
import com.bizvpm.dps.client.Result;
import com.bizvpm.dps.client.Task;

public class Test3 {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		DPS dps = new DPS("http://127.0.0.1:8199/dps/server?wsdl");
		
		try {
			final IProcessorManager manager = dps.getProcessorManager();
			Task task = new Task();
			task.setName("¥Æ––¥¶¿Ì≤‚ ‘");
			task.setPriority(Task.PRIORITY_1);
			
			List processors = new ArrayList();
			Map processor1 = new HashMap();
			processor1.put("processorTypeId", "com.bizvpm.dps.processor.dummy:math.add");
			Map parameter1 = new HashMap<String,Object>();
			parameter1.put("value1", 1d);
			parameter1.put("value2", 2d);
			processor1.put("parameter", parameter1);
			processors.add(processor1);
			
			Map processor2 = new HashMap();
			processor2.put("processorTypeId", "com.bizvpm.dps.processor.dummy:math.minus");
			Map parameter2 = new HashMap<String,Object>();
			parameter2.put("value2", 7.8d);
			processor2.put("parameter", parameter2);
			processors.add(processor2);
			ArrayList medList = new ArrayList();
			ArrayList medItem = new ArrayList();
			medItem.add(0);
			medItem.add("result");
			medItem.add("value1");
			medList.add(medItem);
			processor2.put("intermediate", medList);
			processors.add(processor2);

			task.setValue("processors", processors);
			
			Result result = manager.runTask(task, "com.bizvpm.dps.processor.dispatch:dispatch.sequential");
			System.out.println(result.getValue("result"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		
	}

}
