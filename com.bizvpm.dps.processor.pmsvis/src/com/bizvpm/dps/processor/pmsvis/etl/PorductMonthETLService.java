package com.bizvpm.dps.processor.pmsvis.etl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.processor.mongodbds.Domain;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Variable;

public class PorductMonthETLService implements IProcessorRunable {

	@Override
	public ProcessResult run(ProcessTask pT, IProgressMonitor monitor, IProcessContext context) throws Exception {
		MongoDatabase db = Domain.getDatabase((String) pT.get("domain"));
		String year = "" + pT.get("year");
		String month = String.format("%03d", pT.get("month"));

		MongoCollection<Document> prjColl = db.getCollection("project");
		MongoCollection<Document> sMDCol = db.getCollection("salesMonthData");
		createSalesMonthData(prjColl, sMDCol, year, month);
		ProcessResult result = new ProcessResult();
		result.put("result", "");
		return result;
	}

	/**
	 * 构建项目-产品月销售数据
	 * 
	 * @param db
	 * @param year
	 * @param month
	 */
	private void createSalesMonthData(MongoCollection<Document> prjColl, MongoCollection<Document> sMDCol, String year,
			String month) {
		// 获取项目-产品月销售数据
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.addFields(new Field<String>("sYear", year), new Field<String>("sMonth", month)));
		pipeline.add(Aggregates.lookup("productitem", "_id", "project_id", "productitem"));
		pipeline.add(
				Aggregates
						.lookup("salesdata",
								Arrays.asList(new Variable<String>("matnr", "$productitem.id"), new Variable<String>(
										"gjahr", "$sYear"), new Variable<String>("perde", "$sMonth")),
								Arrays.asList(
										Aggregates.match(new BasicDBObject("$expr",
												new BasicDBObject("$and", Arrays.asList(
														new BasicDBObject("$in", Arrays.asList("$MATNR", "$$matnr")),
														new BasicDBObject("$eq", Arrays.asList("$GJAHR", "$$gjahr")),
														new BasicDBObject("$eq",
																Arrays.asList("$PERDE", "$$perde")))))),
										Aggregates.group(null,
												new BsonField("revenue", new BasicDBObject("$sum", "$VV010")),
												new BsonField("cost",
														new BasicDBObject("$sum",
																new BasicDBObject("$add",
																		Arrays.asList("$VV030", "$VV040")))),
												new BsonField("profit", new BasicDBObject("$sum",
														new BasicDBObject("$subtract", Arrays.asList("$VV010",
																new BasicDBObject("$add",
																		Arrays.asList("$VV030", "$VV040")))))))),
								"salesData"));
		pipeline.add(Aggregates.unwind("$salesData"));
		pipeline.add(Aggregates.project(new BasicDBObject("project_id", "$_id").append("revenue", "$salesData.revenue")
				.append("cost", "$salesData.cost").append("profit", "$salesData.profit").append("GJAHR", "$sYear")
				.append("PERDE", "$sMonth")));
		pipeline.add(Aggregates.project(new BasicDBObject("_id", false)));
		List<Document> salesMonthDatas = new ArrayList<Document>();
		prjColl.aggregate(pipeline).into(salesMonthDatas);

		// 清除之前写入的月销售数据
		sMDCol.deleteMany(new BasicDBObject("year", year).append("month", month));

		// 插入月销售数据
		if (salesMonthDatas.size() > 0)
			sMDCol.insertMany(salesMonthDatas);
	}

}
