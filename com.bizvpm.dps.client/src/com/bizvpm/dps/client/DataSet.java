
package com.bizvpm.dps.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for dataSet complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="dataSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="values" type="{http://service.dps.bizvpm.com/}keyValuePair" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataSet", propOrder = { "values" })
@XmlSeeAlso({ Result.class, Task.class })
public class DataSet {

	@XmlElement(nillable = true)
	protected List<KeyValuePair> values;
	
	
	/**
	 * Gets the value of the values property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the values property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getValues().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link KeyValuePair }
	 * 
	 * 
	 */
	public List<KeyValuePair> getValues() {
		if (values == null) {
			values = new ArrayList<KeyValuePair>();
		}
		return this.values;
	}

	public void setValue(String key, Object value) {
		DataObjectConverter converter = new ClientDataObjectConverter();
		values = getValues();
		for (int i = 0; i < values.size(); i++) {
			KeyValuePair keyValuePair = values.get(i);
			if (keyValuePair.getKey().equals(key)) {
				DataObject data = converter.getDataObject(value);
				keyValuePair.setValue(data);
				return;
			}
		}
		KeyValuePair kv = new KeyValuePair();
		kv.setKey(key);
		DataObject data = converter.getDataObject(value);
		kv.setValue(data);
		values.add(kv);
	}

	public Object getValue(String key) {
		values = getValues();
		for (int i = 0; i < values.size(); i++) {
			KeyValuePair keyValuePair = values.get(i);
			if (keyValuePair.getKey().equals(key)) {
				return keyValuePair.getValue();
			}
		}
		return null;
	}

	public void setFileValue(String key, File file) {
		DataHandler data = new DataHandler(new FileDataSource(file));
		setValue(key, data);
	}

	public InputStream getInputStream(String key) throws IOException {
		values = getValues();
		DataObjectConverter convert = new ClientDataObjectConverter();
		for (int i = 0; i < values.size(); i++) {
			KeyValuePair keyValuePair = values.get(i);
			if (keyValuePair.getKey().equals(key)) {
				DataObject data = keyValuePair.getValue();
				Object value = convert.getValue(data);
				if (value instanceof DataHandler) {
					return ((DataHandler) value).getInputStream();
				} else {
					return null;
				}
			}
		}
		return null;
	}

	public void writeToFile(String parameter, File file) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = getInputStream(parameter);
			os = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
			}
		} catch (IOException e) {
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
}
