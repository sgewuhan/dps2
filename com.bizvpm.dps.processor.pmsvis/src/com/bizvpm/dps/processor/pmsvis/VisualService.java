package com.bizvpm.dps.processor.pmsvis;

import java.io.IOException;
import java.io.InputStream;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvpm.dps.processor.mongodbds.Domain;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

public class VisualService extends AbstractVisualService {

	@Override
	protected void updateResult(ObjectId _id, MongoDatabase db, String colName, ObjectId v_id, String msg) {
		db.getCollection(colName + ".files").updateOne(new Document("_id", _id),
				new Document("$set", new Document("metadata.preview", v_id).append("metadata.previewMsg", msg)));
	}

	@SuppressWarnings("deprecation")
	@Override
	protected ProcessResult handleTransferedFile(ProcessTask pT, GridFSFile file, InputStream pdfs) throws IOException {
		ObjectId _id = new ObjectId((String) pT.get("_id"));
		MongoDatabase db = Domain.getDatabase((String) pT.get("domain"));
		String colName = (String) pT.get("col");
		String fileName = file.getFilename();

		ProcessResult result;
		// 保存数据
		GridFSUploadOptions option = new GridFSUploadOptions();
		option.metadata(new Document("contentType", "application/pdf").append("masterfilemd5", file.getMD5()));
		String tgtFileName = getPDFFileName(fileName);
		ObjectId v_id = GridFSBuckets.create(db, (String) pT.get("target")).uploadFromStream(tgtFileName, pdfs, option);
		pdfs.close();
		// 如果生成成功
		if (v_id != null) {
			updateResult(_id, db, colName, v_id, null);
			result = new ProcessResult();
			result.put("result", "ok");
			return result;
		} else {
			updateResult(_id, db, colName, v_id, "保存可视化文件失败");
			result = new ProcessResult();
			result.put("result", "保存可视化文件失败");
			return result;
		}
	}

	private String getPDFFileName(String filename) {
		int idx = filename.lastIndexOf('.');
		if ((idx > -1) && (idx < (filename.length()))) {
			return filename.substring(0, idx) + ".pdf";
		}
		return filename + ".pdf";
	}

}
