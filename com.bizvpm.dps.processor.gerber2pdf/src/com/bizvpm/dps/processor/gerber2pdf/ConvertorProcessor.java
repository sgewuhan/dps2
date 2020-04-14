package com.bizvpm.dps.processor.gerber2pdf;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class ConvertorProcessor implements IProcessorRunable {

	public ConvertorProcessor() {
	}

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		String path = Activator.getDefault().getPreferenceStore()
				.getString("convertorPath");
		if (path != null) {
			String pathName = DPSUtil.getTempDirector(getClass(), true);
			long timeMillis = System.currentTimeMillis();
			String outputFilePath = pathName + timeMillis + ".pdf";
			File outputFile = new File(outputFilePath);
			//写入输入文件
			File inputFile = new File(pathName + timeMillis);
			processTask.writeToFile("file", inputFile);
			
			String parameter = " -silentexit-nowarnings-output=\""+pathName + timeMillis +"\" " + "\""+pathName + timeMillis + "\"";
			String cmdString = path + parameter;
			Process process = Runtime.getRuntime().exec(cmdString);
			process.waitFor();
			
			ProcessResult r = new ProcessResult();
			r.putByteArray("file", outputFile);
			inputFile.delete();
			outputFile.delete();
			return r;
		} else {
			throw new Exception("Can not find CAD_CONVERTER_PATH");
		}
	}

}
