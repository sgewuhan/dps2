
package com.tmt.dps.processor.sms.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UserPsw" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AccountType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AccountInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SchTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SMSMobile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SMSContent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "userName",
    "userPsw",
    "accountType",
    "accountInfo",
    "schTime",
    "smsMobile",
    "smsContent"
})
@XmlRootElement(name = "SendSMS")
public class SendSMS {

    @XmlElement(name = "UserName")
    protected String userName;
    @XmlElement(name = "UserPsw")
    protected String userPsw;
    @XmlElement(name = "AccountType")
    protected String accountType;
    @XmlElement(name = "AccountInfo")
    protected String accountInfo;
    @XmlElement(name = "SchTime")
    protected String schTime;
    @XmlElement(name = "SMSMobile")
    protected String smsMobile;
    @XmlElement(name = "SMSContent")
    protected String smsContent;

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the userPsw property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserPsw() {
        return userPsw;
    }

    /**
     * Sets the value of the userPsw property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserPsw(String value) {
        this.userPsw = value;
    }

    /**
     * Gets the value of the accountType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * Sets the value of the accountType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountType(String value) {
        this.accountType = value;
    }

    /**
     * Gets the value of the accountInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountInfo() {
        return accountInfo;
    }

    /**
     * Sets the value of the accountInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountInfo(String value) {
        this.accountInfo = value;
    }

    /**
     * Gets the value of the schTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchTime() {
        return schTime;
    }

    /**
     * Sets the value of the schTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchTime(String value) {
        this.schTime = value;
    }

    /**
     * Gets the value of the smsMobile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSMSMobile() {
        return smsMobile;
    }

    /**
     * Sets the value of the smsMobile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSMSMobile(String value) {
        this.smsMobile = value;
    }

    /**
     * Gets the value of the smsContent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSMSContent() {
        return smsContent;
    }

    /**
     * Sets the value of the smsContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSMSContent(String value) {
        this.smsContent = value;
    }

}
