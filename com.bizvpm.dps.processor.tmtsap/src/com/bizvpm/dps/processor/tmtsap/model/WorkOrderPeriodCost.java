package com.bizvpm.dps.processor.tmtsap.model;

/**
 * ֱ�Ӽ�¼�ڹ�������µ��з��ɱ�
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

	@Override
	public Double getAccountValue(String accountNumber) {
		return null;
	}

	// public String getCostCode() {
	// return (String) getValue(F_COSTCENTERCODE);
	// }
	//
	// public String getLabel() {
	// String label = getDesc();
	// String costCode = getCostCode();
	// if (label == null) {
	// return costCode;
	// } else {
	// return costCode + label;
	// }
	// }

}