package com.bizvpm.dps.processor.tmtsap;

import java.util.Map;

public interface ICostCollector {

	public Map<String, Map<String, Double>> getCost(String[] costCodeArray,
			String[] workordersArray, String[] costElementArray, int year,
			int month) throws Exception;
}
