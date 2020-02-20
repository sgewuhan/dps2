package com.bizvpm.dps.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import com.bizvpm.dps.service.IDataSetAdaptable;
import com.sun.xml.internal.ws.util.ByteArrayDataSource;

public class ProcessDataSet implements IDataSetAdaptable {

	Map<String, Object> values;

	public Object get(String parameter) {
		return values.get(parameter);
	}

	public Object put(String parameter, Object value) {
		if (values == null) {
			values = new HashMap<String, Object>();
		}
		return values.put(parameter, value);
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void putFile(String parameter, File file) {
		put(parameter, new DataHandler(new FileDataSource(file)));
	}

	public void putByteArray(String parameter, File file) throws Exception {
		byte[] byteArray = getBytes(file);
		put(parameter, new DataHandler(new ByteArrayDataSource(byteArray, "application/octet-stream")));
	}

	public void putByteArray(String parameter, byte[] byteArray, String mime) throws Exception {
		put(parameter, new DataHandler(new ByteArrayDataSource(byteArray, mime)));
	}

	private byte[] getBytes(File file) throws Exception {

		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		return getBytes(is, length);

	}

	private byte[] getBytes(InputStream is, long length) throws Exception {
		/*
		 * You cannot create an array using a long type. It needs to be an int
		 * type. Before converting to an int type, check to ensure that file is
		 * not loarger than Integer.MAX_VALUE;
		 */
		if (length > Integer.MAX_VALUE) {
			is.close();
			return null;
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while ((offset < bytes.length) && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {

			offset += numRead;

		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			is.close();
			throw new Exception("Could not completely read file ");
		}

		is.close();
		return bytes;
	}

	public void writeToFile(String parameter, File file) throws Exception {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = getInputStream(parameter);
			if (is == null) {
				throw new Exception(
						"Process dataset doesn't contains inputstream value, check parameter: " + parameter);
			}
			os = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (os != null) {
				os.close();
			}
			if (is != null) {
				is.close();
			}
		}
	}

	public InputStream getInputStream(String parameter) throws Exception {
		Object data = get(parameter);
		if (data instanceof DataHandler) {
			DataSource ds = ((DataHandler) data).getDataSource();
			return ds.getInputStream();
		}
		return null;
	}

	public OutputStream getOutputStream(String parameter) throws Exception {
		Object data = get(parameter);
		if (data instanceof DataHandler) {
			DataSource ds = ((DataHandler) data).getDataSource();
			return ds.getOutputStream();
		}
		return null;
	}

	public String getContentType(String parameter) {
		Object data = get(parameter);
		if (data instanceof DataHandler) {
			DataSource ds = ((DataHandler) data).getDataSource();
			return ds.getContentType();
		}
		return null;
	}

	public String getDataSourceName(String parameter) {
		Object data = get(parameter);
		if (data instanceof DataHandler) {
			DataSource ds = ((DataHandler) data).getDataSource();
			return ds.getName();
		}
		return null;
	}

}
