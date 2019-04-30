package com.bizvpm.dps.processor.topsreport;

import java.awt.image.BufferedImage;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class TOPSReportProcessor implements IProcessorRunable {

	private static final String PARA_REPORT_DS_PARA = "datasource_parameter";

	private static final String PARA_REPORT_DESIGN = "design";

	private static final String PARA_REPORT_TASK_PARA = "task_parameter";

	private static final String PARA_REPORT_SERVERPATH = "serverPath";

	private static final String PARA_REPORT_FILENAME = "fileName";

	// private static final String SYSTEM_TEMPORARY_PATH =
	// System.getProperty("java.io.tmpdir");

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
		Map<String, String> pics = new HashMap<String, String>();

		String host = (String) processTask.get(PARA_REPORT_SERVERPATH);
		html = convertHTML(html, host, downloadFileUrl, pics);

		String pathName = DPSUtil.getTempDirector(getClass(), true);

		long time = new Date().getTime();

		String fileName = (String) processTask.get(PARA_REPORT_FILENAME);
		if (fileName == null || "".equals(fileName)) {
			fileName = "" + time;
		}

		File dir = new File(pathName + time);
		dir.mkdirs();

		// ���ز�����ͼƬ
		File img = new File(pathName + time + File.separator + "image");
		img.mkdirs();

		Map<String, String> picBuffereds = new HashMap<String, String>();
		downloadImage(pics, picBuffereds, img.getPath());

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

		try {
			ComThread.InitSTA();
			app = new ActiveXComponent("Word.Application");
			app.setProperty("Visible", false);
			dis = app.getProperty("Documents").toDispatch();
			// dis = Dispatch.invoke(dis, "Add", Dispatch.Method, new Object[0], new
			// int[1]).toDispatch();
			dis = Dispatch.call(dis, "Open", Activator.getDefault().getTemplatePath()).toDispatch();
			Dispatch selection = app.getProperty("Selection").toDispatch();
			Dispatch.invoke(selection, "InsertFile", Dispatch.Method,
					new Object[] { filename, "", new Variant(false), new Variant(false), new Variant(false) },
					new int[3]);

			// �滻ͼƬ
			for (String replaceText : picBuffereds.keySet()) {
				String imgPath = picBuffereds.get(replaceText);
				// ������ƶ�����ʼλ��
				Dispatch.call(selection, "HomeKey", new Variant(6));
				while (find(selection, replaceText)) {
					insertImage(selection, imgPath);
				}
			}

			// ����new Variant(16)
			// word
			// ����ʽ�����б�https://docs.microsoft.com/zh-cn/dotnet/api/microsoft.office.interop.word.wdsaveformat?view=word-pia
			// ��MSDN�п���WdSaveFormat ���в�ѯ
			Dispatch.invoke(dis, "SaveAs", Dispatch.Method, new Object[] { toFilename, new Variant(16) }, new int[1]);
		} catch (Exception e) {
			throw e;
		} finally {
			if (dis != null) {
				Dispatch.call(dis, "Close", false);
			}
			if (app != null) {
				app.invoke("Quit", 0);
				app = null;
			}
		}

		// // ʹ��POI���ĵ�
		// FileInputStream fis = new FileInputStream(toFilename);
		// XWPFDocument document = new XWPFDocument(OPCPackage.open(fis));
		// // ����ͼƬ�ߴ�
		// setPictures(document.getTables());

		// ��ȡ�����ļ���������ZIP
		File att = new File(pathName + time + File.separator + "����");
		att.mkdirs();

		downloadFile(downloadFileUrl, att.getPath());

		ProcessResult result = new ProcessResult();

		// ����zip�����
		File zip = new File(pathName + time + File.separator + fileName + ".zip");
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zip));

		// �������������
		BufferedOutputStream bos = new BufferedOutputStream(zipOut);
		// ���ú���
		compress(zipOut, bos, att, att.getName());
		compress(zipOut, bos, img, img.getName());
		compress(zipOut, bos, outputFile, outputFile.getName());

		bos.close();
		zipOut.close();

		DataSource dataSource = new FileDataSource(zip);

		DataHandler resultDataHandler = new DataHandler(dataSource);
		result.put("result", resultDataHandler);
		return result;
	}

	/**
	 * ��ǰ������滻ͼƬ
	 * 
	 * @param imagePath
	 *            ͼƬ��·��
	 */
	private void insertImage(Dispatch selection, String imagePath) {
		// ���ͼƬԭʼ�ߴ粢�������ź�ĳߴ�
		BufferedImage image = ImageUtil.getBufferedImage(imagePath);

		int width = 200;
		int height = width * image.getHeight() / image.getWidth();

		// ����ͼƬ
		Dispatch picture = Dispatch.call(Dispatch.get(selection, "InLineShapes").toDispatch(), "AddPicture", imagePath)
				.getDispatch();
		Dispatch.call(picture, "Select");
		Dispatch.put(picture, "Width", new Variant(width));// ����ͼƬ���
		Dispatch.put(picture, "Height", new Variant(height));// ����ͼƬ�߶�
		// Dispatch ShapeRange = Dispatch.call(picture, "ConvertToShape").toDispatch();
		// // ȡ��ͼƬ����
		// Dispatch WrapFormat = Dispatch.get(ShapeRange, "WrapFormat").toDispatch(); //
		// ȡ��ͼƬ�ĸ�ʽ����
		// Dispatch.put(WrapFormat, "Type", 5);
		// ���û��Ƹ�ʽ��0 - 7�������ǲ���˵��
		// wdWrapInline 7 ����״Ƕ�뵽�����С�
		// wdWrapNone 3 ����״��������ǰ�档����� wdWrapFront ��
		// wdWrapSquare 0 ʹ���ֻ�����״��������״����һ��������
		// wdWrapThrough 2 ʹ���ֻ�����״��
		// wdWrapTight 1 ʹ���ֽ��ܵػ�����״��
		// wdWrapTopBottom 4 �����ַ�����״���Ϸ����·���
		// wdWrapBehind 5 ����״�������ֺ��档
		// wdWrapFront 6 ����״��������ǰ�档
	}

	/**
	 * ����word�е�����
	 * 
	 * @param selection
	 *            ѡ��λ��
	 * @param toFindText
	 *            ��Ҫ���ҵ�����
	 * @return �Ƿ��ҵ������ҵ�����
	 */
	private boolean find(Dispatch selection, String toFindText) {
		// ��selection����λ�ÿ�ʼ��ѯ
		Dispatch find = Dispatch.call(selection, "Find").toDispatch();
		// ����Ҫ���ҵ��ı�
		Dispatch.put(find, "Text", toFindText);
		// ��ǰ����
		Dispatch.put(find, "Forward", "True");
		// ���ø�ʽ
		Dispatch.put(find, "Format", "True");
		// ��Сдƥ��
		Dispatch.put(find, "MatchCase", "True");
		// ȫ��ƥ��
		Dispatch.put(find, "MatchWholeWord", "True");
		// ���Ҳ�ѡ��
		return Dispatch.call(find, "Execute").getBoolean();
	}

	/**
	 * TODO �����Ż�����HTMLת��ʱ�������ز����� ����ͼƬ
	 * 
	 * @param pics
	 *            ͼƬ���ϣ�keyΪ�滻���ͼƬ�������ƣ�valueΪͼƬurl
	 * @param picBuffereds
	 *            ��ͼƬ���ϣ�keyΪ�滻���ͼƬ�������ƣ�valueΪͼƬ�ļ����λ��
	 * @param saveDir
	 *            ͼƬ�����ʱ�ļ���
	 */
	private void downloadImage(Map<String, String> pics, Map<String, String> picBuffereds, String saveDir) {
		for (String key : pics.keySet()) {

			try {
				// ��ȡͼƬ���ص�ַ��Ӧ�õ�ַΪ�ͻ��˵�ַ�����ת���ɷ���˵�ַ�������ء�
				String url = pics.get(key);
				if (url.indexOf("/bvs/fs") >= 0) {
					String id = "";
					String namespace = "";
					String fileName = "";
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
							if (para.startsWith("name=")) {
								fileName = para.replace("name=", "");
							}
						}
					}
					url = Activator.getDefault().getServer() + "/fs/" + namespace + "/" + id + "/"
							+ URLEncoder.encode(fileName, "utf-8");
					// ���ز���ͼƬ��ŵ���ʱ�ļ�����
					File file = new File(saveDir + File.separator + fileName);
					BufferedImage image = ImageUtil.getBufferedImage(new URL(url));
					ImageUtil.saveImage(image, file.getPath(), "jpg");
					picBuffereds.put(key, file.getPath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ѹ���ļ�
	 * 
	 * @param out
	 *            zip�������
	 * @param bos
	 *            ������
	 * @param sourceFile
	 *            Ҫѹ�����ļ��л��ļ�
	 * @param base
	 *            ѹ����·��
	 */
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

	/**
	 * TODO �ϲ���HTMLת����. �����ļ�
	 * 
	 * @param downloadFileUrl
	 *            Ҫ���ص��ļ���keyΪ�滻���ͼƬ�������ƣ�valueΪ�ļ�url
	 * @param saveDir
	 *            �ļ������ʱ�ļ���
	 */
	private void downloadFile(Map<String, String> downloadFileUrl, String saveDir) {
		for (String fileName : downloadFileUrl.keySet()) {
			try {
				// ��ȡ�ļ����ص�ַ��Ӧ�õ�ַΪ�ͻ��˵�ַ�����ת���ɷ���˵�ַ�������ء�
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
				// �����ļ�
				FileUtils.copyURLToFile(new URL(url), new File(saveDir + File.separator + fileName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ת��HTML
	 * 
	 * @param html
	 *            birt���ɵ�html
	 * @param host
	 *            �ͻ��˵�ַ
	 * @param downloadFileUrl
	 *            Ҫ���صĸ���
	 * @param pics
	 *            Ҫ���ص�ͼƬ
	 * @return ת�����html
	 */
	private String convertHTML(String html, String host, Map<String, String> downloadFileUrl,
			Map<String, String> pics) {
		// �滻CKEditor�еĹؼ���
		html = html.replaceAll("&amp;", "&");
		html = html.replaceAll("&quot;", "'");
		html = html.replaceAll("&lt;", "<");
		html = html.replaceAll("&gt;", ">");
		html = html.replaceAll("&amp;", "&");
		html = html.replaceAll("&amp", "&");

		// html = html.replaceAll("/bvs/fs", host + "/bvs/fs");

		html = html.replace("</head>", "<link rel='stylesheet' type='text/css'href='" + host
				+ "/bvs/widgets/ckeditor/codebase/contents.css'></head>");

		String regEx_html = "___HTML___(.*?)(___EHTML___)";
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(html);
		while (m_html.find()) {
			String o_html = m_html.group();
			String n_html = o_html.replaceAll("&#xa0;", "");
			n_html = n_html.replaceAll("<br/>", "");
			n_html = n_html.replaceAll("___HTML___", "");
			n_html = n_html.replaceAll("___EHTML___", "");
			html = html.replace(o_html, n_html);
		}

		String regEx_file = "___FILE___(.*?)(___EFILE___)";
		Pattern p_file = Pattern.compile(regEx_file, Pattern.CASE_INSENSITIVE);
		Matcher m_file = p_file.matcher(html);
		while (m_file.find()) {
			String o_file = m_file.group();
			String n_flie = o_file.replaceAll(",", "<br/>");
			n_flie = n_flie.replaceAll("___FILE___\\[", "");
			n_flie = n_flie.replaceAll("\\]___EFILE___", "");
			n_flie = n_flie.replaceAll("___FILE___", "");
			n_flie = n_flie.replaceAll("___EFILE___", "");
			html = html.replace(o_file, n_flie);
		}

		// ��ȡͼƬ
		int count = 0;
		String regEx_image = "<(img|IMG)(.*?)(>|></img>|/>)";
		Pattern p_image = Pattern.compile(regEx_image, Pattern.CASE_INSENSITIVE);
		Matcher m_image = p_image.matcher(html);
		while (m_image.find()) {
			count++;
			String o_image = m_image.group();
			String group = m_image.group(2);
			Pattern srcText = Pattern.compile("(src|SRC)=(\"|\')(.*?)(\"|\')");// ƥ��ͼƬ�ĵ�ַ
			Matcher matcher2 = srcText.matcher(group);
			if (matcher2.find()) {
				String sImg = "${imgReplace" + count + "}";
				pics.put(sImg, matcher2.group(3));
				html = html.replace(o_image, sImg);
			}
		}

		html = html.replaceAll("</p><br/>", "</p>");

		html = html.replaceAll("</p><br/>", "</p>");

		html = html.replaceAll("null", "");

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

		Document doc = Jsoup.parse(html);
		Elements tables = doc.getElementsByTag("table");
		for (Element table : tables) {
			Elements children = table.children();
			String[] widths = null;
			if ("colgroup".equals(children.get(0).tagName().toLowerCase())) {
				widths = new String[children.size() - 1];
				for (int i = 0; i < children.size() - 1; i++) {
					Element element = children.get(i);
					Elements col = element.getElementsByTag("col");
					if (col.hasAttr("style")) {
						widths[i] = col.attr("style");
					}
				}
			}
			if (widths != null) {
				Element element = children.get(children.size() - 1);
				Elements tbody = element.children();
				Elements trs = null;
				if ("tbody".equals(tbody.get(0).tagName().toLowerCase())) {
					trs = tbody.get(0).children();
				} else if ("tr".equals(tbody.get(0).tagName().toLowerCase())) {
					trs = element.children();
				}
				if (trs != null) {
					for (Element tr : trs) {
						Elements tds = tr.children();
						for (int i = 0; i < tds.size(); i++) {
							if (tds.get(i).hasAttr("colspan")) {
								int colspan = Integer.parseInt(tds.get(i).attr("colspan"));
								i = i + colspan - 1;
							} else {
								String width = widths[i];
								if (width != null) {
									if (tds.get(i).hasAttr("style")) {
										String attr = tds.get(i).attr("style");
										tds.get(i).removeAttr("style");
										tds.get(i).attr("style", attr + ";" + width);
									} else {
										tds.get(i).attr("style", width);
									}
								}
							}
						}
					}

				}

			}
		}
		html = doc.toString();
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

	public static void main(String[] args) {

	}

}
