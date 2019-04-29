package com.bizvpm.dps.processor.topsreport;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

public class ImageUtil {

	public static Image getImage(Device display, File file) {
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			Image image = new Image(display, stream);

			return image;

		} catch (FileNotFoundException e) {
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException unexpected) {
			}
		}

		return null;
	}

	public static Image getImage(Device display, String name) {
		InputStream stream = null;
		try {
			stream = new FileInputStream(name);
			Image image = new Image(display, stream);

			return image;

		} catch (FileNotFoundException e) {
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException unexpected) {
			}
		}

		return null;
	}

	public static BufferedImage getBufferedImage(String name) {

		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(name));
		} catch (IOException e) {
		}

		return image;
	}

	public static BufferedImage getBufferedImage(InputStream is) {

		BufferedImage image = null;
		try {
			image = ImageIO.read(is);
		} catch (IOException e) {
		}
		return image;
	}

	public static BufferedImage getBufferedImage(URL url) {

		BufferedImage image = null;
		try {
			image = ImageIO.read(url);
		} catch (IOException e) {
		}
		return image;
	}

	public static Image scaleFitImage(Image image, int width, int height) {
		Rectangle sourceBounds = image.getBounds();
		ImageData imageData = image.getImageData();

		if (sourceBounds.width > width || sourceBounds.height > height) {
			float ratioW = ((float) width) / ((float) sourceBounds.width);
			float ratioH = ((float) height) / ((float) sourceBounds.height);
			float ratio = Math.min(ratioW, ratioH);

			int nwidth = (int) (sourceBounds.width * ratio);
			int nheight = (int) (sourceBounds.height * ratio);
			return new Image(image.getDevice(), imageData.scaledTo(nwidth, nheight));
		} else {
			return new Image(image.getDevice(), imageData);
		}
	}

	public static ImageData scaleFitImageData(Image image, int width, int height) {
		Rectangle sourceBounds = image.getBounds();
		ImageData imageData = image.getImageData();

		if (sourceBounds.width > width || sourceBounds.height > height) {
			float ratioW = ((float) width) / ((float) sourceBounds.width);
			float ratioH = ((float) height) / ((float) sourceBounds.height);
			float ratio = Math.min(ratioW, ratioH);

			int nwidth = (int) (sourceBounds.width * ratio);
			int nheight = (int) (sourceBounds.height * ratio);
			return imageData.scaledTo(nwidth, nheight);
		} else {
			return imageData;
		}
	}

	public static Rectangle getFitableBounds(float sourceWidth, float sourceHeight, float targetWidth,
			float targetHeight) {

		int srcX;
		int srcY;
		int srcWidth;
		int srcHeight;
		float targetWHRatio = targetWidth / targetHeight;
		float sourceWHRatio = sourceWidth / sourceHeight;

		if (targetWHRatio >= sourceWHRatio) {
			srcWidth = (int) sourceWidth;
			srcX = 0;
			srcHeight = Math.round(sourceWidth / targetWHRatio);
			srcY = Math.round((sourceHeight - sourceWidth / targetWHRatio) / 2);
		} else {
			srcHeight = (int) sourceHeight;
			srcY = 0;
			srcWidth = Math.round(srcHeight * targetWHRatio);
			srcX = Math.round((sourceWidth - srcHeight * targetWHRatio) / 2);

		}
		return new Rectangle(srcX, srcY, srcWidth, srcHeight);
	}

	/**
	 * 鐩存帴浣跨敤GC杩涜鎿嶄綔
	 * 
	 * @param image
	 * @param targetWidth
	 * @param targetHeight
	 * @param drawable
	 * @return
	 */
	public static void drawImage(Drawable drawable, Image image, int targetWidth, int targetHeight) {
		Rectangle bounds = getFitableBounds(image.getBounds().width, image.getBounds().height, targetWidth,
				targetHeight);
		GC gc = new GC(drawable);
		gc.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, targetWidth, targetHeight);
		gc.dispose();
	}

	/**
	 * 灏嗗浘鐗囬�搴斿埌�?圭洰鏍囧昂�?�?
	 * 
	 * @param image
	 * @param targetWidth
	 * @param targetHeight
	 * @return
	 */
	public static Image fitImage(Image image, int targetWidth, int targetHeight) {

		BufferedImage awtSrcImage = convertToAWT(image.getImageData());
		Rectangle bounds = getFitableBounds(image.getBounds().width, image.getBounds().height, targetWidth,
				targetHeight);
		// 鍓鑷冲悎閫傜殑灏哄
		BufferedImage awtClipImage = awtSrcImage.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
		// 缂╂斁鑷冲搴斿昂�?�?
		BufferedImage awtTgtImage = new BufferedImage(targetWidth, targetHeight, awtSrcImage.getType());
		java.awt.Image awtimage = awtClipImage.getScaledInstance(targetWidth, targetHeight,
				java.awt.Image.SCALE_SMOOTH);
		Graphics g = awtTgtImage.getGraphics();
		g.drawImage(awtimage, 0, 0, null); // 缁樺埗鐩爣鍥�
		g.dispose();

		// 娴嬭瘯浣跨敤
		// try {
		// saveImage(awtTgtImage, "d:/temp/" + System.currentTimeMillis()
		// + ".jpg", "jpg");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		ImageData imageData = convertToSWT(awtTgtImage);

		return new Image(null, imageData);
	}

	/**
	 * 灏嗗浘鐗囬�搴斿埌�?圭洰鏍囧昂�?�?
	 * 
	 * @param image
	 * @param targetWidth
	 * @param targetHeight
	 * @return
	 */
	public static BufferedImage fitImage(BufferedImage image, int targetWidth, int targetHeight) {

		Rectangle bounds = getFitableBounds(image.getWidth(), image.getHeight(), targetWidth, targetHeight);

		BufferedImage awtClipImage = image.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);

		if (bounds.width == targetWidth || bounds.height == targetHeight) {
			return awtClipImage;
		}

		BufferedImage targetImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		targetImage.getGraphics().drawImage(
				awtClipImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

		return targetImage;
	}

	public static Image getImage(Device device, BufferedImage image) {
		ImageData imageData = convertToSWT(image);
		return new Image(device, imageData);
	}

	/**
	 * 
	 * @param org
	 *            .eclipse.swt.graphics.ImageData;
	 * @return java.awt.image.BufferedImage;
	 */
	private static BufferedImage convertToAWT(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					pixelArray[0] = rgb.red;
					pixelArray[1] = rgb.green;
					pixelArray[2] = rgb.blue;
					raster.setPixels(x, y, 1, 1, pixelArray);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}

	/**
	 * 
	 * @param java
	 *            .awt.image.BufferedImage;
	 * @return org.eclipse.swt.graphics.ImageData;
	 */
	private static ImageData convertToSWT(BufferedImage bufferedImage) {
		ColorModel colorModel = bufferedImage.getColorModel();

		if (colorModel instanceof DirectColorModel) {
			DirectColorModel cm = (DirectColorModel) colorModel;
			PaletteData palette = new PaletteData(cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), cm.getPixelSize(),
					palette);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					try {
						raster.getPixel(x, y, pixelArray);
						int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
						data.setPixel(x, y, pixel);

					} catch (Exception e) {
					}
				}
			}
			return data;
		} else if (colorModel instanceof IndexColorModel) {
			IndexColorModel cm = (IndexColorModel) colorModel;
			int size = cm.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			cm.getReds(reds);
			cm.getGreens(greens);
			cm.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), cm.getPixelSize(),
					palette);
			data.transparentPixel = cm.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		} else if (colorModel instanceof ComponentColorModel) {
			// ComponentColorModel cm = (ComponentColorModel) colorModel;
			// PaletteData palette = new PaletteData(cm.getRedMask(),
			// cm.getGreenMask(), cm.getBlueMask());
			// ImageData data = new ImageData(bufferedImage.getWidth(),
			// bufferedImage.getHeight(),
			// cm.getPixelSize(), palette);
			// WritableRaster raster = bufferedImage.getRaster();
			// int[] pixelArray =colorModel.getComponentSize();
			// for (int y = 0; y < data.height; y++) {
			// for (int x = 0; x < data.width; x++) {
			// raster.getPixel(x, y, pixelArray);
			// int pixel = palette.getPixel(new RGB(pixelArray[0],
			// pixelArray[1], pixelArray[2]));
			// data.setPixel(x, y, pixel);
			// }
			// }
			// return data;

			// ASSUMES: 3 BYTE BGR IMAGE TYPE

			PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);

			// This is valid because we are using a 3-byte Data model with no
			// transparent pixels
			data.transparentPixel = -1;

			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;

		}
		return null;
	}

	public static void saveImage(RenderedImage image, String fileName, String formatName) {
		FileOutputStream out = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(image, formatName, bos);
			out = new FileOutputStream(fileName);
			out.write(bos.toByteArray());
		} catch (IOException e) {
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}
	}

	// public static void genarateNumber(int from, int to, int sizeX, int sizeY,
	// String destFolder) {
	// if (to < from) {
	// return;
	// }
	// Color[] colors = { Color.ORANGE, Color.BLACK };
	// for (int i = from; i <= to; i++) {
	// BufferedImage bi = new BufferedImage(sizeX, sizeY,
	// BufferedImage.TYPE_INT_BGR);
	// Graphics2D g2d = (Graphics2D) bi.getGraphics();
	// Font f = new Font("Arial", Font.BOLD, 48);
	// g2d.setFont(f);
	// g2d.setColor(colors[i % 2]);
	// g2d.fillRect(0, 0, sizeX, sizeY);
	// g2d.setColor(Color.WHITE);
	// g2d.drawString(" "+i, 50, 100);
	//
	// DecimalFormat nf = new DecimalFormat("000");
	// saveImage(bi,destFolder+"/"+nf.format(i)+".jpg","jpg");
	// }
	// }

	// public static void main(String[] args) {
	// // 瀵规煇涓簱閲岄潰鐨勫浘鐗囪繘琛屽眳涓苟鍓潗閫傚簲澶у�?
	// String destFolder = "D:/temp";
	// String sourceFolder = "D:/lib/picture/source/search2_files";
	// // transfer(sourceFolder, destFolder, 150, 150);
	// transfer(sourceFolder,destFolder,148,70);
	// // genarateNumber(0,48,150,150,destFolder);
	// }

	public static InputStream transfer(InputStream is, int width, int height) {
		BufferedImage bf = getBufferedImage(is);
		try {
			is.close();
		} catch (IOException e1) {
		}
		if (bf == null) {
			return null;
		}
		int sourceWidth = bf.getWidth();
		int sourceHeight = bf.getWidth();
		if (sourceWidth < width || sourceHeight < height) {
			return null;
		}

		BufferedImage bf2 = fitImage(bf, width, height);
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(bf2, "jpeg", os); //$NON-NLS-1$
			InputStream is2 = new ByteArrayInputStream(os.toByteArray());
			os.close();
			return is2;
		} catch (IOException e) {
		}
		return null;
	}

	public static void transfer(String sourcePath, String destPath, int width, int height) {
		String targetPath = destPath + "/" + width + "_" + height + "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File targetFolder = new File(targetPath);
		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
		}

		File folder = new File(sourcePath);
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			BufferedImage bf = getBufferedImage(file.getPath());
			if (bf == null) {
				// System.out.println(" not a valid image file");
				continue;
			}
			int sourceWidth = bf.getWidth();
			int sourceHeight = bf.getWidth();
			if (sourceWidth < width || sourceHeight < height) {
				continue;
			}

			BufferedImage bf2 = fitImage(bf, width, height);
			String fileName = targetPath + file.getName();
			saveImage(bf2, fileName, fileName.substring(fileName.lastIndexOf(".") + 1)); //$NON-NLS-1$
		}
	}

	public static void main(String[] args) {
		try {
			BufferedImage source = ImageIO.read(new File("h:/1.jpg"));
			BufferedImage target = getRoundClipImage(source);
			ImageIO.write(target, "PNG", new File("h:/3.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage getRoundClipImage(BufferedImage source) throws IOException {

		// ������Ҫ�Ƿ�ʹ�� BufferedImage.TYPE_INT_ARGB
		BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, source.getWidth(), source.getHeight());

		Graphics2D g2 = image.createGraphics();
		image = g2.getDeviceConfiguration().createCompatibleImage(source.getWidth(), source.getHeight(),
				Transparency.TRANSLUCENT);
		g2 = image.createGraphics();
		g2.setComposite(AlphaComposite.Clear);
		g2.fill(new java.awt.Rectangle(image.getWidth(), image.getHeight()));
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
		g2.setClip(shape);
		// ʹ�� setRenderingHint ���ÿ����
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(source, 0, 0, null);
		g2.dispose();
		return image;
	}

	public static ImageData getRoundClipImage(Image image, int width, int height) {
		ImageData sourceImageData = scaleFitImageData(image, width, height);
		BufferedImage bufferSource = convertToAWT(sourceImageData);
		// ������Ҫ�Ƿ�ʹ�� BufferedImage.TYPE_INT_ARGB
		BufferedImage bufferTarget = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, width, height);

		Graphics2D g2 = bufferTarget.createGraphics();
		bufferTarget = g2.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		g2 = bufferTarget.createGraphics();
		g2.setComposite(AlphaComposite.Clear);
		g2.fill(new java.awt.Rectangle(width, height));
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
		g2.setClip(shape);
		// ʹ�� setRenderingHint ���ÿ����
		g2.drawImage(bufferSource, 0, 0, null);
		g2.dispose();
		try {
			ImageIO.write(bufferTarget, "PNG", new File("h:/4.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return convertToSWT(bufferTarget);
	}

}
