package com.bizvpm.dps.processor.tmtsap.model;

import org.bson.Document;

public class RNDPeriodCost implements IAccountPeriod, IFinanceAccountNumber {

	public static final String F_YEAR = "year"; //$NON-NLS-1$

	public static final String F_MONTH = "month"; //$NON-NLS-1$

	public static final String F_COSTCENTERCODE = "costcenter"; //$NON-NLS-1$

	@Override
	public Double getAccountValue(String accountNumber) {
		return null;//(Double) getValue(accountNumber);
	}

	public String getCostCode() {
		return null;//(String) getValue(F_COSTCENTERCODE);
	}

	// @Override
	// public String getLabel() {
	// String label = getDesc();
	// String costCode = getCostCode();
	// if (label == null) {
	// return costCode;
	// } else {
	// return costCode + label;
	// }
	// }

	public Document getOrganization() {
//		DBCollection col = getCollection(IModelConstants.C_ORGANIZATION);
//		DBObject data = col.findOne(new BasicDBObject().append(Organization.F_COST_CENTER_CODE, getCostCode()));
//
//		if (data != null) {
//			return ModelService.createModelObject(data, Organization.class);
//		}
		return null;
	}

	public Document get_data() {
		return null;
	}

}
