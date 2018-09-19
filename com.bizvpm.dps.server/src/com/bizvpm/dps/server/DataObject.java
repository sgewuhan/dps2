
package com.bizvpm.dps.server;

import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataObject", propOrder = {
    "intValue",
    "longValue",
    "doubleValue",
    "floatValue",
    "booleanValue",
    "stringValue",
    "dataHandlerValue",
    "dataName",
    "listValue",
    "dateValue",
    "mapValue"
})
public class DataObject {

    protected Integer intValue;
    protected Long longValue;
    protected Double doubleValue;
    protected Float floatValue;
    protected Boolean booleanValue;
    protected String stringValue;
    @XmlMimeType("application/octet-stream")
    protected DataHandler dataHandlerValue;
    protected String dataName;
    @XmlElement(nillable = true)
    protected List<DataObject> listValue;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateValue;
    protected DataSet mapValue;
    /**
     * Gets the value of the intValue property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIntValue() {
        return intValue;
    }

    /**
     * Sets the value of the intValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIntValue(Integer value) {
        this.intValue = value;
    }

    /**
     * Gets the value of the longValue property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLongValue() {
        return longValue;
    }

    /**
     * Sets the value of the longValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLongValue(Long value) {
        this.longValue = value;
    }

    /**
     * Gets the value of the doubleValue property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getDoubleValue() {
        return doubleValue;
    }

    /**
     * Sets the value of the doubleValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setDoubleValue(Double value) {
        this.doubleValue = value;
    }

    /**
     * Gets the value of the floatValue property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getFloatValue() {
        return floatValue;
    }

    /**
     * Sets the value of the floatValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setFloatValue(Float value) {
        this.floatValue = value;
    }

    /**
     * Gets the value of the booleanValue property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBooleanValue() {
        return booleanValue;
    }

    /**
     * Sets the value of the booleanValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBooleanValue(Boolean value) {
        this.booleanValue = value;
    }

    /**
     * Gets the value of the stringValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * Sets the value of the stringValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStringValue(String value) {
        this.stringValue = value;
    }

    /**
     * Gets the value of the dataHandlerValue property.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getDataHandlerValue() {
        return dataHandlerValue;
    }

    /**
     * Sets the value of the dataHandlerValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setDataHandlerValue(DataHandler value) {
        this.dataHandlerValue = value;
    }

    /**
     * Gets the value of the listValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataObject }
     * 
     * 
     */
    public List<DataObject> getListValue() {
        if (listValue == null) {
            listValue = new ArrayList<DataObject>();
        }
        return this.listValue;
    }

    /**
     * Gets the value of the dateValue property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateValue() {
        return dateValue;
    }

    /**
     * Sets the value of the dateValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateValue(XMLGregorianCalendar value) {
        this.dateValue = value;
    }
    
    public DataSet getMapValue() {
		return mapValue;
	}
    
    public void setMapValue(DataSet mapValue) {
		this.mapValue = mapValue;
	}
    
    public String getDataName() {
		return dataName;
	}
    
    public void setDataName(String dataName) {
		this.dataName = dataName;
	}
    

}
