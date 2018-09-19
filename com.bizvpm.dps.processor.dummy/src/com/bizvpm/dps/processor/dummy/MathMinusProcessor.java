package com.bizvpm.dps.processor.dummy;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class MathMinusProcessor implements IProcessorRunable {

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		
		double v1 = (double) processTask.get("value1");
		double v2 = (double) processTask.get("value2");
		ProcessResult result = new ProcessResult();
		result.put("result", v1-v2);
		return result;
	}

}
