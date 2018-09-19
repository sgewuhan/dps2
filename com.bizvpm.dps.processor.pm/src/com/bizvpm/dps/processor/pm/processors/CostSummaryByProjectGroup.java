package com.bizvpm.dps.processor.pm.processors;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;

public class CostSummaryByProjectGroup extends DatabaseProcessor {

	public static final String[] fileds = new String[] { "8020030000", "8009040000", "8017050000", "8009070000",
			"8016040000", "8009030000", "8033010000", "8002030000", "8001010000", "8002010000", "8021010000",
			"8007010000", "8009050000", "8002060000", "8017070000", "8019030000", "8009060000", "8015010000",
			"8009010000", "8010020000", "8022010000", "8019010000", "8015020000", "8009100000", "8011010000",
			"8012020000", "8030010000", "8029010000", "8010010000", "8016030000", "8019040000" };

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		//String _year = (String) processTask.get("year");
		//String _month = (String) processTask.get("month");

		// int year = Integer.parseInt(_year);
		// int month = Integer.parseInt(_month);

		ProcessResult result = new ProcessResult();
		// MongoCollection<BasicDBObject> col =
		// getCollection("projectMonthData");
		// 取数演示程序，只取三条测试

		//List<Map<String,Object>> results = AggregetionData.getResult(getDB(), _year, _month);
		//result.put("results", results);
//
//		Map<String, Object> row = new HashMap<String, Object>();
//		row.put("year", _year);
//		row.put("month", _month);
//		row.put("complanyCode", "4010");
//		row.put("costCenterCode", "E001010000");
//		row.put("buzScopeCode", "4000");
//		row.put("accountCode", "8083010000");
//		row.put("pjGroupCode", "100000");
//		row.put("ammount", 45678d);
//		results.add(row);
//
//		row = new HashMap<String, Object>();
//		row.put("year", _year);
//		row.put("month", _month);
//		row.put("complanyCode", "4010");
//		row.put("costCenterCode", "E001010000");
//		row.put("buzScopeCode", "4050");
//		row.put("accountCode", "8083010000");
//		row.put("pjGroupCode", "100001");
//		row.put("ammount", 12345d);
//		results.add(row);
//
//		row = new HashMap<String, Object>();
//		row.put("year", _year);
//		row.put("month", _month);
//		row.put("complanyCode", "4010");
//		row.put("costCenterCode", "E001010000");
//		row.put("buzScopeCode", "4000");
//		row.put("accountCode", "8083010000");
//		row.put("pjGroupCode", "100000");
//		row.put("ammount", 99999d);
//		results.add(row);

		return result;
	}
}
