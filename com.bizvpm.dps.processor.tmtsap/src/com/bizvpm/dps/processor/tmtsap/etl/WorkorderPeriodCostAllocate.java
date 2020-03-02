package com.bizvpm.dps.processor.tmtsap.etl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.bizvpm.dps.processor.mongodbds.Domain;
import com.bizvpm.dps.processor.tmtsap.model.RNDPeriodCost;
import com.bizvpm.dps.processor.tmtsap.model.WorkOrderPeriodCost;
import com.bizvpm.dps.processor.tmtsap.tools.Check;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;

/**
 * ʹ����Ŀʵ�ʿ�ʼʱ������ʱ������з��ɱ���̯ �������ڳ����ݵĵ���
 * 
 * @author Administrator
 * 
 */
public class WorkorderPeriodCostAllocate {

	public static final String COSECENTERCODE = "cost";
	public static final String ACCOUNTNUMERS = "account";
	public static final String YEAR = "year";
	public static final String MONTH = "month";
	public static final String RNDCOST = "rndcost";
	private MongoCollection<Document> costAllocateCol;
	private MongoCollection<Document> projectCol;
	private String domain;

	public WorkorderPeriodCostAllocate(String domain) {
		this.domain = domain;
		projectCol = Domain.getCollection(domain, "project");
		costAllocateCol = Domain.getCollection(domain, "rndcostallocation");
	}

	public Collection<? extends WorkOrderPeriodCost> getData(Document parameter) {

		Object year = parameter.get(YEAR);
		Object month = parameter.get(MONTH);
		if (!(year instanceof Integer) || !(month instanceof Integer)) {
			throw new IllegalArgumentException("�ڼ� year, month��������");
		}

		Object costCenterCode = parameter.get(COSECENTERCODE);
		if (!(costCenterCode instanceof String)) {
			throw new IllegalArgumentException("�ɱ����Ĵ��� costcode ��������");
		}

		Object rndCost = parameter.get(RNDCOST);
		if (!(rndCost instanceof RNDPeriodCost)) {
			throw new IllegalArgumentException("�ɱ������з��ɱ�  rndcost ��������");
		}

		// 1. ���ݳɱ����Ļ����֯
		RNDPeriodCost rndpc = ((RNDPeriodCost) rndCost);
		Document org = rndpc.getOrganization();

		if (org == null) {
			throw new IllegalArgumentException("�ɱ������޷���ö�Ӧ����֯");
		}

		Document company = getCompany(org);
		if (company == null) {
			throw new IllegalArgumentException("�ɱ������޷���ö�Ӧ�Ĺ�˾����");
		}

		// �����֯�¼��������ڽ��е���Ŀ
		List<Document> list = getOrganizationStructure(company);
		List<Object> orgids = new ArrayList<Object>();
		Check.isAssigned(list, l -> l.forEach(doc -> orgids.add(doc.get("_id"))));

		// �е���֯��orgids��,�ڵ��´��ڽ���״̬�ĵ���Ŀ�Ĺ������
		Calendar cal = Calendar.getInstance();
		cal.set(((Integer) year).intValue(), ((Integer) month).intValue() - 1, 1, 0, 0, 0);
		Date stop = cal.getTime();

		// ����ȡ��һ�·ݵ�һ�������
		cal.add(Calendar.MONTH, 1);
		Date start = cal.getTime();

		Document query = new Document();
		query.put("launchorg_id", new Document("$in", orgids));
		query.put("$and", Arrays.asList(new Document("$or", Arrays.asList(
				// ���ʱ��Ϊ��
				// �������ʱ�����ǰһ�µ����һ��
				new Document("actualfinish", null), new Document("actualfinish", new Document("$gte", stop)))),
				new Document("$or", Arrays.asList(
						// ��ʼʱ����벻Ϊ��
						// ��ʼʱ�����С�ڵ��µ����һ��
						new Document("actualstart", new Document("$ne", null)),
						new Document("actualstart", new Document("$lt", start))))));

		List<String> workorders = projectCol.distinct("workorder", query, String.class).into(new ArrayList<String>());

		// Ӧ�����������ɱ����ĵĹ������
		Set<String> effectiveWorkOrders = new HashSet<String>();

		// ������ֶ���������ŵģ�������֯������ŵĶ�Ӧ�Ƿ���ȷ������Ǹóɱ����ĵĹ�����ţ�����ʱ���ݱ������ù������
		Check.isAssigned(workorders, l -> l.stream().filter(w -> hasWorkOrder(company, w))
				.forEach(workOrder -> effectiveWorkOrders.add(workOrder)));

		// ���з��ɱ�ƽ̯��ÿ���������
		
		if (effectiveWorkOrders.isEmpty()) {
			System.out.println("�ɱ�����" + costCenterCode + company + ", ���ڼ�:" + year + month
					+ " �޿ɷ�̯�з��ɱ��Ĺ������,�������ڸ��ڼ�û�����ڽ��е���Ŀ�ɹ���̯��");
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

			Document data = rndpc.get_data();
			Iterator<String> iter2 = data.keySet().iterator();
			while (iter2.hasNext()) {
				String key = iter2.next();
				if (!Check.isNumbers(key)) {// �����������͵��ֶκ���
					continue;
				}
				Object cost = data.get(key);
				if (cost == null) {
					wopc.put(key, 0d);
				} else {
					double value = ((Double) cost) / effectiveWorkOrders.size();
					wopc.put(key, value);
				}
			}

			toBeInsert.add(wopc);
		}

		List<WorkOrderPeriodCost> result = new ArrayList<WorkOrderPeriodCost>();
		if (toBeInsert.size() > 0) {
			costAllocateCol.insertMany(toBeInsert);
			toBeInsert.forEach(doc -> result.add(new WorkOrderPeriodCost(doc)));
		}

		return result;
	}

	private boolean hasWorkOrder(Document company, String workOrder) {
		long count = Domain.getCollection(domain, "companyworkorders").countDocuments(
				new BasicDBObject().append("organizationid", company.get("_id")).append("workorderid", workOrder));
		return count > 0;

	}

	private List<Document> getOrganizationStructure(Document doc) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("_id", doc.get("_id"))));
		pipeline.add(Aggregates.graphLookup("organization", "$_id", "parent_id", "_id", "parent"));
		pipeline.add(Aggregates.graphLookup("organization", "$_id", "_id", "parent_id", "child"));
		pipeline.add(Aggregates.addFields(
				new Field<Document>("structure", new Document("$setUnion", new Document("$parent", "$child")))));
		pipeline.add(Aggregates.unwind("$structure"));
		pipeline.add(Aggregates.replaceRoot("$structure"));
		return Domain.getCollection(domain, "organization").aggregate(pipeline).into(new ArrayList<Document>());
	}

	private Document getCompany(Document org) {
		Object companycode = org.get("companycode");
		if ((!(companycode instanceof List)) || ((List<?>) companycode).isEmpty()) {
			if (org.get("parent_id") != null) {
				Document parent = Optional
						.ofNullable(Domain.getCollection(domain, "organization")
								.find(new Document("_id", org.get("parent_id"))))
						.map(mapper -> mapper.first()).orElse(null);
				return getCompany(parent);
			}
			return null;
		} else
			return org;
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
