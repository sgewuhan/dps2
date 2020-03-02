package com.bizvpm.dps.processor.tmtsap.model;

import java.util.Optional;

import org.bson.Document;

import com.bizvpm.dps.processor.mongodbds.Domain;
import com.bizvpm.dps.processor.tmtsap.tools.Check;

public class RNDPeriodCost implements IAccountPeriod, IFinanceAccountNumber {

	public static final String F_YEAR = "year"; //$NON-NLS-1$

	public static final String F_MONTH = "month"; //$NON-NLS-1$

	public static final String F_COSTCENTERCODE = "costcenter"; //$NON-NLS-1$

	private Document data;

	private String domain;

	public RNDPeriodCost(Document data, String domain) {
		this.data = data;
		this.domain = domain;
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
		if (Check.isNotAssigned(label)) {
			return costCode;
		} else {
			return costCode + label;
		}
	}

	public Document getOrganization() {
		return Optional
				.ofNullable(Domain.getCollection(domain, "organization")
						.find(new Document("costcentercode", getCostCode())))
				.map(mapper -> mapper.first()).orElse(null);
	}

	public Document get_data() {
		return data;
	}

}
