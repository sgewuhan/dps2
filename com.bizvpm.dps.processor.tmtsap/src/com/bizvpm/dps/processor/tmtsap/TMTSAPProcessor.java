package com.bizvpm.dps.processor.tmtsap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.processor.mongodbds.Domain;
import com.bizvpm.dps.processor.tmtsap.etl.ICostCollectorFactory;
import com.bizvpm.dps.processor.tmtsap.etl.RNDPeriodCostAdapter;
import com.bizvpm.dps.processor.tmtsap.etl.WorkorderPeriodCostAdapter;
import com.bizvpm.dps.processor.tmtsap.tools.Check;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Variable;

public class TMTSAPProcessor implements IProcessorRunable {

	private MongoCollection<Document> rndCol;
	private MongoCollection<Document> workOrderCol;
	private MongoCollection<Document> saleDataCol;
	private MongoCollection<Document> rndAllocationCol;
	private MongoCollection<Document> cbsSubjectCol;
	private MongoCollection<Document> saleMonthDataCol;
	private String domain;
	private MongoCollection<Document> projectCol;

	public TMTSAPProcessor() {
	}

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		domain = (String) processTask.get("domain");
		Integer year = (Integer) processTask.get("year");
		Integer month = (Integer) processTask.get("month");
		Integer day = (Integer) processTask.get("day");
		boolean isDoMonthData = false;

		Calendar cal = Calendar.getInstance();
		if (year != null)
			cal.set(Calendar.YEAR, year);
		if (month != null)
			cal.set(Calendar.MONTH, month - 1);
		if (day != null)
			cal.set(Calendar.DAY_OF_MONTH, day);

		if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
			isDoMonthData = true;
		}

		// 减少一天
		cal.add(Calendar.DATE, -1);
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH) + 1;
		day = cal.get(Calendar.DAY_OF_MONTH);

		initCollection();

		List<String> costCodes = getCostCodeArray();
		List<String> costElementArray = getCostElementArray();
		List<String> workOrders = getWorkOrders();

		ICostCollectorFactory costCollectorFactory = new SAPCostCollectorFactory();

		doETL(year, month, day, isDoMonthData, costElementArray, workOrders, costCodes, costCollectorFactory,
				costCollectorFactory, new SAPSaleDataCollector());

		ProcessResult result = new ProcessResult();
		result.put("result", "");
		return result;
	}

	private boolean doETL(int year, int month, int day, boolean isDoMonthData, List<String> costElementArray,
			List<String> workOrders, List<String> costCodes, ICostCollectorFactory costCodeFactory,
			ICostCollectorFactory workOrdersFactory, ISaleDataCollector sdc) throws Exception {
		long start, end;

		clear(year, month);

		System.out.println("[成本数据]准备获取成本中心数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		RNDPeriodCostAdapter rndAdapter = new RNDPeriodCostAdapter(costCodeFactory, domain);
		rndAdapter.runGetData(costCodes, costElementArray, year, month);
		end = System.currentTimeMillis();
		System.out.println("[成本数据]获得成本中心数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[成本数据]准备获取工作令号研发成本数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		WorkorderPeriodCostAdapter workorderadapter = new WorkorderPeriodCostAdapter(workOrdersFactory, domain);
		workorderadapter.runGetData(workOrders, costElementArray, year, month);
		end = System.currentTimeMillis();
		System.out.println("[成本数据]获得工作令号研发成本完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[销售数据]准备获取销售数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		runGetData(year, month, sdc);
		end = System.currentTimeMillis();
		System.out.println("[销售数据]获得销售数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[项目数据]准备更新项目月销售数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		doProjectMonthSalesData(year, month);
		end = System.currentTimeMillis();
		System.out.println("[项目数据]更新项目月销售数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[组织指标数据]准备更新项目月成本数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		doCBSSubject(year, month);
		end = System.currentTimeMillis();
		System.out.println("[组织指标数据]更新项目月成本数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		return true;
	}

	@SuppressWarnings("unchecked")
	private void doCBSSubject(int year, int month) {
		List<Document> result = new ArrayList<Document>();
		cbsSubjectCol.find(new BasicDBObject("id", "" + year + String.format("%02d", month))).into(result);
		projectCol.aggregate(Arrays.asList(Aggregates.lookup("cbs", "_id", "scope_id", "cbs"),
				Aggregates.addFields(new Field<String>("cbs", "$cbs._id")),
				Aggregates.lookup("cbsSubject", "cbs", "cbsItem_id", "cbsSubject"))).forEach((Document pj) -> {
					String wos = pj.getString("workOrder");
					List<Document> cbsSubject = (List<Document>) pj.get("cbsSubject");
					if (cbsSubject == null)
						cbsSubject = new ArrayList<Document>();
					Check.isAssigned(wos, wo -> {
						String[] workOrders = wo.split(",");

//						 rndAllocationCol.aggregate(Arrays.asList(Aggregates.match(new BasicDBObject(""))));

						// workOrderCol.aggregate(Arrays.asList());
					});

				});
		if (result.size() > 0) {
			cbsSubjectCol.deleteMany(new BasicDBObject("id", "" + year + String.format("%02d", month)));
			cbsSubjectCol.insertMany(result);
		}
	}

	private void doProjectMonthSalesData(int year, int month) {
		List<Document> pms = new ArrayList<Document>();
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.lookup("productitem", "_id", "project_id", "productitem"));
		pipeline.add(Aggregates.addFields(new Field<String>("productitem", "$productitem.id")));
		pipeline.add(
				Aggregates.lookup("salesdata", Arrays.asList(new Variable<String>("productitem", "$productitem")),
						Arrays.asList(
								Aggregates.match(new Document("$expr", new Document("$and",
										Arrays.asList(new Document("$in", Arrays.asList("$MATNR", "$$productitem")),
												new Document("$eq", Arrays.asList("$GJAHR", "" + year)),
												new Document("$eq",
														Arrays.asList("$PERDE", String.format("%03d", month))))))),
								Aggregates.group(null,
										new BsonField("VV010",
												new Document("$sum",
														new Document("$ifNull", Arrays.asList("$VV010", 0)))),
										new BsonField("VV030",
												new Document("$sum",
														new Document("$ifNull", Arrays.asList("$VV030", 0)))),
										new BsonField("VV040",
												new Document("$sum",
														new Document("$ifNull", Arrays.asList("$VV040", 0)))))),
						"salesdata"));
		pipeline.add(Aggregates.unwind("$salesdata"));
		pipeline.add(Aggregates.addFields(new Field<String>("VV010", "$salesdata.VV010"),
				new Field<String>("VV030", "$salesdata.VV030"), new Field<String>("VV040", "$salesdata.VV040")));
		saleDataCol.aggregate(pipeline).forEach((Document doc) -> {
			pms.add(new Document("GJAHR", "" + year).append("PERDE", String.format("%03d", month))
					.append("project_id", doc.get("_id")).append("VV010", doc.get("VV010"))
					.append("VV030", doc.get("VV030")).append("VV040", doc.get("VV040")));
		});
		if (pms.size() > 0)
			saleMonthDataCol.insertMany(pms);

	}

	private void clear(int year, int month) {
		rndClear(year, month);
		workOrderClear(year, month);
		saleDataClear(year, month);
	}

	private void initCollection() {
		rndCol = Domain.getCollection(domain, "rndcost");
		rndAllocationCol = Domain.getCollection(domain, "rndcostallocation");
		workOrderCol = Domain.getCollection(domain, "workordercost");
		saleDataCol = Domain.getCollection(domain, "salesdata");
		cbsSubjectCol = Domain.getCollection(domain, "cbsSubject");
		saleMonthDataCol = Domain.getCollection(domain, "salesMonthData");
		projectCol = Domain.getCollection(domain, "project");

	}

	private void runGetData(int year, int month, ISaleDataCollector sdc) throws Exception {
		sdc.runGetData(saleDataCol, year, month);
	}

	private void rndClear(int year, int month) {
		rndCol.deleteMany(new BasicDBObject("year", year).append("month", month));
		rndAllocationCol.deleteMany(new BasicDBObject("year", year).append("month", month));
		cbsSubjectCol.updateMany(new BasicDBObject("id", "" + year + String.format("%02d", month)),
				new BasicDBObject("$set", new BasicDBObject("cost", null)));
	}

	private void workOrderClear(int year, int month) {
		workOrderCol.deleteMany(new BasicDBObject("year", year).append("month", month));
	}

	private void saleDataClear(int year, int month) {
		String gjahr = "" + year;
		String perde = String.format("%03d", month);
		saleDataCol.deleteMany(new BasicDBObject("GJAHR", gjahr).append("PERDE", perde));
		saleMonthDataCol.deleteMany(new BasicDBObject("id", gjahr + perde));
	}

	private List<String> getWorkOrders() {
		List<String> result = new ArrayList<String>();
		Domain.getCollection(domain, "project").distinct("workOrder", String.class).forEach((String workOrder) -> {
			if (workOrder != null && !workOrder.trim().isEmpty())
				result.addAll(Arrays.asList(workOrder.split(",")));
		});
		return result;
	}

	private List<String> getCostElementArray() {
		return Domain.getCollection(domain, "accountItem").distinct("id", String.class).into(new ArrayList<String>());
	}

	private List<String> getCostCodeArray() {
		return Domain.getCollection(domain, "organization")
				.distinct("costcentercode",
						new BasicDBObject().append("$and", new BasicDBObject[] {
								new BasicDBObject().append("costcentercode", new BasicDBObject().append("$ne", null)),
								new BasicDBObject().append("costcentercode", new BasicDBObject().append("$ne", "")) }),
						String.class)
				.into(new ArrayList<>());

	}

}
