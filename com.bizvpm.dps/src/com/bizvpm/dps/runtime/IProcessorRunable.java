package com.bizvpm.dps.runtime;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IProcessorRunable {

	ProcessResult run(ProcessTask processTask, IProgressMonitor monitor,
			IProcessContext context) throws Exception;

}
