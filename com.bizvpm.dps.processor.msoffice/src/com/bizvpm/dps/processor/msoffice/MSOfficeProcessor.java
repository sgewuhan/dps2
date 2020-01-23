package com.bizvpm.dps.processor.msoffice;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
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
	private File template;
	private Map<String, String> pics;
	private String serverPath;
	private String pathName;
	private long time;
	private File img;
	private File att;
	private boolean returnZIP;
	private boolean hasImage;
	private boolean hasAtt;

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		init(processTask);
		convert();

		ProcessResult r = new ProcessResult();
		r.putByteArray("file", outputFile);

		File file = new File(pathName + time);
		file.delete();

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
			dis = msOfficeConverter.openDocument(app, filename, template.getPath());
			msOfficeConverter.convert(app, dis, inputFile.getPath(), toFilename, pics);
		} catch (Exception e) {
			throw e;
		} finally {
			msOfficeConverter.dispose(app, dis);
			ComThread.Release();
		}

		if (returnZIP) {
			// 创建zip输出流
			File out = outputFile;
			outputFile = new File(pathName + time + File.separator + time + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputFile));

			// 创建缓冲输出流
			BufferedOutputStream bos = new BufferedOutputStream(zipOut);
			// 调用函数
			if (hasImage)
				compress(zipOut, bos, img, img.getName());

			if (hasAtt)
				compress(zipOut, bos, att, att.getName());

			compress(zipOut, bos, out, out.getName());

			bos.close();
			zipOut.close();
		}

	}

	private void init(ProcessTask processTask) throws Exception {
		pics = new HashMap<String, String>();
		sourceType = (String) processTask.get("sourceType");
		targetType = (String) processTask.get("targetType");

		serverPath = (String) processTask.get("serverPath");

		String targetName = (String) processTask.get("targetName");

		time = new Date().getTime();

		pathName = DPSUtil.getTempDirector(getClass(), true);

		inputFile = new File(pathName + time + File.separator + time + "." + sourceType);
		outputFile = new File(pathName + time + File.separator + time + "." + targetType);

		img = new File(pathName + time + File.separator + "image");
		img.mkdirs();
		att = new File(pathName + time + File.separator + "附件");
		att.mkdirs();

		Object file = processTask.get("file");
		if (file instanceof String) {
			inputFile = new File(pathName + time + File.separator + targetName + "." + sourceType);
			outputFile = new File(pathName + time + File.separator + targetName + "." + targetType);
			String html = convertHTML((String) file);
			ByteArrayInputStream ins = new ByteArrayInputStream(html.getBytes("UTF-8"));
			OutputStream os = new FileOutputStream(inputFile);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			ins.close();

		} else {
			processTask.writeToFile("file", inputFile);
		}
		template = new File(pathName + "normal.dotx");
		Object t = processTask.get("template");
		if (t instanceof String) {
			template = new File((String) t);
		}
		if (template.isFile()) {
			processTask.writeToFile("template", template);
		}

		returnZIP = Boolean.TRUE.equals(processTask.get("returnZIP"));
		hasImage = Boolean.TRUE.equals(processTask.get("hasImage"));
		hasAtt = Boolean.TRUE.equals(processTask.get("hasAtt"));
	}

	private String convertHTML(String html) {
		// 获取图片
		int count = 0;
		String regEx_image = "<(img|IMG)(.*?)(>|></img>|/>)";
		Pattern p_image = Pattern.compile(regEx_image, Pattern.CASE_INSENSITIVE);
		Matcher m_image = p_image.matcher(html);
		while (m_image.find()) {
			count++;
			String o_image = m_image.group();
			String group = m_image.group(2);
			Pattern srcText = Pattern.compile("(src|SRC)=(\"|\')(.*?)(\"|\')");// 匹配图片的地址
			Matcher matcher2 = srcText.matcher(group);
			if (matcher2.find()) {
				String sImg = "${imgReplace" + count + "}";
				try {
					String src = StringEscapeUtils.unescapeHtml3(matcher2.group(3));
					String imagePath = downloadImage(count, src, img.getPath());
					pics.put(sImg, imagePath);
					html = html.replace(o_image, sImg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// 格式化下载文件并获取下载文件地址。

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
				String group = StringEscapeUtils.unescapeHtml3(m_href.group(1));
				downloadFile(fileName, group, att.getPath());
			}
			html = html.replace(o_a, fileName);
		}
		return html;
	}

	private String downloadImage(int count, String url, String saveDir) throws Exception {
		// 获取图片下载地址，应该地址为客户端地址，因此转换成服务端地址进行下载。
		File file;
		if (url.indexOf("/bvs/fs") >= 0) {
			String id = "";
			String namespace = "";
			String domain = "";
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
					if (para.startsWith("domain=")) {
						domain = para.replace("domain=", "");
					}
				}
			}
			url = serverPath + "/fs/" + domain + "/" + namespace + "/" + id + "/"
					+ URLEncoder.encode(fileName, "utf-8");
			// 下载并将图片存放到临时文件夹中
			file = new File(saveDir + File.separator + fileName);
			BufferedImage image = ImageUtil.getBufferedImage(new URL(url));
			ImageUtil.saveImage(image, file.getPath(), "jpg");
			return file.getPath();
		} else if (url.startsWith("http")) {
			count++;
			BufferedImage image = ImageUtil.getBufferedImage(new URL(url));
			file = new File(saveDir + File.separator + count + ".jpg");
			ImageUtil.saveImage(image, file.getPath(), "jpg");
			return file.getPath();
		} else {
			BufferedImage image = ImageUtil.getBufferedImage(url);
			file = new File(saveDir + File.separator + count + ".jpg");
			ImageUtil.saveImage(image, file.getPath(), "jpg");
			return file.getPath();
		}
	}

	private void downloadFile(String fileName, String url, String saveDir) {
		try {
			// 获取文件下载地址，应该地址为客户端地址，因此转换成服务端地址进行下载。
			if (url.indexOf("/bvs/fs") >= 0) {
				String id = "";
				String namespace = "";
				String domain = "";
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
						if (para.startsWith("domain=")) {
							domain = para.replace("domain=", "");
						}
					}
				}
				url = serverPath + "/fs/" + domain + "/" + namespace + "/" + id + "/"
						+ URLEncoder.encode(fileName, "utf-8");
			}
			// 下载文件
			FileUtils.copyURLToFile(new URL(url), new File(saveDir + File.separator + fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 压缩文件
	 * 
	 * @param out
	 *            zip包输出流
	 * @param bos
	 *            缓冲流
	 * @param sourceFile
	 *            要压缩的文件夹或文件
	 * @param base
	 *            压缩的路径
	 * @throws Exception
	 */
	private void compress(ZipOutputStream out, BufferedOutputStream bos, File sourceFile, String base)
			throws Exception {
		// 如果路径为目录（文件夹）
		if (sourceFile.isDirectory()) {

			// 取出文件夹中的文件（或子文件夹）
			File[] flist = sourceFile.listFiles();

			if (flist.length == 0)// 如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
			{
				System.out.println(base + "/");
				out.putNextEntry(new ZipEntry(base + "/"));
			} else// 如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
			{
				for (int i = 0; i < flist.length; i++) {
					compress(out, bos, flist[i], base + "/" + flist[i].getName());
				}
			}
		} else// 如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
		{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
			out.putNextEntry(new ZipEntry(base));
			out.write(FileUtils.readFileToByteArray(sourceFile));
			IOUtils.closeQuietly(bis);
			out.flush();
			out.closeEntry();
		}
	}
}
