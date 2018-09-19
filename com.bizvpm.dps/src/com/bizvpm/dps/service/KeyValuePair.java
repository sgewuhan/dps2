
package com.bizvpm.dps.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


@XmlAccessorType(XmlAccessType.FIELD)
public class KeyValuePair {

	private String key;

	private DataObject value;
    
    public String getKey() {
        return key;
    }

    public void setKey(String value) {
        this.key = value;
    }

    public DataObject getValue() {
        return value;
    }

    public void setValue(DataObject value) {
        this.value = value;
    }

}
