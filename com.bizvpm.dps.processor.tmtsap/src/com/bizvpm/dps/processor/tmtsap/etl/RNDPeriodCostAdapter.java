package com.bizvpm.dps.processor.tmtsap.etl;

import java.util.List;

import org.bson.Document;

import com.bizvpm.dps.processor.tmtsap.model.RNDPeriodCost;
import com.bizvpm.dps.processor.tmtsap.tools.Check;

public class RNDPeriodCostAdapter extends BasicPeriodCostAdapter {

	public RNDPeriodCostAdapter(ICostCollectorFactory ccf, String domain) {
		super(ccf, domain);
	}

	public List<Document> runGetData(List<String> costCodeArray, List<String> costElementArray, int year, int month)
			throws Exception {
		List<Document> result = runGetData(costCodeArray, null, costElementArray, year, month, "rndcost");
		// 分摊至每个项目的工作令号
		Check.isAssigned(result,
				l -> l.forEach(doc -> allocateToWorkOrder((String) doc.get("costcenter"), year, month, doc)));
		return result;
	}

	/**
	 * 导入期初数据时使用，不考虑工时记录直接根据项目进行分摊
	 * 
	 * @param costCenterCode
	 * @param year
	 * @param month
	 * @param dbObject
	 */
	public void allocateToWorkOrder(String costCenterCode, int year, int month, Document costCenterRNDCostData) {
		// 初始化工作令号期间数据分摊适配器
		WorkorderPeriodCostAllocate adapter = new WorkorderPeriodCostAllocate(domain);

		Document parameter = new Document();
		parameter.put(WorkorderPeriodCostAllocate.YEAR, year);
		parameter.put(WorkorderPeriodCostAllocate.MONTH, month);
		parameter.put(WorkorderPeriodCostAllocate.COSECENTERCODE, costCenterCode);
		RNDPeriodCost rndPeriodCost = new RNDPeriodCost(costCenterRNDCostData, domain);
		parameter.put(WorkorderPeriodCostAllocate.RNDCOST, rndPeriodCost);
		adapter.getData(parameter);
	}
}
