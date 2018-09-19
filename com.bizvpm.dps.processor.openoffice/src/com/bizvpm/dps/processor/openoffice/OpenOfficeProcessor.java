package com.bizvpm.dps.processor.openoffice;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class OpenOfficeProcessor implements IProcessorRunable {

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor,
			IProcessContext context) throws Exception {
		ProcessResult r = new ProcessResult();
		r.put("result", "worker " + Thread.currentThread().getName());
		r.put("taskname", "MSWordProcessorRunable");
		r.putFile("newfile", new File("d:\\newFile.pdf"));
		return r;

	}

}
