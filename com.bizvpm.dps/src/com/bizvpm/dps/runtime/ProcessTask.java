package com.bizvpm.dps.runtime;

public class ProcessTask  extends ProcessDataSet{

	protected String name;

	protected int priority;
	
	protected String id;
	
	protected String parentId;

	public String getName() {
		return name;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	public String getParentId() {
		return parentId;
	}
	
}
