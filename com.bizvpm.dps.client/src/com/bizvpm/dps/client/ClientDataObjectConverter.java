package com.bizvpm.dps.client;

import java.io.File;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import com.sun.xml.internal.ws.util.ByteArrayDataSource;

public class ClientDataObjectConverter extends DataObjectConverter {

	@Override
	public DataObject getDataObject(Object value) {
		if (value instanceof File) {
			DataObject data = new DataObject();
			setDataHandlerValue(data, new DataHandler(new FileDataSource((File) value)));
			return data;
		} else if (value instanceof InputStream) {
			DataObject data = new DataObject();
			byte[] bytes = getBytes((InputStream) value);
			setDataHandlerValue(data, new DataHandler(new ByteArrayDataSource(bytes, "application/octet-stream")));
			return data;
		}
		return super.getDataObject(value);
	}

}
