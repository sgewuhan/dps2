package com.bizvpm.dps.processor.pmsvis.etl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;

import com.bizvpm.dps.processor.pmsvis.tools.Check;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public abstract class AbstractMonthETLService implements IProcessorRunable {

	/**
	 * 获取贴现率
	 * 
	 * @param db
	 * @return
	 */
	protected Double getDiscountRate(MongoDatabase db) {
		MongoCollection<Document> settingCol = db.getCollection("setting");
		return (Double) Optional.ofNullable(settingCol.find(new Document("name", "贴现率")).first())
				.map(d -> d.get("discountRate")).orElse(0.08);
	}

	protected double getIrr(Map<String, Double> outFlowMap, Map<String, Double> cashFlowMap) {
		List<Double> outFlow = getAllValue(outFlowMap);
		List<Double> cashFlow = getAllValue(cashFlowMap);

		final double minDif = 0.00000001;
		int loopNum = 1000;

		double minValue = 0d;
		double maxValue = 1d;
		double testValue = 0d;

		while (loopNum > 0) {
			testValue = (minValue + maxValue) / 2;
			double flowOut = getNPV(outFlow, testValue, outFlow.size());
			double npv = getNPV(cashFlow, testValue, outFlow.size());
			if (flowOut == 0 && npv == 0) {
				return 0d;
			} else if (Math.abs(npv + flowOut) < minDif) {
				break;
			} else if (Math.abs(flowOut) > npv) {
				maxValue = testValue;
			} else {
				minValue = testValue;
			}
			loopNum--;
		}
		return testValue;
	}

	private List<Double> getAllValue(Map<String, Double> outFlowMap) {
		List<Double> result = new LinkedList<Double>();
		String firstKey = null;
		String endKey = null;
		for (String key : outFlowMap.keySet()) {
			if (firstKey == null || firstKey.compareTo(key) > 0)
				firstKey = key;

			if (endKey == null || endKey.compareTo(key) < 0)
				endKey = key;
		}
		if (Check.isAssigned(firstKey) && Check.isAssigned(endKey)) {
			int first = Integer.parseInt(firstKey);
			int end = Integer.parseInt(endKey);
			while (first <= end) {
				result.add(outFlowMap.get(String.format("%04d", first)));
				first++;
			}
		}
		return result;
	}

	private double getNPV(List<Double> flowInArr, double rate, int n) {
		double npv = 0;
		for (int i = 0; i < flowInArr.size(); i++) {
			if ((i + 1) < n) {
				npv += Optional.ofNullable(flowInArr.get(i)).orElse(0d) * Math.pow(1 + rate, Math.abs(i - n + 1));
			} else if ((i + 1) > n) {
				npv += Optional.ofNullable(flowInArr.get(i)).orElse(0d) / Math.pow(1 + rate, Math.abs(i - n + 1));
			} else {
				npv += Optional.ofNullable(flowInArr.get(i)).orElse(0d);
			}
		}
		return npv;
	}
}
