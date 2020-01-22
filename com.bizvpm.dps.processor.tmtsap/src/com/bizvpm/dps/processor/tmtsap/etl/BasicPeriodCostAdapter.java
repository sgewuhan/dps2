package com.bizvpm.dps.processor.tmtsap.etl;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.bizvpm.dps.processor.tmtsap.ICostCollector;
import com.bizvpm.dps.processor.tmtsap.model.WorkOrderPeriodCost;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public abstract class BasicPeriodCostAdapter {

	public static final String ORGCODE = "org"; //$NON-NLS-1$
	public static final String COSECENTERCODE = "cost"; //$NON-NLS-1$
	public static final String ACCOUNTNUMERS = "account"; //$NON-NLS-1$
	public static final String YEAR = "year"; //$NON-NLS-1$
	public static final String MONTH = "month"; //$NON-NLS-1$
	private ICostCollectorFactory ccf;

	public BasicPeriodCostAdapter(ICostCollectorFactory ccf) {
		this.ccf = ccf;
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
	public DBObject[] runGetData(String[] costCodeArray, String[] workordersArray, String[] costElementArray, int year,
			int month, String targetCollection) throws Exception {
		ICostCollector cc = createCostCollector();
		Map<String, Map<String, Double>> ret = cc.getCost(costCodeArray, workordersArray, costElementArray, year,
				month);

		DBObject[] sr = new BasicDBObject[ret.size()];
		Iterator<String> iter = ret.keySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			String costCenterCode = iter.next();
			sr[i] = new BasicDBObject();
			sr[i].put(WorkOrderPeriodCost.F_WORKORDER, costCenterCode);
			sr[i].put(WorkOrderPeriodCost.F_COSTCENTERCODE, costCenterCode);
			sr[i].put(WorkOrderPeriodCost.F_MONTH, new Integer(month));
			sr[i].put(WorkOrderPeriodCost.F_YEAR, new Integer(year));
			sr[i].put(WorkOrderPeriodCost.F__CDATE, new Date());
			Map<String, Double> values = ret.get(costCenterCode);
			if (values != null) {
				sr[i].putAll(values);
			}
			i++;
		}

		// TODO 保存到数据库
		// DBCollection col = DBActivator.getCollection(IModelConstants.DB,
		// targetCollection);
		// col.insert(sr, WriteConcern.UNACKNOWLEDGED);

		return sr;
	}

	private ICostCollector createCostCollector() {
		return ccf.createCostCollector();
	}

	public void setCostCollectorFactory(ICostCollectorFactory ccf) {
		this.ccf = ccf;
	}

}
