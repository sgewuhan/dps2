package com.bizvpm.dps.processor.tmtsap;

import com.bizvpm.dps.processor.tmtsap.etl.ICostCollectorFactory;

public class SAPCostCollectorFactory implements ICostCollectorFactory {

	@Override
	public ICostCollector createCostCollector() {
		return new JCO_ZXFUN_PM_YFFY();
	}

}
