package com.bizvpm.dps.service;

import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataObject {
	
	private Integer intValue;
	
	private Long longValue;
	
	private Double doubleValue;
	
	private Float floatValue;
	
	private Boolean booleanValue;
	
	private String stringValue;
	
	@XmlMimeType("application/octet-stream")  
    private DataHandler dataHandlerValue;
	
	private String dataName;
	
	private List<DataObject> listValue;

	private Date dateValue;

	private DataSet mapValue;

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(Float floatValue) {
		this.floatValue = floatValue;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public DataHandler getDataHandlerValue() {
		return dataHandlerValue;
	}

	public void setDataHandlerValue(DataHandler dataHandlerValue) {
		this.dataHandlerValue = dataHandlerValue;
	}

	public List<DataObject> getListValue() {
		return listValue;
	}

	public void setListValue(List<DataObject> listValue) {
		this.listValue = listValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public void setMapValue(DataSet mapValue) {
		this.mapValue = mapValue;
	}
	
	public DataSet getMapValue() {
		return mapValue;
	}
	
	public String getDataName() {
		return dataName;
	}
	
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	
}
