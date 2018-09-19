
package com.bizvpm.dps.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for persistableProcessor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="persistableProcessor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="managerUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parameterList" type="{http://service.dps.bizvpm.com/}parameterList" minOccurs="0"/>
 *         &lt;element name="descritpion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistableProcessor", propOrder = {
    "id",
    "managerUrl",
    "name",
    "parameterList",
    "descritpion",
    "online"
})
public class PersistableProcessor {

    protected String id;
    protected String managerUrl;
    protected String name;
    protected ParameterList parameterList;
    protected String descritpion;
    protected boolean online;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the managerUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManagerUrl() {
        return managerUrl;
    }

    /**
     * Sets the value of the managerUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManagerUrl(String value) {
        this.managerUrl = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the parameterList property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterList }
     *     
     */
    public ParameterList getParameterList() {
        return parameterList;
    }

    /**
     * Sets the value of the parameterList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterList }
     *     
     */
    public void setParameterList(ParameterList value) {
        this.parameterList = value;
    }

    /**
     * Gets the value of the descritpion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescritpion() {
        return descritpion;
    }

    /**
     * Sets the value of the descritpion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescritpion(String value) {
        this.descritpion = value;
    }
    
    public boolean isOnline() {
		return online;
	}
    
    public void setOnline(boolean online) {
		this.online = online;
	}

}
