package com.bizvpm.dps.processor.pmsvis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.processor.mongodbds.Domain;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;

public abstract class AbstractVisualService implements IProcessorRunable {

	private static final String DEFAULT_GENERIC = "com.bizvpm.dps.processor.openoffice:openoffice.converter";
	private static final String DEFAULT_DWG = "com.bizvpm.dps.processor.acmecad:acmecad.acmecadconverter";

	@Override
	public ProcessResult run(ProcessTask pT, IProgressMonitor monitor, IProcessContext context) throws Exception {

		// ȡ���ļ���¼
		ObjectId _id = new ObjectId((String) pT.get("_id"));
		MongoDatabase db = Domain.getDatabase((String) pT.get("domain"));
		String colName = (String) pT.get("col");

		updateResult(_id, db, colName, null, "����ת�����ӻ��ļ�");

		GridFSBucket bucket = GridFSBuckets.create(db, colName);
		GridFSFile file = bucket.find(Filters.eq("_id", _id)).first();
		Assert.isNotNull(file, "�޷����ָ����Դ�ļ�");
		// // ����ļ���MIME����
		// String contentType = Optional.ofNullable(file.getMetadata()).map(d ->
		// d.getString("contentType")).orElse("");
		// if (contentType.isEmpty())
		// contentType = "application/octet-stream";

		// ����ļ���
		GridFSDownloadStream os = bucket.openDownloadStream(_id);
		String fileName = file.getFilename();
		String ext = getExtensionName(fileName).toLowerCase();
		ProcessResult result = null;
		// �����office�ļ�
		if (isOfficeFile(ext)) {
			result = runOffice(pT, os, file, context);
		} else if (isDWGFile(ext)) {
			// �����DWG�ļ�
			result = runDWG(pT, os, file, context);
		} else if(isImageFile(ext)){
			result = runGenericConvertor(pT, os, file, context, DEFAULT_GENERIC);
		} else {
			result = runGenericConvertor(pT, os, file, context, DEFAULT_GENERIC);
//			updateResult(_id, db, colName, null, "��֧�ִ�����Դ�ļ��Ŀ��ӻ�");
//			result = new ProcessResult();
//			result.put("result", "��֧�ִ�����Դ�ļ��Ŀ��ӻ�");
//			return result;
		}

		InputStream pdfs = result.getInputStream("file");
		if (pdfs == null) {
			updateResult(_id, db, colName, null, "ת�����ӻ��ļ�ʧ��");
			result = new ProcessResult();
			result.put("result", "ת�����ӻ��ļ�ʧ��");
			return result;
		}
		return handleTransferedFile(pT, file, pdfs);
	}

	protected abstract ProcessResult handleTransferedFile(ProcessTask pT, GridFSFile file, InputStream pdfs) throws IOException;

	protected abstract void updateResult(ObjectId _id, MongoDatabase db, String colName, ObjectId v_id, String msg);

	private ProcessResult runDWG(ProcessTask pT, GridFSDownloadStream os, GridFSFile file, IProcessContext context)
			throws Exception {
		String srcFilename = file.getFilename();
		String sourceType = getExtensionName(srcFilename).toLowerCase();
		String targetType = "pdf";

		ProcessTask subTask = new ProcessTask();
		subTask.setName(pT.getName() + ", ת��");
		subTask.setParentId(pT.getId());
		subTask.setPriority(10);
		subTask.put("sourceType", sourceType);
		subTask.put("targetType", targetType);
		subTask.put("file", os);
		// ���Ӳ���

		subTask.put("autoZoomExtend", Optional.ofNullable(pT.get("autoZoomExtend")).orElse(Boolean.TRUE));
		subTask.put("rasterPixel", Optional.ofNullable(pT.get("rasterPixel")).orElse(1));
		subTask.put("backgroundColor", Optional.ofNullable(pT.get("backgroundColor")).orElse(7));
		subTask.put("lineWeight", Optional.ofNullable(pT.get("lineWeight")).orElse(1));
		subTask.put("autoSize", Optional.ofNullable(pT.get("autoSize")).orElse(Boolean.TRUE));
		subTask.put("rasterWidth", pT.get("rasterWidth"));
		subTask.put("rasterHeight", pT.get("rasterHeight"));
		subTask.put("layoutPaperSize", pT.get("layoutPaperSize"));
		subTask.put("maskRaster", pT.get("maskRaster"));
		subTask.put("presetWatermark", pT.get("presetWatermark"));
		subTask.put("presetWatermarkFile", pT.get("presetWatermarkFile"));
		subTask.put("dpi", pT.get("dpi"));
		subTask.put("jpegQuality", pT.get("jpegQuality"));
		subTask.put("penSetName", pT.get("penSetName"));
		subTask.put("lineRemoval", pT.get("lineRemoval"));
		subTask.put("scale", pT.get("scale"));
		subTask.put("margin", pT.get("margin"));

		// ����ת��������
		String processorTypeId = (String) pT.get("dwgConvertor");
		if (processorTypeId == null || processorTypeId.isEmpty())
			processorTypeId = DEFAULT_DWG;
		return context.runTask(subTask, processorTypeId);

		// task.setValue("autoZoomExtend", Boolean.TRUE);
		// task.setValue("rasterPixel", 1);
		// task.setValue("backgroundColor", 7);
		// task.setValue("lineWeight", 1);
		// task.setValue("autoSize", Boolean.TRUE);
		// task.setFileValue("file", new File("D:/officetest/TEMPLATE
		// _A1.dwg"));
		//
		// File file = new File("D:/officetest/TEMPLATE _A1.pdf");
		// Result result = manager.runTask(task, type[2]);
		// result.writeToFile("file", file);

	}

	private ProcessResult runOffice(ProcessTask pT, GridFSDownloadStream inputstream, GridFSFile file,
			IProcessContext context) throws Exception {
		// ����ת��������
		String processorTypeId = (String) pT.get("officeConvertor");
		if (processorTypeId == null || processorTypeId.isEmpty())
			processorTypeId = DEFAULT_GENERIC;
		
		return runGenericConvertor(pT, inputstream, file, context, processorTypeId);
	}

	private ProcessResult runGenericConvertor(ProcessTask pT, GridFSDownloadStream inputstream, GridFSFile file,
			IProcessContext context, String processorTypeId) throws Exception {
		// �����ļ���
		String srcFilename = file.getFilename();
		String sourceType = getExtensionName(srcFilename).toLowerCase();
		String targetType = "pdf";

		ProcessTask subTask = new ProcessTask();
		subTask.setName(pT.getName() + ", ת��");
		subTask.setParentId(pT.getId());
		subTask.setPriority(10);
		subTask.put("file", inputstream);
		subTask.put("sourceType", sourceType);
		subTask.put("targetType", targetType);

		return context.runTask(subTask, processorTypeId);
	}

	private boolean isOfficeFile(String ext) {
		return Arrays.asList("doc", "docx", "rtf", "txt", "csv", "xls", "xlsx", "ppt", "pptx", "html","htm").contains(ext);
	}

	private boolean isDWGFile(String ext) {
		return Arrays.asList("dwg", "dxf", "dwf").contains(ext);
	}
	
	private boolean isImageFile(String ext) {
		return Arrays.asList("jpg", "gif", "png","bpm","jpeg","svg").contains(ext);
	}

	private static String getExtensionName(String filename) {
		int idx = filename.lastIndexOf('.');
		if ((idx > -1) && (idx < (filename.length() - 1))) {
			return filename.substring(idx + 1);
		}
		return filename;
	}

}
