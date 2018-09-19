package com.bizvpm.dps.server;

import java.util.List;

public class PersistableProcessorManager  {

	private String host;
	
	private List<PersistableProcessor> processorConfigs;
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<PersistableProcessor> getProcessorConfigs() {
		return processorConfigs;
	}

	public void setProcessorConfigs(List<PersistableProcessor> processorConfigs) {
		this.processorConfigs = processorConfigs;
	}
	
	

}
