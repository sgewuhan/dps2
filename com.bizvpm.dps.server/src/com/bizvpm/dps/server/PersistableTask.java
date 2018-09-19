package com.bizvpm.dps.server;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class PersistableTask extends Task {
	
	private String processorTypeId;
	
	private String status;

	private String message;

	private Date receivedDate;
	
	private Date doneDate;
	
	private Date errorDate;
	
	@XmlElement(nillable = true)
    protected List<KeyValuePair> result;
	
	public void setProcessorTypeId(String processorTypeId) {
		this.processorTypeId = processorTypeId;
	}
	
	public String getProcessorTypeId() {
		return processorTypeId;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}

	public Date getErrorDate() {
		return errorDate;
	}

	public void setErrorDate(Date errorDate) {
		this.errorDate = errorDate;
	}

	public List<KeyValuePair> getResult() {
		return result;
	}
	
	public void setResult(List<KeyValuePair> result) {
		this.result = result;
	}
	
}
