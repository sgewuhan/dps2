package com.bizvpm.dps.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.xml.internal.ws.util.ByteArrayDataSource;

public class DataObjectConverter {

	public Object getValue(DataObject data) {
		if (data == null) {
			return null;
		}

		Object value = data.isBooleanValue();
		if (value != null) {
			return value;
		}

		value = data.getDataHandlerValue();
		if (value != null) {
			return value;
		}

		value = data.getDateValue();
		if (value instanceof XMLGregorianCalendar) {
			return convertToDate((XMLGregorianCalendar) value);
		}

		value = data.getDoubleValue();
		if (value != null) {
			return value;
		}

		value = data.getFloatValue();
		if (value != null) {
			return value;
		}

		value = data.getIntValue();
		if (value != null) {
			return value;
		}

		value = data.getListValue();
		if (value != null) {
			List<?> list = (List<?>) value;
			if (!list.isEmpty()) {
				List<Object> result = new ArrayList<Object>();
				for (int i = 0; i < list.size(); i++) {
					Object item = list.get(i);
					if (item instanceof DataObject) {
						result.add(getValue((DataObject) item));
					}
				}
				return result;
			}
		}
		
		value = data.getMapValue();
		if(value!=null){
			DataSet map = (DataSet)value;
			List<KeyValuePair> mapItems = map.getValues();
			if(!mapItems.isEmpty()){
				Map<String,Object> result = new HashMap<String,Object>();
				for (int i = 0; i < mapItems.size(); i++) {
					KeyValuePair mapItem = mapItems.get(i);
					String itemKey = mapItem.getKey();
					DataObject itemValue = mapItem.getValue();
					Object mapItemValue = getValue(itemValue);
					result.put(itemKey, mapItemValue);
				}
				return result;
			}
		}

		value = data.getLongValue();
		if (value != null) {
			return value;
		}

		value = data.getStringValue();
		if (value != null) {
			return value;
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public DataObject getDataObject(Object value) {
		DataObject data = new DataObject();
		if (value instanceof String) {
			setStringValue(data, (String) value);
		} else if (value instanceof Float) {
			setFloatValue(data, (float) value);
		} else if (value instanceof Double) {
			setDoubleValue(data, (double) value);
		} else if (value instanceof Boolean) {
			setBooleanValue(data, (boolean) value);
		} else if (value instanceof Date) {
			setDateValue(data, (Date) value);
		} else if (value instanceof Integer) {
			setIntValue(data, (int) value);
		} else if (value instanceof Long) {
			setLongValue(data, (Long) value);
		} else if (value instanceof DataHandler) {
			setDataHandlerValue(data, (DataHandler) value);
		} else if (value instanceof List) {
			List<?> list = (List<?>) value;
			List<DataObject> dataList = new ArrayList<DataObject>();
			for (int i = 0; i < list.size(); i++) {
				Object itemValue = list.get(i);
				DataObject itemData = getDataObject(itemValue);
				if (itemData != null) {
					dataList.add(itemData);
				}
			}
			setListValue(data, dataList);
		} else if (value instanceof DataSet) {
			setMapValue(data, (DataSet) value);
		} else if (value instanceof Map) {
			DataSet ds = new DataSet();
			Iterator iter = ((Map) value).keySet().iterator();
			while(iter.hasNext()){
				Object next = iter.next();
				String key = next.toString();
				Object itemValue = ((Map) value).get(next);
				DataObject dataObject = getDataObject(itemValue);
				KeyValuePair kv = new KeyValuePair();
				kv.setKey(key);
				kv.setValue(dataObject);
				ds.getValues().add(kv);
			}
			setMapValue(data, ds);
		} else if (value instanceof File) {
			setDataHandlerValue(data, new DataHandler(new FileDataSource((File) value)));
		} else if (value instanceof InputStream) {
			byte[] bytes = getBytes((InputStream) value);
			setDataHandlerValue(data, new DataHandler(new ByteArrayDataSource(bytes, "application/octet-stream")));
		} else {
			return null;
		}
		return data;
	}

	protected byte[] getBytes(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len = 0;
		byte[] b = new byte[1024];
		try {
			while ((len = is.read(b, 0, b.length)) != -1) {
				baos.write(b, 0, len);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				baos.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	protected void setStringValue(DataObject data, String value) {
		data.setBooleanValue(null);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		data.setDateValue(null);
		data.setDoubleValue(null);
		data.setFloatValue(null);
		data.setIntValue(null);
		data.getListValue().clear();
		data.setLongValue(null);
		data.setStringValue(value);
		data.setMapValue(null);
	}

	protected void setFloatValue(DataObject data, float value) {
		data.setBooleanValue(null);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		data.setDateValue(null);
		data.setDoubleValue(null);
		data.setFloatValue(value);
		data.setIntValue(null);
		data.getListValue().clear();
		data.setLongValue(null);
		data.setStringValue(null);
		data.setMapValue(null);
	}

	protected void setDoubleValue(DataObject data, double value) {
		data.setBooleanValue(null);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		data.setDateValue(null);
		data.setDoubleValue(value);
		data.setFloatValue(null);
		data.setIntValue(null);
		data.getListValue().clear();
		data.setLongValue(null);
		data.setStringValue(null);
		data.setMapValue(null);
	}

	protected void setBooleanValue(DataObject data, boolean value) {
		data.setBooleanValue(value);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		data.setDateValue(null);
		data.setDoubleValue(null);
		data.setFloatValue(null);
		data.setIntValue(null);
		data.getListValue().clear();
		data.setLongValue(null);
		data.setStringValue(null);
		data.setMapValue(null);
	}

	protected void setDateValue(DataObject data, Date value) {
		data.setBooleanValue(null);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		XMLGregorianCalendar date = convertToXMLGregorianCalendar(value);
		data.setDateValue(date);
		data.setDoubleValue(null);
		data.setFloatValue(null);
		data.setIntValue(null);
		data.getListValue().clear();
		data.setLongValue(null);
		data.setStringValue(null);
		data.setMapValue(null);
	}

	protected void setIntValue(DataObject data, int value) {
		data.setBooleanValue(null);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		data.setDateValue(null);
		data.setDoubleValue(null);
		data.setFloatValue(null);
		data.setIntValue(value);
		data.getListValue().clear();
		data.setLongValue(null);
		data.setStringValue(null);
		data.setMapValue(null);
	}

	protected void setLongValue(DataObject data, Long value) {
		data.setBooleanValue(null);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		data.setDateValue(null);
		data.setDoubleValue(null);
		data.setFloatValue(null);
		data.setIntValue(null);
		data.getListValue().clear();
		data.setLongValue(value);
		data.setStringValue(null);
		data.setMapValue(null);
	}

	protected void setDataHandlerValue(DataObject data, DataHandler value) {
		data.setBooleanValue(null);
		data.setDataHandlerValue(value);
		data.setDataName(value.getName());
		data.setDateValue(null);
		data.setDoubleValue(null);
		data.setFloatValue(null);
		data.setIntValue(null);
		data.getListValue().clear();
		data.setLongValue(null);
		data.setStringValue(null);
		data.setMapValue(null);
	}

	protected void setListValue(DataObject data, List<DataObject> dataList) {
		data.setBooleanValue(null);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		data.setDateValue(null);
		data.setDoubleValue(null);
		data.setFloatValue(null);
		data.setIntValue(null);
		data.getListValue().clear();
		data.getListValue().addAll(dataList);
		data.setLongValue(null);
		data.setStringValue(null);
		data.setMapValue(null);
	}
	
	protected void setMapValue(DataObject data, DataSet map){
		data.setBooleanValue(null);
		data.setDataHandlerValue(null);
		data.setDataName(null);
		data.setDateValue(null);
		data.setDoubleValue(null);
		data.setFloatValue(null);
		data.setIntValue(null);
		data.getListValue().clear();
		data.setLongValue(null);
		data.setStringValue(null);
		data.setMapValue(map);
	}

	protected XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		XMLGregorianCalendar gc = null;
		try {
			gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return gc;
	}

	protected Date convertToDate(XMLGregorianCalendar cal) {
		GregorianCalendar ca = cal.toGregorianCalendar();
		return ca.getTime();
	}

	protected byte[] getBytes(File file) throws Exception {

		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		return getBytes(is, length);

	}

	protected byte[] getBytes(InputStream is, long length) throws Exception {
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

}