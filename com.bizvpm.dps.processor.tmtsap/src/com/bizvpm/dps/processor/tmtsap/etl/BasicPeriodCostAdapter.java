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
	 * ���SAP�ɱ������ڼ��з��ɱ�������
	 * 
	 * @param orgCodeArray
	 *            , ��֯�������飬��Ӧ�ڳɱ����Ĵ�������
	 * @param costCodeArray
	 *            ���ɱ����Ĵ������飬��Ӧ����֯��������
	 * @param start
	 *            , ������ڼ俪ʼʱ��
	 * @param end
	 *            , �ڼ����ʱ��
	 * @param account
	 *            , �з��ɱ���Ŀ, Ϊ��ʱȡȫ����Ŀ
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

		// TODO ���浽���ݿ�
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
