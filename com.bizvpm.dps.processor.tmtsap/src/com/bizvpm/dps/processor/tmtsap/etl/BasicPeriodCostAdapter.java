package com.bizvpm.dps.processor.tmtsap.etl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.bizvpm.dps.processor.mongodbds.Domain;
import com.bizvpm.dps.processor.tmtsap.ICostCollector;
import com.bizvpm.dps.processor.tmtsap.model.WorkOrderPeriodCost;

public abstract class BasicPeriodCostAdapter {

	public static final String ORGCODE = "org"; //$NON-NLS-1$
	public static final String COSECENTERCODE = "cost"; //$NON-NLS-1$
	public static final String ACCOUNTNUMERS = "account"; //$NON-NLS-1$
	public static final String YEAR = "year"; //$NON-NLS-1$
	public static final String MONTH = "month"; //$NON-NLS-1$
	private ICostCollectorFactory ccf;
	protected String domain;

	public BasicPeriodCostAdapter(ICostCollectorFactory ccf, String domain) {
		this.ccf = ccf;
		this.domain = domain;
	}

	/**
	 * 获得SAP成本中心期间研发成本的数据
	 * 
	 * @param orgCodeArray
	 *            , 组织代码数组，对应于成本中心代码数组
	 * @param costCodeArray
	 *            ，成本中心代码数组，对应于组织代码数组
	 * @param start
	 *            , 计算的期间开始时间
	 * @param end
	 *            , 期间结束时间
	 * @param account
	 *            , 研发成本科目, 为空时取全部科目
	 * @param costElementArray
	 * @param targetCollection
	 * @throws Exception
	 */
	public List<Document> runGetData(List<String> costCodeArray, List<String> workordersArray,
			List<String> costElementArray, int year, int month, String targetCollection) throws Exception {
		ICostCollector cc = createCostCollector();
		Map<String, Map<String, Double>> ret = cc.getCost(costCodeArray, workordersArray, costElementArray, year,
				month);
		List<Document> sr = new ArrayList<Document>();
		Iterator<String> iter = ret.keySet().iterator();
		while (iter.hasNext()) {
			String costCenterCode = iter.next();
			Document doc = new Document();
			sr.add(doc);
			doc.put(WorkOrderPeriodCost.F_WORKORDER, costCenterCode);
			doc.put(WorkOrderPeriodCost.F_COSTCENTERCODE, costCenterCode);
			doc.put(WorkOrderPeriodCost.F_MONTH, new Integer(month));
			doc.put(WorkOrderPeriodCost.F_YEAR, new Integer(year));
			doc.put(WorkOrderPeriodCost.F__CDATE, new Date());
			Map<String, Double> values = ret.get(costCenterCode);
			if (values != null) {
				doc.putAll(values);
			}
		}
		if (sr.size() > 0)
			Domain.getCollection(domain, targetCollection).insertMany(sr);
		return sr;
	}

	private ICostCollector createCostCollector() {
		return ccf.createCostCollector();
	}

	public void setCostCollectorFactory(ICostCollectorFactory ccf) {
		this.ccf = ccf;
	}

}
