package com.bizvpm.dps.runtime;

public interface IProcessorActivator {

	void startCheck() throws Exception;

	void start() throws Exception;

	void stop() throws Exception;

}
