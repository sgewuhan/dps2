package com.bizvpm.dps.processor.tmtsap.etl;

import com.bizvpm.dps.processor.tmtsap.ICostCollector;

public interface ICostCollectorFactory {

	public ICostCollector createCostCollector();
}
