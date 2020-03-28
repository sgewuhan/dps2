package com.awesometech.dps.processor.irobot;

import java.io.File;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class Createjob implements IProcessorRunable {

	private File inputFile;

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		// TODO Auto-generated method stub
		init(processTask);
		int jobId = create();
		inputFile.delete();
		ProcessResult r = new ProcessResult();
		r.put("jobId", jobId);
		return r;
	}

	private void init(ProcessTask processTask) throws Exception {

		long time = new Date().getTime();
		String pathName = DPSUtil.getTempDirector(getClass(), true);
		String fileType = (String) processTask.get("fileType");
		inputFile = new File(pathName + time + "." + fileType);
		processTask.writeToFile("file", inputFile);

	}

	// TODO 创建Job，返回jobid，如果jobid为空，返回-1
	private int create() {

		return 0;
	}

}
