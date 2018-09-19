package com.bizvpm.dps.server;

import java.util.List;

public class Parameter {

	private String name;
	private String type;
	private boolean optional;
	private String description;
	private List<Object> restrictions;

	public Parameter() {
	}


	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isOptional() {
		return optional;
	}

	public List<Object> getRestrictions() {
		return restrictions;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public void setRestrictions(List<Object> restrictions) {
		this.restrictions = restrictions;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

}
