package com.bizvpm.dps.processor.tmtsap.etl;

import com.mongodb.DBObject;

public class WorkorderPeriodCostAdapter extends BasicPeriodCostAdapter {

	public WorkorderPeriodCostAdapter(ICostCollectorFactory ccf) {
		super(ccf);
	}

	public DBObject[] runGetData(String[] workOrderArray, String[] costElementArray, int year, int month)
			throws Exception {
		DBObject[] result = runGetData(null, workOrderArray, costElementArray, year, month, "workordercost");

		return result;
	}

}
