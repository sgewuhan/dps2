
package com.bizvpm.dps.server;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistableProcessor", propOrder = { "id", "managerUrl", "name", "parameterList", "descritpion","online" })
public class PersistableProcessor {

	protected String id;
	protected String managerUrl;
	protected String name;
	protected ParameterList parameterList;
	protected String descritpion;
	protected boolean online;

	public String getId() {
		return id;
	}

	public void setId(String value) {
		this.id = value;
	}

	public String getManagerUrl() {
		return managerUrl;
	}

	public void setManagerUrl(String value) {
		this.managerUrl = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public ParameterList getParameterList() {
		return parameterList;
	}

	public void setParamterList(ParameterList parameterList) {
		this.parameterList = parameterList;
	}

	public String getDescritpion() {
		return descritpion;
	}

	public void setDescritpion(String descritpion) {
		this.descritpion = descritpion;
	}
	
	public boolean isOnline() {
		return online;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}

}
