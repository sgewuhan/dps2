package com.bizvpm.dps.processor.openoffice;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.activation.DataHandler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jodconverter.LocalConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.sun.xml.internal.ws.util.ByteArrayDataSource;

public class OpenOfficeProcessor implements IProcessorRunable {

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		String sourceType = (String) processTask.get("sourceType");
		String targetType = (String) processTask.get("targetType");
		LocalConverter convertor = LocalConverter.make(Starter.getManager());
		InputStream is = processTask.getInputStream("file");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		convertor.convert(is, true).as(getType(sourceType)).to(os).as(getType(targetType)).execute();
		ProcessResult result = new ProcessResult();
		result.put("file", new DataHandler(new ByteArrayDataSource(os.toByteArray(), "application/octet-stream")));
		os.close();
		return result;

	}

	private DocumentFormat getType(String type) {
		return DefaultDocumentFormatRegistry.getFormatByExtension(type);
	}

}
