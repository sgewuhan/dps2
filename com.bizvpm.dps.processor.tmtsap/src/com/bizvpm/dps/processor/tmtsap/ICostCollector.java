package com.bizvpm.dps.processor.tmtsap;

import java.util.List;
import java.util.Map;

public interface ICostCollector {

	public Map<String, Map<String, Double>> getCost(List<String> costCodeArray,
			List<String> workordersArray, List<String> costElementArray, int year,
			int month) throws Exception;
}
