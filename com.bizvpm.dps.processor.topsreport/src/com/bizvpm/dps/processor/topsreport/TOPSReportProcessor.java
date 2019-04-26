package com.bizvpm.dps.processor.topsreport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.commons.io.FileUtils;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;

public class TOPSReportProcessor implements IProcessorRunable {

	private static final String PARA_REPORT_DS_PARA = "datasource_parameter";

	private static final String PARA_REPORT_DESIGN = "design";

	private static final String PARA_REPORT_TASK_PARA = "task_parameter";

	private static final String PARA_REPORT_SERVERPATH = "serverPath";

	private static final String PARA_REPORT_FILENAME = "fileName";

	public TOPSReportProcessor() {
	}

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		IReportEngine engine = Activator.getEngine();

		IReportRunnable reportRunnable = createReportRunnable(engine, processTask);

		IRunAndRenderTask task = engine.createRunAndRenderTask(reportRunnable);

		IRenderOption renderOptions = getHtmlRenderOption();
		Assert.isNotNull(renderOptions, "Must set report render option before generate ContentBody");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		renderOptions.setOutputStream(out);

		task.setRenderOption(renderOptions);

		setTaskParameter(task, processTask);

		task.run();
		task.close();

		String html = out.toString("utf-8");

		Map<String, String> downloadFileUrl = new HashMap<String, String>();

		String host = (String) processTask.get(PARA_REPORT_SERVERPATH);
		html = convertHTML(html, host, downloadFileUrl);

		String pathName = DPSUtil.getTempDirector(getClass(), true);

		long time = new Date().getTime();

		String fileName = (String) processTask.get(PARA_REPORT_FILENAME);
		if (fileName == null || "".equals(fileName)) {
			fileName = "" + time;
		}

		File dir = new File(pathName + time);
		dir.mkdirs();

		File inputFile = new File(pathName + time + File.separator + fileName + ".html");

		ByteArrayInputStream ins = new ByteArrayInputStream(html.getBytes("UTF-8"));
		OutputStream os = new FileOutputStream(inputFile);
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		os.close();
		ins.close();

		File outputFile = new File(pathName + time + File.separator + fileName + ".docx");

		String filename = inputFile.getPath();
		String toFilename = outputFile.getPath();

		Dispatch dis = null;
		ActiveXComponent app = null;

		AbstractMSOfficeConverter msOfficeConverter = AbstractMSOfficeConverter.getInstance();
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

		// TODO ��ȡ�����ļ���������ZIP
		File att = new File(pathName + time + File.separator + "����");
		att.mkdirs();

		downloadFile(downloadFileUrl, host, pathName + time + File.separator + "����");

		ProcessResult result = new ProcessResult();

		// ����zip�����
		File zip = new File(pathName + time + File.separator + fileName + ".zip");
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zip));

		// �������������
		BufferedOutputStream bos = new BufferedOutputStream(zipOut);
		// ���ú���
		compress(zipOut, bos, att, att.getName());
		compress(zipOut, bos, outputFile, outputFile.getName());
		
		bos.close();
		zipOut.close();

		DataSource dataSource = new FileDataSource(zip);

		DataHandler resultDataHandler = new DataHandler(dataSource);
		result.put("result", resultDataHandler);
		return result;
	}

	private void compress(ZipOutputStream out, BufferedOutputStream bos, File sourceFile, String base) {
		try {
			// ���·��ΪĿ¼���ļ��У�
			if (sourceFile.isDirectory()) {

				// ȡ���ļ����е��ļ��������ļ��У�
				File[] flist = sourceFile.listFiles();

				if (flist.length == 0)// ����ļ���Ϊ�գ���ֻ����Ŀ�ĵ�zip�ļ���д��һ��Ŀ¼�����
				{
					System.out.println(base + "/");
					out.putNextEntry(new ZipEntry(base + "/"));
				} else// ����ļ��в�Ϊ�գ���ݹ����compress���ļ����е�ÿһ���ļ������ļ��У�����ѹ��
				{
					for (int i = 0; i < flist.length; i++) {
						compress(out, bos, flist[i], base + "/" + flist[i].getName());
					}
				}
			} else// �������Ŀ¼���ļ��У�����Ϊ�ļ�������д��Ŀ¼����㣬֮���ļ�д��zip�ļ���
			{
				out.putNextEntry(new ZipEntry(base));
				FileInputStream fos = new FileInputStream(sourceFile);
				BufferedInputStream bis = new BufferedInputStream(fos);

				int tag;
				// ��Դ�ļ�д�뵽zip�ļ���
				while ((tag = bis.read()) != -1) {
					bos.write(tag);
				}
				bis.close();
				fos.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void downloadFile(Map<String, String> downloadFileUrl, String host, String saveDir) {
		for (String fileName : downloadFileUrl.keySet()) {
			try {
				String url = downloadFileUrl.get(fileName);
				if (url.indexOf("/bvs/fs") >= 0) {
					String id = "";
					String namespace = "";
					String[] split = url.split("\\?");
					if (split.length > 1) {
						String[] paras = split[1].split("&");
						for (String para : paras) {
							if (para.startsWith("id=")) {
								id = para.replace("id=", "");
							}
							if (para.startsWith("namespace=")) {
								namespace = para.replace("namespace=", "");
							}
						}
					}
					url = Activator.getDefault().getServer() + "/fs/" + namespace + "/" + id + "/"
							+ URLEncoder.encode(fileName, "utf-8");
				}
				FileUtils.copyURLToFile(new URL(url), new File(saveDir + File.separator + fileName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String convertHTML(String html, String host, Map<String, String> downloadFileUrl) {
		html = html.replaceAll("&amp;", "&");
		html = html.replaceAll("&quot;", "'");
		html = html.replaceAll("&lt;", "<");
		html = html.replaceAll("&gt;", ">");
		html = html.replaceAll("&amp;", "&");
		html = html.replaceAll("&amp", "&");

		// �滻��ַ
		html = html.replaceAll("/bvs/fs", host + "/bvs/fs");

		html = html.replace("</head>", "<link rel='stylesheet' type='text/css'href='" + host
				+ "/bvs/widgets/ckeditor/codebase/contents.css'></head>");

		String regEx_html = "___HTML___(.*?)(___EHTML___)";
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher om = p_html.matcher(html);
		while (om.find()) {
			String o_html = om.group();
			String n_html = o_html.replaceAll("&#xa0;", "");
			n_html = n_html.replaceAll("<br/>", "");
			n_html = n_html.replaceAll("___HTML___", "");
			n_html = n_html.replaceAll("___EHTML___", "");
			html = html.replace(o_html, n_html);
		}

		html = html.replaceAll("</p><br/>", "</p>");

		html = html.replaceAll("</p><br/>", "</p>");

		// ��ʽ�������ļ�����ȡ�����ļ���ַ��
		String regEx_a = "<a(.*?)(</a>)";
		Pattern p_a = Pattern.compile(regEx_a, Pattern.CASE_INSENSITIVE);
		Matcher am = p_a.matcher(html);
		while (am.find()) {
			String o_a = am.group();
			String fileName = "";
			Matcher m_name = Pattern.compile(">(.*?)(</a>)").matcher(o_a);
			while (m_name.find()) {
				fileName = m_name.group(1);
			}

			Matcher m_href = Pattern.compile("href\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(o_a);
			while (m_href.find()) {
				String group = m_href.group(1);
				downloadFileUrl.put(fileName, group);
			}
			html = html.replace(o_a, fileName);
		}
		return html;
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
			if (design instanceof String) {// URL��ʽ
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
