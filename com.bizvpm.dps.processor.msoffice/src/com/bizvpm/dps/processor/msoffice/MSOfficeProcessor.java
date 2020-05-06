package com.bizvpm.dps.processor.msoffice;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
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

import javax.swing.ImageIcon;

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
	private Map<String, String> pics;
	private Map<String, String> p;
	private String serverPath;
	private String pathName;
	private long time;
	private File img;
	private File att;
	private boolean returnZIP;
	private boolean hasImage;
	private boolean hasAtt;
	private String templatePath;

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
			dis = msOfficeConverter.openDocument(app, filename, templatePath);
			msOfficeConverter.convert(app, dis, inputFile.getPath(), toFilename, pics, p);
		} catch (Exception e) {
			throw e;
		} finally {
			msOfficeConverter.dispose(app, dis);
			try {
				ComThread.Release();
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		if (returnZIP) {
			// ����zip�����
			File out = outputFile;
			outputFile = new File(pathName + time + File.separator + time + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputFile));

			// �������������
			BufferedOutputStream bos = new BufferedOutputStream(zipOut);
			// ���ú���
			if (hasImage)
				compress(zipOut, bos, img, img.getName());

			if (hasAtt)
				compress(zipOut, bos, att, att.getName());

			compress(zipOut, bos, out, out.getName());

			bos.close();
			zipOut.close();
		}

	}

	@SuppressWarnings("unchecked")
	private void init(ProcessTask processTask) throws Exception {
		sourceType = (String) processTask.get("sourceType");
		targetType = (String) processTask.get("targetType");

		time = new Date().getTime();

		pathName = DPSUtil.getTempDirector(getClass(), true);

		File path = new File(pathName + time);
		path.mkdirs();

		inputFile = new File(pathName + time + File.separator + time + "." + sourceType);
		outputFile = new File(pathName + time + File.separator + time + "." + targetType);

		int targett = AbstractMSOfficeConverter.getFileType(targetType);
		if (targett != AbstractMSOfficeConverter.FILETYPE_PDF_FILE) {
			pics = new HashMap<String, String>();
			serverPath = (String) processTask.get("serverPath");

			p = (Map<String, String>) processTask.get("parameter");

			img = new File(pathName + time + File.separator + "image");
			img.mkdirs();
			att = new File(pathName + time + File.separator + "����");
			att.mkdirs();

			File template = new File(pathName + "normal.dotx");
			Object t = processTask.get("template");
			if (t instanceof String) {
				template = new File((String) t);
			}
			if (template.isFile()) {
				processTask.writeToFile("template", template);
			}

			templatePath = template.getPath();

			returnZIP = Boolean.TRUE.equals(processTask.get("returnZIP"));
			hasImage = Boolean.TRUE.equals(processTask.get("hasImage"));
			hasAtt = Boolean.TRUE.equals(processTask.get("hasAtt"));
		}
		
		Object file = processTask.get("file");
		if (file instanceof String) {
			String targetName = (String) processTask.get("targetName");

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
	}

	private String convertHTML(String html) {
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
				String group = StringEscapeUtils.unescapeHtml3(m_href.group(1));
				downloadFile(fileName, group, att.getPath());
			}
			html = html.replace(o_a, fileName);
		}
		return html;
	}

	private String downloadImage(int count, String url, String saveDir) throws Exception {
		// ��ȡͼƬ���ص�ַ��Ӧ�õ�ַΪ�ͻ��˵�ַ�����ת���ɷ���˵�ַ�������ء�
		File file;
		Image image;
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
			// ���ز���ͼƬ��ŵ���ʱ�ļ�����
			file = new File(saveDir + File.separator + id + ".jpg");
			image = Toolkit.getDefaultToolkit().getImage(new URL(url));
		} else if (url.startsWith("http")) {
			count++;
			image = Toolkit.getDefaultToolkit().getImage(new URL(url));
			file = new File(saveDir + File.separator + count + ".jpg");
		} else {
			image = Toolkit.getDefaultToolkit().getImage(url);
			// image = ImageUtil.getBufferedImage(url);
			file = new File(saveDir + File.separator + count + ".jpg");
		}
		ImageUtil.saveImage(toBufferedImage(image), file.getPath(), "jpg");
		return file.getPath();
	}

	public BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			int transparency = Transparency.OPAQUE;
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (Exception e) {
			// The system does not have a screen
		}
		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	private void downloadFile(String fileName, String url, String saveDir) {
		try {
			// ��ȡ�ļ����ص�ַ��Ӧ�õ�ַΪ�ͻ��˵�ַ�����ת���ɷ���˵�ַ�������ء�
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
				// �����ļ�
				FileUtils.copyURLToFile(new URL(url), new File(saveDir + File.separator + id));
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	 * @throws Exception
	 */
	private void compress(ZipOutputStream out, BufferedOutputStream bos, File sourceFile, String base)
			throws Exception {
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
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
			out.putNextEntry(new ZipEntry(base));
			out.write(FileUtils.readFileToByteArray(sourceFile));
			IOUtils.closeQuietly(bis);
			out.flush();
			out.closeEntry();
		}
	}
}
