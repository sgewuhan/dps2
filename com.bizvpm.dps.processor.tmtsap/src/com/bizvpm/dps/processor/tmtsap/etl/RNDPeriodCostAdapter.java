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
		// ��̯��ÿ����Ŀ�Ĺ������
		Check.isAssigned(result,
				l -> l.forEach(doc -> allocateToWorkOrder((String) doc.get("costcenter"), year, month, doc)));
		return result;
	}

	/**
	 * �����ڳ�����ʱʹ�ã������ǹ�ʱ��¼ֱ�Ӹ�����Ŀ���з�̯
	 * 
	 * @param costCenterCode
	 * @param year
	 * @param month
	 * @param dbObject
	 */
	public void allocateToWorkOrder(String costCenterCode, int year, int month, Document costCenterRNDCostData) {
		// ��ʼ����������ڼ����ݷ�̯������
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
