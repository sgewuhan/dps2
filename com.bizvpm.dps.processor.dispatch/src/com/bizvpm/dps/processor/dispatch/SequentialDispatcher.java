package com.bizvpm.dps.processor.dispatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class SequentialDispatcher implements IProcessorRunable {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		Object value = processTask.get("processors");

		List<?> processors = (List<?>) value;

		ProcessResult result = null;

		List<Map<String, Object>> intermediates = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < processors.size(); i++) {
			Map processor = (Map) processors.get(i);

			// 处理器类型
			String processorTypeId = (String) processor.get("processorTypeId");
			Assert.isTrue(value instanceof List<?>, "processors[" + i + "] must have a processorTypeId");

			HashMap<String, Object> parameters = new HashMap<String, Object>();

			// 传入参数
			Object inPara = processor.get("parameter");
			if (inPara instanceof Map) {
				parameters.putAll((Map<? extends String, ? extends Object>) inPara);
			}

			// 取中间参数
			Object medParaList = processor.get("intermediate");
			if ((medParaList instanceof List) && ((List<?>) medParaList).size() > 0) {
				for (int j = 0; j < ((List<?>) medParaList).size(); j++) {
					Object medPara = ((List<?>) medParaList).get(j);
					if ((medPara instanceof List) && ((List<?>) medPara).size() > 2) {
						Integer index = (Integer) ((List<?>) medPara).get(0);
						String out = (String) ((List<?>) medPara).get(1);
						String in = (String) ((List<?>) medPara).get(2);
						if (intermediates.size() > index) {
							Map<String, Object> intm = intermediates.get(index);
							Object v = intm.get(out);
							parameters.put(in, v);
						}
					}
				}
			}

			ProcessTask subTask = new ProcessTask();
			subTask.setName(processTask.getName() + ", sub task " + i);
			subTask.setParentId(processTask.getId());
			subTask.setPriority(10);
			Iterator<Entry<String, Object>> iter = parameters.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Object> ent = iter.next();
				subTask.put(ent.getKey(), ent.getValue());
			}
			result = context.runTask(subTask, processorTypeId);
			intermediates.add(result.getValues());
		}

		return result;
	}

}
