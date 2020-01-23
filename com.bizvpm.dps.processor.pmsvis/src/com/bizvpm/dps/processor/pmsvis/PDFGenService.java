package com.bizvpm.dps.processor.pmsvis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;

import org.bson.types.ObjectId;

import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.sun.xml.internal.ws.util.ByteArrayDataSource;

public class PDFGenService extends AbstractVisualService {

	@Override
	protected ProcessResult handleTransferedFile(ProcessTask pT, GridFSFile file, InputStream pdfs) throws IOException {
		ProcessResult result = new ProcessResult();
		result.put("file", new DataHandler(new ByteArrayDataSource(toByteArray(pdfs), "application/octet-stream")));
		pdfs.close();
		return result;
	}

	@Override
	protected void updateResult(ObjectId _id, MongoDatabase db, String colName, ObjectId v_id, String msg) {
	}
	
	public static byte[] toByteArray(InputStream input) throws IOException {
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024*4];
	    int n = 0;
	    while (-1 != (n = input.read(buffer))) {
	        output.write(buffer, 0, n);
	    }
	    return output.toByteArray();
	}


}
