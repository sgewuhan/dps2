package com.bizvpm.dps.service;

public class ProcessCounter {

	private int maxCount;
	private int used;

	public ProcessCounter(int maxCount) {
		this.maxCount = maxCount;
		used = 0;
	}

	synchronized public void add(int i) {
		used+=i;
	}
	
	public boolean isFree(){
		return maxCount>used;
	}
	
	public int getUsed() {
		return used;
	}
	

}
