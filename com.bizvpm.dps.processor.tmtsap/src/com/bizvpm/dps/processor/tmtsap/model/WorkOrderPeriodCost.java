package com.bizvpm.dps.processor.tmtsap.model;

import org.bson.Document;

/**
 * 直接记录在工作令号下的研发成本
 * 
 * @author Administrator
 *
 */
public class WorkOrderPeriodCost implements IAccountPeriod {

	public static final String F_YEAR = "year";

	public static final String F_MONTH = "month";

	public static final String F_COSTCENTERCODE = "costcenter";

	public static final String F_WORKORDER = "workorder";

	public static final String F__CDATE = "_cdate";

	private Document data;

	public WorkOrderPeriodCost(Document data) {
		this.data = data;
	}

	@Override
	public Double getAccountValue(String accountNumber) {
		return data.getDouble(accountNumber);
	}

	public String getCostCode() {
		return data.getString(F_COSTCENTERCODE);
	}

	public String getLabel() {
		String label = data.getString("desc");
		String costCode = getCostCode();
		if (label == null) {
			return costCode;
		} else {
			return costCode + label;
		}
	}

}