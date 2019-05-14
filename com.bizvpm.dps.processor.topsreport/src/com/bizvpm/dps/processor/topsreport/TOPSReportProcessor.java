package com.bizvpm.dps.processor.topsreport;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class TOPSReportProcessor implements IProcessorRunable {

	private static final String PARA_REPORT_SERVERPATH = "serverPath";

	private static final String PARA_REPORT_FILE = "file";

	public TOPSReportProcessor() {
	}

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {

		InputStream inputStream = processTask.getInputStream(PARA_REPORT_FILE);
		String host = (String) processTask.get(PARA_REPORT_SERVERPATH);
		String html = IOUtils.toString(inputStream, "utf-8");
		html = convertHTML(html, host);
		ProcessResult result = new ProcessResult();
		result.put("result", html);
		result.putFile("template", new File(Activator.getDefault().getTemplatePath()));
		result.put("serverPath", Activator.getDefault().getServer());
		return result;
	}

	private String convertHTML(String html, String host) {
		// Ìæ»»CKEditorÖÐµÄ¹Ø¼ü×Ö
		html = html.replace("</head>", "<link rel='stylesheet' type='text/css'href='" + host
				+ "/bvs/widgets/ckeditor/codebase/contents.css'></head>");

		String regEx_html = "___HTML___(.*?)(___EHTML___)";
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(html);
		while (m_html.find()) {
			String o_html = m_html.group();
			String n_html = o_html.replaceAll("&#xa0;", "");
			n_html = n_html.replaceAll("<br/>", "");
			n_html = StringEscapeUtils.unescapeHtml3(n_html);
			n_html = n_html.replaceAll("___HTML___", "");
			n_html = n_html.replaceAll("___EHTML___", "");
			html = html.replace(o_html, n_html);
		}

		String regEx_arr = "___ARRAY___(.*?)(___EARRAY___)";
		Pattern p_arr = Pattern.compile(regEx_arr, Pattern.CASE_INSENSITIVE);
		Matcher m_arr = p_arr.matcher(html);
		while (m_arr.find()) {
			String o_arr = m_arr.group();
			String n_arr = StringEscapeUtils.unescapeHtml3(o_arr);
			n_arr = n_arr.replaceAll("___ARRAY___\\[", "");
			n_arr = n_arr.replaceAll("\\]___EARRAY___", "");
			n_arr = n_arr.replaceAll("___ARRAY___", "");
			n_arr = n_arr.replaceAll("___EARRAY___", "");
			html = html.replace(o_arr, n_arr);
		}

		String regEx_arrbr = "___ARRAYBR___(.*?)(___EARRAYBR___)";
		Pattern p_arrbr = Pattern.compile(regEx_arrbr, Pattern.CASE_INSENSITIVE);
		Matcher m_arrbr = p_arrbr.matcher(html);
		while (m_arrbr.find()) {
			String o_arrbr = m_arrbr.group();
			String n_arrbr = o_arrbr.replaceAll(",", "<br/>");
			n_arrbr = StringEscapeUtils.unescapeHtml3(n_arrbr);
			n_arrbr = n_arrbr.replaceAll("___ARRAYBR___", "");
			n_arrbr = n_arrbr.replaceAll("___EARRAYBR___", "");
			html = html.replace(o_arrbr, n_arrbr);
		}

		html = html.replaceAll("</p><br/>", "</p>");

		html = html.replaceAll("</p><br/>", "</p>");

		html = html.replaceAll("null", "");

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
}
