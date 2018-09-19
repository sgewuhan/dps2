package com.bizvpm.dps.processor.acmecad;

import java.io.File;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.processor.acmecad.preferences.AcmePreferenceConstants;
import com.bizvpm.dps.runtime.DPSUtil;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class AcmeCADProcessor implements IProcessorRunable {

	private String sourceType;
	private String targetType;
	private File inputFile;
	private File outputFile;
	private String parameter;
	private File presetWatermarkFile = null;

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor,
			IProcessContext context) throws Exception {
		init(processTask);

		convert();

		ProcessResult r = new ProcessResult();
		r.putByteArray("file", outputFile);

		inputFile.delete();
		outputFile.delete();
		if (presetWatermarkFile != null) {
			presetWatermarkFile.delete();
		}

		return r;
	}

	private void init(ProcessTask processTask) throws Exception {
		long time = new Date().getTime();
		String pathName = DPSUtil.getTempDirector(getClass(), true);
		sourceType = (String) processTask.get("sourceType");
		targetType = (String) processTask.get("targetType");
		inputFile = new File(pathName + time + "." + sourceType);
		outputFile = new File(pathName + time + "." + targetType);

		parameter = " /r /i";

		parameter += setAutoZoomExtend(
				(Boolean) processTask.get("autoZoomExtend"));

		parameter += setRasterPixel((int) processTask.get("rasterPixel"));

		parameter += setBackgroundColor(
				(int) processTask.get("backgroundColor"));

		parameter += setLineWeight((int) processTask.get("lineWeight"));

		parameter += setAutoSize((Boolean) processTask.get("autoSize"));

		parameter += setRasterWidth((String) processTask.get("rasterWidth"));

		parameter += setRasterHeight((String) processTask.get("rasterHeight"));

		parameter += setLayoutPaperSize(
				(Boolean) processTask.get("layoutPaperSize"));

		parameter += setMaskRaster((Boolean) processTask.get("maskRaster"));

		parameter += setPresetWatermark(
				(Boolean) processTask.get("presetWatermark"));

		if (processTask.get("presetWatermarkFile") != null) {
			presetWatermarkFile = new File(pathName + time + ".wmk");
			processTask.writeToFile("presetWatermarkFile", presetWatermarkFile);
			parameter += setPresetWatermarkFile();
		}

		parameter += setDPI((Integer) processTask.get("dpi"));

		parameter += setJpegQuality((Integer) processTask.get("jpegQuality"));

		parameter += setPenSetName((String) processTask.get("penSetName"));

		parameter += setLineRemoval((Integer) processTask.get("lineRemoval"));

		parameter += setScale((Double) processTask.get("scale"));

		parameter += setMargin((Double) processTask.get("margin"));

		parameter += setFormat();

		processTask.writeToFile("file", inputFile);
	}

	private String setMargin(Double margin) {
		if (margin != null) {
			return " /margin " + margin;
		}
		return "";
	}

	private String setScale(Double scale) {
		if (scale != null) {
			return " /scale " + scale;
		}
		return "";
	}

	private String setLineRemoval(Integer lineRemoval) {
		if (lineRemoval != null) {
			return " /hide " + lineRemoval;
		}
		return "";
	}

	private String setPenSetName(String penSetName) {
		if (penSetName != null) {
			return " /pw \"" + penSetName + "\"";
		}
		return "";
	}

	private String setJpegQuality(Integer jpegQuality) {
		if (jpegQuality != null) {
			return " /j " + jpegQuality;
		}
		return "";
	}

	private String setDPI(Integer dpi) {
		if (dpi != null) {
			return " /res " + dpi;
		}
		return "";
	}

	private String setPresetWatermarkFile() {
		return " /wmfile \"" + presetWatermarkFile.getPath() + "\"";
	}

	private String setPresetWatermark(Boolean presetWatermark) {
		if (Boolean.TRUE.equals(presetWatermark)) {
			return " /wm";
		}
		return "";
	}

	private String setMaskRaster(Boolean maskRaster) {
		if (Boolean.TRUE.equals(maskRaster)) {
			return " /m";
		}
		return "";
	}

	private String setRasterHeight(String rasterHeight) {
		if (rasterHeight != null) {
			return " /h " + rasterHeight;
		}
		return "";
	}

	private String setRasterWidth(String rasterWidth) {
		if (rasterWidth != null) {
			return " /w " + rasterWidth;
		}
		return "";
	}

	private String setLayoutPaperSize(Boolean layoutPaperSize) {
		if (Boolean.TRUE.equals(layoutPaperSize)) {
			return " /ls";
		}
		return "";
	}

	private String setAutoSize(Boolean autoSize) {
		if (Boolean.TRUE.equals(autoSize)) {
			return " /ad";
		}
		return "";
	}

	private String setLineWeight(int lineWeight) {
		return " /lw " + lineWeight;
	}

	private String setBackgroundColor(int backgroundColor) {
		return " /b " + backgroundColor;
	}

	private String setRasterPixel(int rasterPixelFormat) {
		return " /p " + rasterPixelFormat;
	}

	private String setAutoZoomExtend(Boolean autoZoomExtend) {
		if (Boolean.TRUE.equals(autoZoomExtend)) {
			return " /e";
		}
		return "";

	}

	private String setFormat() {
		String format = "";
		targetType = targetType.toLowerCase();
		if ("bmp".equals(targetType)) {
			format = "1";
		} else if ("jpg".equals(targetType)) {
			format = "2";
		} else if ("gif".equals(targetType)) {
			format = "3";
		} else if ("pcx".equals(targetType)) {
			format = "4";
		} else if ("tiff".equals(targetType)) {
			format = "5";
		} else if ("png".equals(targetType)) {
			format = "6";
		} else if ("tga".equals(targetType)) {
			format = "7";
		} else if ("wmf".equals(targetType)) {
			format = "8";
		} else if ("svg".equals(targetType)) {
			format = "101";
		} else if ("plt".equals(targetType)) {
			format = "102";
		} else if ("hgl".equals(targetType)) {
			format = "103";
		} else if ("pdf".equals(targetType)) {
			format = "104";
		} else if ("svgz".equals(targetType)) {
			format = "106";
		} else if ("cgm".equals(targetType)) {
			format = "107";
		} else if ("eps".equals(targetType)) {
			format = "108";
		}
		return " /f " + format;
	}

	private void convert() throws Exception {
		String cadConverterPath = Activator.getDefault().getPreferenceStore()
				.getString(AcmePreferenceConstants.CAD_CONVERTER_PATH);
		if (cadConverterPath != null) {
			String cmdString = cadConverterPath + parameter
					+ " / o \"" + outputFile.getPath() + "\" \""
					+ inputFile.getPath() + "\"";
			Process process = Runtime.getRuntime().exec(cmdString);
			process.waitFor();
			
		} else {
			throw new Exception("Can not find CAD_CONVERTER_PATH");
		}
	}

}
