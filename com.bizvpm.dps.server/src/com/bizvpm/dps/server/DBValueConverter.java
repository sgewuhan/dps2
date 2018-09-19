package com.bizvpm.dps.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.sun.xml.internal.ws.util.ByteArrayDataSource;

public class DBValueConverter extends DataObjectConverter implements IPersistenceConstants {

	private DB db;

	public DBValueConverter(DB db) {
		this.db = db;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getValue(DataObject data) {
		Object value = super.getValue(data);
		if (value instanceof DataHandler) {
			InputStream is = null;
			try {
				is = ((DataHandler) value).getInputStream();
				String name = ((DataHandler) value).getName();
				if (name == null || name.isEmpty()) {
					name = "file";
				}
				value = upload(is, name);
			} catch (IOException e) {
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		} else if (value instanceof Map) {
			BasicDBObject result = new BasicDBObject();
			result.putAll((Map) value);
			value = result;
		}
		return value;
	}

	@Override
	public DataObject getDataObject(Object value) {
		if (value instanceof BasicDBObject) {
			if (((BasicDBObject) value).containsField("type")) {
				DataObject d = new DataObject();
				DataSet ds = new DataSet();
				Iterator<String> iter = ((BasicDBObject) value).keySet().iterator();
				while (iter.hasNext()) {
					String itemKey = iter.next();
					Object itemValue = ((BasicDBObject) value).get(itemKey);
					DataObject itemData = getDataObject(itemValue);
					KeyValuePair kv = new KeyValuePair();
					kv.setKey(itemKey);
					kv.setValue(itemData);
					ds.getValues().add(kv);
				}
				d.setMapValue(ds);
			} else {
				Object type = ((BasicDBObject) value).get("type");
				if ("file".equals(type)) {
					ObjectId _id = (ObjectId) ((DBObject) value).get("_id");
					byte[] byteArray = getBytes(_id);
					if (byteArray != null) {
						DataHandler dh = new DataHandler(
								new ByteArrayDataSource(byteArray, "application/octet-stream"));
						DataObject d = new DataObject();
						d.setDataHandlerValue(dh);
						String name = (String) ((BasicDBObject) value).get("filename");
						if (name == null || name.isEmpty()) {
							name = "filename";
						}
						d.setDataName(name);
						return d;
					}
				} else {
				}
			}
		}
		return super.getDataObject(value);
	}

	private byte[] getBytes(ObjectId _id) {
		GridFS gridfs = new GridFS(db, GRID_FS_TASK_FILE);
		GridFSDBFile result = gridfs.find(_id);
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			result.writeTo(out);
			byte[] arr = out.toByteArray();
			return arr;
		} catch (IOException e) {
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}

		return null;
	}

	private BasicDBObject upload(InputStream in, String fileName) {
		ObjectId _id = new ObjectId();
		GridFS gridfs = new GridFS(db, GRID_FS_TASK_FILE);
		GridFSInputFile file = gridfs.createFile(in, true);
		file.put("_id", _id); //$NON-NLS-1$
		file.setFilename(fileName);
		file.save();
		return new BasicDBObject().append("type", "file").append("_id", _id).append("db", db.getName())
				.append("col", GRID_FS_TASK_FILE).append("filename", fileName);
	}

}
