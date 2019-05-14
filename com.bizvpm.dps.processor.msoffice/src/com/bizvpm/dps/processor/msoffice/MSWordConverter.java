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
			// 替换图片
			for (String replaceText : pics.keySet()) {
				String imgPath = pics.get(replaceText);
				// 将光标移动到起始位置
				Dispatch.call(selection, "HomeKey", new Variant(6));
				while (find(selection, replaceText)) {
					insertImage(selection, imgPath);
				}
			}
			// 参数new Variant(16)
			// word
			// 另存格式参数列表https: //
			// docs.microsoft.com/zh-cn/dotnet/api/microsoft.office.interop.word.wdsaveformat?view=word-pia
			// 在MSDN中可用WdSaveFormat 进行查询
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
	 * 向当前插入点替换图片
	 *
	 * @param imagePath
	 *            图片的路径
	 */
	private void insertImage(Dispatch selection, String imagePath) {
		// 获得图片原始尺寸并计算缩放后的尺寸
		BufferedImage image = ImageUtil.getBufferedImage(imagePath);

		int width = image.getWidth() < 200 ? image.getWidth() : 200;
		int height = width * image.getHeight() / image.getWidth();

		// 插入图片
		Dispatch picture = Dispatch.call(Dispatch.get(selection, "InLineShapes").toDispatch(), "AddPicture", imagePath)
				.getDispatch();
		Dispatch.call(picture, "Select");
		Dispatch.put(picture, "Width", new Variant(width));// 设置图片宽度
		Dispatch.put(picture, "Height", new Variant(height));// 设置图片高度
		// Dispatch ShapeRange = Dispatch.call(picture, "ConvertToShape").toDispatch();
		// // 取得图片区域
		// Dispatch WrapFormat = Dispatch.get(ShapeRange, "WrapFormat").toDispatch();
		//
		// 取得图片的格式对象
		// Dispatch.put(WrapFormat, "Type", 5);
		// 设置环绕格式（0 - 7）下面是参数说明
		// wdWrapInline 7 将形状嵌入到文字中。
		// wdWrapNone 3 将形状放在文字前面。请参阅 wdWrapFront 。
		// wdWrapSquare 0 使文字环绕形状。行在形状的另一侧延续。
		// wdWrapThrough 2 使文字环绕形状。
		// wdWrapTight 1 使文字紧密地环绕形状。
		// wdWrapTopBottom 4 将文字放在形状的上方和下方。
		// wdWrapBehind 5 将形状放在文字后面。
		// wdWrapFront 6 将形状放在文字前面。
	}

	/**
	 * 查找word中的内容
	 *
	 * @param selection
	 *            选中位置
	 * @param toFindText
	 *            需要查找的内容
	 * @return 是否找到待查找的内容
	 */
	private boolean find(Dispatch selection, String toFindText) {
		// 从selection所在位置开始查询
		Dispatch find = Dispatch.call(selection, "Find").toDispatch();
		// 设置要查找的文本
		Dispatch.put(find, "Text", toFindText);
		// 向前查找
		Dispatch.put(find, "Forward", "True");
		// 设置格式
		Dispatch.put(find, "Format", "True");
		// 大小写匹配
		Dispatch.put(find, "MatchCase", "True");
		// 全字匹配
		Dispatch.put(find, "MatchWholeWord", "True");
		// 查找并选中
		return Dispatch.call(find, "Execute").getBoolean();
	}
}
