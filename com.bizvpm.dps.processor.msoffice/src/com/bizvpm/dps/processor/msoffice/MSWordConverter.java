package com.bizvpm.dps.processor.msoffice;

import java.awt.image.BufferedImage;
import java.util.Map;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class MSWordConverter extends AbstractMSOfficeConverter {

	public MSWordConverter(String sourceType, String targetType) {
		super.sourceType = sourceType;
		super.targetType = targetType;
	}

	@Override
	public ActiveXComponent getActiveXComponent() throws Exception {
		ActiveXComponent app = new ActiveXComponent("Word.Application");
		app.setProperty("Visible", false);
		return app;
	}

	@Override
	public Dispatch openDocument(ActiveXComponent app, String filename, String templatePath) throws Exception {
		Dispatch dis = app.getProperty("Documents").toDispatch();
		int targett = getFileType(targetType);
		if (targett == FILETYPE_PDF_FILE) {
			dis = Dispatch.invoke(dis, "Open", Dispatch.Method,
					new Object[] { filename, new Variant(false), new Variant(true), new Variant(false) }, new int[1])
					.toDispatch();
			Dispatch.put(dis, "RemovePersonalInformation", false);
		} else {
			dis = app.getProperty("Documents").toDispatch();
			dis = Dispatch.call(dis, "Open", templatePath).toDispatch();
		}
		return dis;
	}

	@Override
	public void convert(ActiveXComponent app, Dispatch dis, String fromFilename, String toFilename,
			Map<String, String> pics) throws Exception {
		int targett = getFileType(targetType);
		if (targett == FILETYPE_PDF_FILE) {
			Dispatch.call(dis, "ExportAsFixedFormat", toFilename, 17);
		} else {
			Dispatch selection = app.getProperty("Selection").toDispatch();
			Dispatch.invoke(selection, "InsertFile", Dispatch.Method,
					new Object[] { fromFilename, "", new Variant(false), new Variant(false), new Variant(false) },
					new int[3]);
			// �滻ͼƬ
			for (String replaceText : pics.keySet()) {
				String imgPath = pics.get(replaceText);
				// ������ƶ�����ʼλ��
				Dispatch.call(selection, "HomeKey", new Variant(6));
				while (find(selection, replaceText)) {
					insertImage(selection, imgPath);
				}
			}
			// ����new Variant(16)
			// word
			// ����ʽ�����б�https: //
			// docs.microsoft.com/zh-cn/dotnet/api/microsoft.office.interop.word.wdsaveformat?view=word-pia
			// ��MSDN�п���WdSaveFormat ���в�ѯ
			Dispatch.invoke(dis, "SaveAs", Dispatch.Method, new Object[] { toFilename, new Variant(16) }, new int[1]);
		}
	}

	@Override
	public void dispose(ActiveXComponent app, Dispatch dis) throws Exception {
		if (dis != null) {
			Dispatch.call(dis, "Close", false);
		}
		if (app != null) {
			app.invoke("Quit", 0);
			app = null;
		}
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

		int width = image.getWidth() < 200 ? image.getWidth() : 200;
		int height = width * image.getHeight() / image.getWidth();

		// ����ͼƬ
		Dispatch picture = Dispatch.call(Dispatch.get(selection, "InLineShapes").toDispatch(), "AddPicture", imagePath)
				.getDispatch();
		Dispatch.call(picture, "Select");
		Dispatch.put(picture, "Width", new Variant(width));// ����ͼƬ���
		Dispatch.put(picture, "Height", new Variant(height));// ����ͼƬ�߶�
		// Dispatch ShapeRange = Dispatch.call(picture, "ConvertToShape").toDispatch();
		// // ȡ��ͼƬ����
		// Dispatch WrapFormat = Dispatch.get(ShapeRange, "WrapFormat").toDispatch();
		//
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
}
