package com.bizvpm.dps.processor.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.eclipse.birt.report.engine.api.DocxRenderOption;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IPPTRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.sun.xml.internal.ws.util.ByteArrayDataSource;

public class BirtReportProcessor implements IProcessorRunable {

	private static final String PARA_REPORT_DESIGN = "design";

	private static final String PARA_REPORT_TASK_PARA = "task_parameter";

	private static final String PARA_REPORT_DS_PARA = "datasource_parameter";

	private static final String PARA_REPORT_OUTPUT = "output";

	private static final String OUTPUT_HTML = "html";

	private static final String OUTPUT_PDF = "pdf";

	private static final String OUTPUT_DOCX = "docx";

	private static final String OUTPUT_EXCEL = "excel";

	private static final String OUTPUT_PPTX = "pptx";

	public BirtReportProcessor() {
	}

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		IReportEngine engine = Activator.getEngine();

		IReportRunnable reportRunnable = createReportRunnable(engine, processTask);

		IRunAndRenderTask task = engine.createRunAndRenderTask(reportRunnable);

		IRenderOption renderOptions = createRenderOption(processTask);
		Assert.isNotNull(renderOptions, "Must set report render option before generate ContentBody");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		renderOptions.setOutputStream(out);

		task.setRenderOption(renderOptions);

		setTaskParameter(task, processTask);

		task.run();
		task.close();

		String html = null;
		if(Boolean.TRUE.equals(processTask.get("output_html_string"))){
			task = engine.createRunAndRenderTask(reportRunnable);

			IRenderOption renderOptions1 =  getHtmlRenderOption();
			Assert.isNotNull(renderOptions1, "Must set report render option before generate ContentBody");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			renderOptions1.setOutputStream(baos);

			task.setRenderOption(renderOptions1);

			setTaskParameter(task, processTask);

			task.run();
			task.close();
			html = baos.toString("utf-8");
		}
		
		
		ProcessResult result = new ProcessResult();
		DataSource dataSource = new ByteArrayDataSource(out.toByteArray(), "application/octet-stream");

		DataHandler resultDataHandler = new DataHandler(dataSource);
		result.put("result", resultDataHandler);
		if(html!=null){
			result.put("html", html);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	private void setTaskParameter(IRunAndRenderTask task, ProcessTask processTask) {
		Object parameters = processTask.get(PARA_REPORT_TASK_PARA);
		if (parameters instanceof Map) {
			Iterator iter = ((Map) parameters).keySet().iterator();
			while (iter.hasNext()) {
				Object key = iter.next();
				task.setParameterValue(key.toString(), ((Map) parameters).get(key));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void setDataSourceParameter(IReportRunnable reportRunnable, ProcessTask processTask) {
		Object parameters = processTask.get(PARA_REPORT_DS_PARA);
		if (!(parameters instanceof Map)) {
			return;
		}
		ReportDesignHandle designHandle = (ReportDesignHandle) reportRunnable.getDesignHandle();
		SlotHandle dataSources = designHandle.getDataSources();
		int count = dataSources.getCount();
		for (int i = 0; i < count; i++) {
			DesignElementHandle handle = dataSources.get(i);
			try {
				handle.setProperties((Map) parameters);
			} catch (SemanticException e) {
				e.printStackTrace();
			}
		}
		
//		SlotHandle dataSet = designHandle.getDataSets();
//		count = dataSet.getCount();
//		for (int i = 0; i < count; i++) {
//			DesignElementHandle handle = dataSet.get(i);
//			try {
//				handle.setProperties((Map) parameters);
//			} catch (SemanticException e) {
//				e.printStackTrace();
//			}
//		}
	}

	private IRenderOption createRenderOption(ProcessTask processTask) {
		Object output = processTask.get(PARA_REPORT_OUTPUT);
		if (OUTPUT_HTML.equals(output)) {
			return getHtmlRenderOption();
		} else if (OUTPUT_PDF.equals(output)) {
			return getPdfRenderOption();
		} else if (OUTPUT_DOCX.equals(output)) {
			return getDocxRenderOption();
		} else if (OUTPUT_EXCEL.equals(output)) {
			return getExcelRenderOption();
		} else if (OUTPUT_PPTX.equals(output)) {
			return getPPTXRenderOption();
		}
		return null;
	}

	private IRenderOption getPPTXRenderOption() {
		RenderOption render = new RenderOption();
		render.setOutputFormat("pptx");
		render.setOption(IPPTRenderOption.EXPORT_FILE_FOR_MICROSOFT_OFFICE_2010_2013, Boolean.TRUE);
		render.setEmitterID("org.eclipse.birt.report.engine.emitter.pptx");
		render.setOption(IRenderOption.RENDER_DPI, 96);
		render.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
		return render;
	}

	private IRenderOption getDocxRenderOption() {
		DocxRenderOption render = new DocxRenderOption();
		render.setOutputFormat("docx");
		render.setOption(IRenderOption.HTML_PAGINATION, Boolean.TRUE);
		render.setEmitterID("org.eclipse.birt.report.engine.emitter.docx");
		render.setOption(IRenderOption.RENDER_DPI, 96);
		render.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
		return render;
	}

	private IRenderOption getExcelRenderOption() {
		EXCELRenderOption render = new EXCELRenderOption();
		render.setOutputFormat("xlsx");
		render.setEmitterID("org.eclipse.birt.report.engine.emitter.xlsx");
		render.setSupportedImageFormats("PNG");
		return render;
	}

	private IRenderOption getPdfRenderOption() {
		PDFRenderOption pdfRender = new PDFRenderOption();
		pdfRender.setOutputFormat(IRenderOption.OUTPUT_FORMAT_PDF);
		pdfRender.setSupportedImageFormats("PNG");
//		pdfRender.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
		return pdfRender;
	}

	private IRenderOption getHtmlRenderOption() {
		HTMLRenderOption htmlRender = new HTMLRenderOption();
		htmlRender.setOutputFormat(HTMLRenderOption.HTML);
		htmlRender.setImageDirectory(System.getProperty("java.io.tmpdir"));
		htmlRender.setSupportedImageFormats("PNG");
		return htmlRender;
	}

	private IReportRunnable createReportRunnable(IReportEngine engine, ProcessTask processTask)
			throws EngineException, IOException {
		Object design = processTask.get(PARA_REPORT_DESIGN);

		InputStream designStream = null;
		try {
			if (design instanceof String) {// URL·½Ê½
				URL url = new URL((String) design);
				designStream = url.openStream();
			} else if (design instanceof DataHandler) {
				DataHandler handler = (DataHandler) design;
				designStream = handler.getInputStream();
			}
		} catch (Exception e) {
		}
		Assert.isNotNull(designStream, "can not read value from " + PARA_REPORT_DESIGN);
		IReportRunnable reportRunnable = engine.openReportDesign(designStream);
		setDataSourceParameter(reportRunnable, processTask);

		designStream.close();
		return reportRunnable;
	}

}
