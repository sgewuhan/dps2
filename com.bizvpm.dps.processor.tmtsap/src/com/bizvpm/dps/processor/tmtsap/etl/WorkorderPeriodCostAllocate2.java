package com.bizvpm.dps.processor.tmtsap.etl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvpm.dps.processor.tmtsap.model.RNDPeriodCost;
import com.bizvpm.dps.processor.tmtsap.model.WorkOrderPeriodCost;
import com.bizvpm.dps.processor.tmtsap.tools.Check;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.client.MongoCollection;

/**
 * 使用项目实际开始时间和完成时间进行研发成本分摊 仅用于期初数据的导入
 * 
 * @author Administrator
 * 
 */
public class WorkorderPeriodCostAllocate2 {

	public static final String COSECENTERCODE = "cost";
	public static final String ACCOUNTNUMERS = "account";
	public static final String YEAR = "year";
	public static final String MONTH = "month";
	public static final String RNDCOST = "rndcost";
	private MongoCollection<Document> costAllocateCol;
	private MongoCollection<Document> projectCol;

	public WorkorderPeriodCostAllocate2() {
		// costAllocateCol = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_RND_PEROIDCOST_ALLOCATION);
		// projectCol = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_PROJECT);
	}

	public Collection<? extends WorkOrderPeriodCost> getData(Map<String, Object> parameter) {

		Object year = parameter.get(YEAR);
		Object month = parameter.get(MONTH);
		if (!(year instanceof Integer) || !(month instanceof Integer)) {
			throw new IllegalArgumentException("期间 year, month参数错误");
		}

		Object costCenterCode = parameter.get(COSECENTERCODE);
		if (!(costCenterCode instanceof String)) {
			throw new IllegalArgumentException("成本中心代码 costcode 参数错误");
		}

		Object account = parameter.get(ACCOUNTNUMERS);
		if (account != null && !(account instanceof String[])) {
			throw new IllegalArgumentException("科目表  account 参数错误");
		}

		Object rndCost = parameter.get(RNDCOST);
		if (!(rndCost instanceof RNDPeriodCost)) {
			throw new IllegalArgumentException("成本中心研发成本  rndcost 参数错误");
		}

		// 1. 根据成本中心获得组织
		RNDPeriodCost rndpc = ((RNDPeriodCost) rndCost);
		Document org = rndpc.getOrganization();

		if (org == null) {
			throw new IllegalArgumentException("成本中心无法获得对应的组织");
		}

		// Document company = org.getCompany();
		// if (company == null) {
		// throw new IllegalArgumentException("成本中心无法获得对应的公司代码");
		// }

		// 获得组织下级所有正在进行的项目
		// List<Document> list = company.getOrganizationStructure();
		// ObjectId[] orgids = new ObjectId[list.size()];
		// for (int i = 0; i < orgids.length; i++) {
		// orgids[i] = list.get(i).get_id();
		// }

		// 承担组织在orgids中,在当月处于进行状态的的项目的工作令号
		Calendar cal = Calendar.getInstance();
		cal.set(((Integer) year).intValue(), ((Integer) month).intValue() - 1, 1, 0, 0, 0);
		Date stop = cal.getTime();

		// 增加取后一月份第一天的数据
		cal.add(Calendar.MONTH, 1);
		Date start = cal.getTime();

		Document query = new Document();
		// TODO
		query.put("launchorg_id", new BasicDBObject("$in", orgids));
		query.put("$and", new BasicDBObject[] { new BasicDBObject("$or", new BasicDBObject[] {
				// 完成时间为空
				// 或者完成时间大于前一月的最后一天
				new BasicDBObject("actualfinish", null),
				new BasicDBObject("actualfinish", new BasicDBObject("$gte", stop)) }),

				new BasicDBObject("$or", new BasicDBObject[] {
						// 开始时间必须不为空
						// 开始时间必须小于当月的最后一天
						new BasicDBObject("actualstart", new BasicDBObject("$ne", null)),
						new BasicDBObject("actualstart", new BasicDBObject("$lt", start)) }) });

		@SuppressWarnings("rawtypes")
		List workorders = projectCol.distinct("workorder", query);

		// 应当归属到本成本中心的工作令号
		Set<String> effectiveWorkOrders = new HashSet<String>();

		// 如果出现多条工作令号的，检查该组织工作令号的对应是否正确，如果是该成本中心的工作令号，将工时数据保存至该工作令号
		for (int i = 0; i < workorders.size(); i++) {
			String workOrder = (String) workorders.get(i);
			if (company.hasWorkOrder(workOrder)) {
				effectiveWorkOrders.add(workOrder);
			}
		}

		// 将研发成本平摊到每个工作令号
		if (effectiveWorkOrders.isEmpty()) {
			System.out.println("成本中心" + costCenterCode + company + ", 在期间:" + year + month
					+ " 无可分摊研发成本的工作令号,可能是在该期间没有正在进行的项目可供分摊。");
			return new ArrayList<WorkOrderPeriodCost>();
		}

		List<Document> toBeInsert = new ArrayList<Document>();
		Iterator<?> iter = effectiveWorkOrders.iterator();
		while (iter.hasNext()) {
			String workOrderNumber = (String) iter.next();
			Document wopc = new Document();
			wopc.put(WorkOrderPeriodCost.F_WORKORDER, workOrderNumber);
			wopc.put(WorkOrderPeriodCost.F_YEAR, year);
			wopc.put(WorkOrderPeriodCost.F_MONTH, month);
			wopc.put(WorkOrderPeriodCost.F_COSTCENTERCODE, costCenterCode);

			Iterator<String> iter2 = rndpc.get_data().keySet().iterator();
			while (iter2.hasNext()) {
				String key = iter2.next();
				if (!Check.isNumbers(key)) {// 不是数字类型的字段忽略
					continue;
				}
				Object cost = rndpc.getValue(key);
				if (cost == null) {
					wopc.put(key, 0d);
				} else {
					double value = ((Double) cost) / effectiveWorkOrders.size();
					wopc.put(key, value);
				}
			}

			toBeInsert.add(wopc);
		}

		if (toBeInsert.size() > 0) {
			costAllocateCol.insertMany(toBeInsert);
		}

		// 准备返回
		ArrayList<WorkOrderPeriodCost> result = new ArrayList<WorkOrderPeriodCost>();
		// for (int i = 0; i < toBeInsert.size(); i++) {
		// result.add(ModelService.createModelObject(toBeInsert.get(i),
		// WorkOrderPeriodCost.class));
		// }

		return result;
	}

	public Date[] getStartAndEnd(Integer year, Integer month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.MILLISECOND, -1);
		Date end = cal.getTime();
		return new Date[] { start, end };
	}

}
