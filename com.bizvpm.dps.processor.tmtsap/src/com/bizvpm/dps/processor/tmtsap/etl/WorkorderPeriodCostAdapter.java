package com.bizvpm.dps.processor.tmtsap.etl;

import java.util.List;

import org.bson.Document;

public class WorkorderPeriodCostAdapter extends BasicPeriodCostAdapter {

	public WorkorderPeriodCostAdapter(ICostCollectorFactory ccf, String domain) {
		super(ccf, domain);
	}

	public List<Document> runGetData(List<String> workOrderArray, List<String> costElementArray, int year, int month)
			throws Exception {
		return runGetData(null, workOrderArray, costElementArray, year, month, "workordercost");
	}

}
