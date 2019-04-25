package com.bizvpm.dps.processor.msoffice;

import java.io.File;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;

public class MSOfficeProcessor implements IProcessorRunable {

	private File inputFile;
	private File outputFile;
	private String sourceType;
	private String targetType;

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		init(processTask);
		convert();

		ProcessResult r = new ProcessResult();
		r.putByteArray("file", outputFile);

		inputFile.delete();
		outputFile.delete();

		return r;

	}

	private void convert() throws Exception {
		String filename = inputFile.getPath();
		String toFilename = outputFile.getPath();

		Dispatch dis = null;
		ActiveXComponent app = null;

		AbstractMSOfficeConverter msOfficeConverter = AbstractMSOfficeConverter.getInstance(sourceType, targetType);
		try {
			ComThread.InitSTA();
			app = msOfficeConverter.getActiveXComponent();
			dis = msOfficeConverter.openDocument(app, filename);
			msOfficeConverter.convert(dis, toFilename);
		} catch (Exception e) {
			throw e;
		} finally {
			msOfficeConverter.dispose(app, dis);
		}
	}

	private void init(ProcessTask processTask) throws Exception {
		sourceType = (String) processTask.get("sourceType");
		targetType = (String) processTask.get("targetType");

		long time = new Date().getTime();

		String pathName = DPSUtil.getTempDirector(getClass(), true);

		inputFile = new File(pathName + time + "." + sourceType);
		outputFile = new File(pathName + time + "." + targetType);

		processTask.writeToFile("file", inputFile);
	}
}
