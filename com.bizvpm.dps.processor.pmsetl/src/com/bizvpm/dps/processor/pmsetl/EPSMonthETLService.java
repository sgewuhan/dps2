package com.bizvpm.dps.processor.pmsetl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.processor.mongodbds.Domain;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class EPSMonthETLService extends AbstractMonthETLService {

	@Override
	public ProcessResult run(ProcessTask pT, IProgressMonitor monitor, IProcessContext context) throws Exception {
		MongoDatabase db = Domain.getDatabase((String) pT.get("domain"));
		String year = (String) pT.get("year");
		String month = (String) pT.get("month");

		MongoCollection<Document> prjColl = db.getCollection("eps");
		MongoCollection<Document> eMDCol = db.getCollection("epsMonthData");
		Double discountRate = getDiscountRate(db);

		createEPSMonthData(prjColl, eMDCol, discountRate, year, month);
		ProcessResult result = new ProcessResult();
		result.put("result", "");
		return result;
	}

	/**
	 * 构建EPS月绩效数据
	 * 
	 * @param prjColl
	 * @param eMDCol
	 * @param discountRate
	 * @param year
	 * @param month
	 */
	@SuppressWarnings("unchecked")
	private void createEPSMonthData(MongoCollection<Document> prjColl, MongoCollection<Document> eMDCol,
			Double discountRate, String year, String month) {
		// 获取EPS月绩效数据
		List<Bson> pipeline = Arrays.asList(new Document().append("$graphLookup", new Document()
				.append("from", "eps").append("startWith", "$_id").append("connectFromField", "_id")
				.append("connectToField", "parent_id").append("as", "children").append("depthField", "level")),
				new Document().append("$addFields", new Document().append("children", new Document().append(
						"$concatArrays", Arrays.asList("$children._id", Arrays.asList("$_id"))))),
				new Document().append("$lookup", new Document().append("from", "project")
						.append("let", new Document().append("eps_id", "$children"))
						.append("pipeline", Arrays.asList(new Document().append("$match",
								new Document().append("$expr", new Document().append("$and",
										Arrays.asList(
												new Document().append("$in", Arrays.asList("$eps_id", "$$eps_id"))))))))
						.append("as", "project_id")),
				new Document().append("$addFields", new Document().append("project_id", "$project_id._id")),
				new Document().append("$addFields", new Document().append("discountRate", discountRate).append(
						"nowYear", year).append("sDate", year + month)),
				new Document()
						.append("$lookup",
								new Document().append("from", "salesforecast")
										.append("let", new Document().append("project_id", "$project_id")
												.append("discountRate", "$discountRate").append("nowYear", "$nowYear"))
										.append("pipeline", Arrays.asList(
												new Document().append("$match", new Document().append("$expr",
														new Document().append("$and", Arrays.asList(new Document()
																.append("$in",
																		Arrays.asList("$project_id",
																				"$$project_id")))))),
												new Document().append("$group", new Document().append("_id", "$GJAHR")
														.append("revenue", new Document().append("$sum", "$VV010"))
														.append("cost", new Document().append("$sum",
																new Document().append("$add",
																		Arrays.asList("$VV030", "$VV040"))))
														.append("profit", new Document().append("$sum", new Document()
																.append("$subtract", Arrays.asList("$VV010",
																		new Document().append("$add",
																				Arrays.asList("$VV030", "$VV040"))))))),
												new Document().append("$addFields", new Document().append("exponent",
														new Document().append("$subtract", Arrays
																.asList(new Document().append("$toInt", "$_id"),
																		new Document().append("$toInt",
																				"$$nowYear"))))),
												new Document().append("$addFields", new Document()
														.append("presentRevenue", new Document().append("$cond",
																Arrays.asList(new Document()
																		.append("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$revenue",
																				new Document().append("$pow", Arrays
																						.asList(new Document().append(
																								"$add",
																								Arrays.asList(1.0,
																										"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply",
																				Arrays.asList("$revenue", new Document()
																						.append("$pow", Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))
														.append("presentCost",
																new Document().append("$cond", Arrays.asList(
																		new Document().append("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$cost",
																				new Document().append("$pow", Arrays
																						.asList(new Document().append(
																								"$add",
																								Arrays.asList(1.0,
																										"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply", Arrays
																				.asList("$cost", new Document().append(
																						"$pow",
																						Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))
														.append("presentProfit", new Document().append("$cond", Arrays
																.asList(new Document()
																		.append("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$profit",
																				new Document().append("$pow", Arrays
																						.asList(new Document().append(
																								"$add",
																								Arrays.asList(1.0,
																										"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply",
																				Arrays.asList("$profit", new Document()
																						.append("$pow", Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))),
												new Document().append("$addFields", new Document()
														.append("profitRate",
																new Document().append("$cond", Arrays.asList(
																		new Document().append("$eq", Arrays.asList(
																				new Document().append("$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																				0.0)),
																		0.0,
																		new Document().append(
																				"$divide",
																				Arrays.asList("$profit", "$revenue")))))
														.append("presentProfitRate",
																new Document().append("$cond", Arrays.asList(
																		new Document().append("$eq",
																				Arrays.asList(new Document().append(
																						"$ifNull",
																						Arrays.asList("$presentRevenue",
																								0.0)),
																						0.0)),
																		0.0,
																		new Document().append("$divide",
																				Arrays.asList("$presentProfit",
																						"$presentRevenue")))))),
												new Document().append("$sort", new Document().append("_id", 1.0))))
										.append("as", "salesForecastYearData")),
				new Document()
						.append("$lookup",
								new Document().append("from", "salesMonthData")
										.append("let",
												new Document().append("project_id", "$project_id")
														.append("discountRate",
																"$discountRate")
														.append("nowYear", "$nowYear").append("sDate", "$sDate"))
										.append("pipeline", Arrays.asList(
												new Document().append("$match",
														new Document().append("$expr",
																new Document().append("$and", Arrays.asList(
																		new Document().append("$in",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document().append("$lte",
																				Arrays.asList(
																						new Document().append("$concat",
																								Arrays.asList("$GJAHR",
																										"$PERDE")),
																						"$$sDate")))))),
												new Document().append("$group", new Document().append("_id", "$GJAHR")
														.append("revenue", new Document().append("$sum", "$revenue"))
														.append("cost",
																new Document().append("$sum", "$cost"))
														.append("profit", new Document().append("$sum", "$profit"))),
												new Document().append("$addFields", new Document().append("exponent",
														new Document().append("$subtract",
																Arrays.asList(new Document().append("$toInt", "$_id"),
																		new Document().append("$toInt",
																				"$$nowYear"))))),
												new Document().append("$addFields", new Document().append(
														"presentRevenue",
														new Document().append("$cond", Arrays.asList(
																new Document().append("$gte",
																		Arrays.asList("$exponent", 0.0)),
																new Document().append("$divide",
																		Arrays.asList("$revenue", new Document().append(
																				"$pow",
																				Arrays.asList(new Document().append(
																						"$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																						new Document().append("$abs",
																								"$exponent"))))),
																new Document().append("$multiply", Arrays.asList(
																		"$revenue", new Document().append("$pow", Arrays
																				.asList(new Document().append("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																						new Document().append("$abs",
																								"$exponent"))))))))
														.append("presentCost", new Document().append("$cond", Arrays
																.asList(new Document().append("$gte",
																		Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$cost",
																				new Document().append("$pow", Arrays
																						.asList(new Document()
																								.append("$add", Arrays
																										.asList(1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply", Arrays
																				.asList("$cost", new Document().append(
																						"$pow",
																						Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))
														.append("presentProfit", new Document().append("$cond", Arrays
																.asList(new Document().append("$gte",
																		Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$profit",
																				new Document().append("$pow", Arrays
																						.asList(new Document()
																								.append("$add", Arrays
																										.asList(1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply",
																				Arrays.asList("$profit", new Document()
																						.append("$pow", Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))),
												new Document().append("$addFields", new Document()
														.append("profitRate",
																new Document().append("$cond", Arrays.asList(
																		new Document().append("$eq", Arrays.asList(
																				new Document().append("$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																				0.0)),
																		0.0,
																		new Document().append("$divide",
																				Arrays.asList("$profit", "$revenue")))))
														.append("presentProfitRate",
																new Document().append("$cond", Arrays.asList(
																		new Document().append("$eq",
																				Arrays.asList(new Document().append(
																						"$ifNull",
																						Arrays.asList("$presentRevenue",
																								0.0)),
																						0.0)),
																		0.0,
																		new Document().append("$divide",
																				Arrays.asList("$presentProfit",
																						"$presentRevenue")))))),
												new Document().append("$sort", new Document().append("_id", 1.0))))
										.append("as", "salesRealityYearData")),
				new Document().append("$lookup",
						new Document()
								.append("from", "cbsSubject").append(
										"let",
										new Document()
												.append("project_id", "$project_id").append("discountRate",
														"$discountRate")
												.append("nowYear", "$nowYear"))
								.append("pipeline", Arrays.asList(new Document().append("$lookup",
										new Document().append("from", "cbs").append("localField", "cbsItem_id").append(
												"foreignField", "_id").append("as", "cbs")),
										new Document().append("$unwind", "$cbs"), new Document().append("$graphLookup",
												new Document().append("from", "cbs").append("startWith", "$cbs._id")
														.append("connectFromField", "parent_id")
														.append("connectToField", "_id").append("as", "cbs")),
										new Document().append("$lookup",
												new Document().append("from", "work").append("localField",
														"cbs.scope_id").append("foreignField", "_id")
														.append("as", "work")),
										new Document().append("$unwind",
												new Document().append("path", "$work")
														.append("preserveNullAndEmptyArrays", true)),
										new Document().append("$lookup",
												new Document().append("from", "project")
														.append("localField", "cbs.scope_id")
														.append("foreignField", "_id").append("as", "project")),
										new Document().append("$unwind", new Document().append("path", "$project")
												.append("preserveNullAndEmptyArrays", true)),
										new Document().append("$addFields",
												new Document().append("project_id",
														new Document()
																.append("$ifNull", Arrays.asList("$work.project_id",
																		"$project._id")))),
										new Document().append("$lookup", new Document().append("from", "project")
												.append("localField", "project_id").append("foreignField", "_id")
												.append("as", "project")),
										new Document().append("$unwind", new Document().append("path", "$project")),
										new Document().append("$project", new Document().append("id", true)
												.append("budget",
														new Document().append("$ifNull", Arrays.asList("$budget", 0.0)))
												.append("cost",
														new Document().append("$ifNull", Arrays.asList("$cost", 0.0)))
												.append("project_id", true)),
										new Document().append("$match",
												new Document().append("$expr",
														new Document().append("$and",
																Arrays.asList(new Document().append("$in",
																		Arrays.asList("$project_id", "$$project_id")),
																		new Document())))),
										new Document().append("$group", new Document()
												.append("_id",
														new Document().append("$substr",
																Arrays.asList("$id", 0.0, 4.0)))
												.append("budget", new Document().append("$sum", "$budget"))
												.append("cost", new Document().append("$sum", "$cost"))),
										new Document()
												.append("$addFields", new Document().append("exponent",
														new Document().append("$subtract", Arrays.asList(new Document()
																.append("$toInt", "$_id"),
																new Document().append("$toInt", "$$nowYear"))))),
										new Document().append("$addFields", new Document()
												.append("presentBudget", new Document().append("$cond", Arrays.asList(
														new Document().append("$gte", Arrays.asList("$exponent", 0.0)),
														new Document().append("$divide",
																Arrays.asList("$budget",
																		new Document().append("$pow", Arrays.asList(
																				new Document().append(
																						"$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document().append("$abs",
																						"$exponent"))))),
														new Document().append("$multiply",
																Arrays.asList("$budget",
																		new Document().append("$pow", Arrays.asList(
																				new Document().append("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document().append("$abs",
																						"$exponent"))))))))
												.append("presentCost", new Document().append("$cond", Arrays.asList(
														new Document().append("$gte", Arrays.asList("$exponent", 0.0)),
														new Document().append("$divide",
																Arrays.asList("$cost",
																		new Document().append("$pow", Arrays.asList(
																				new Document().append(
																						"$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document().append("$abs",
																						"$exponent"))))),
														new Document().append("$multiply",
																Arrays.asList("$cost",
																		new Document().append("$pow", Arrays.asList(
																				new Document().append("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document().append("$abs",
																						"$exponent"))))))))),
										new Document().append("$sort", new Document().append("_id", 1.0))))
								.append("as", "cbsYearData")),
				new Document()
						.append("$lookup",
								new Document().append("from", "salesMonthData")
										.append("let",
												new Document().append("project_id", "$project_id")
														.append("discountRate",
																"$discountRate")
														.append("nowYear", "$nowYear").append("sDate", "$sDate"))
										.append("pipeline", Arrays.asList(
												new Document().append("$match",
														new Document().append("$expr",
																new Document().append("$and", Arrays.asList(
																		new Document().append("$in",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document().append("$lte",
																				Arrays.asList(
																						new Document().append("$concat",
																								Arrays.asList("$GJAHR",
																										"$PERDE")),
																						"$$sDate")))))),
												new Document().append("$group", new Document().append("_id", "$GJAHR")
														.append("revenue", new Document().append("$sum", "$revenue"))
														.append("cost",
																new Document().append("$sum", "$cost"))
														.append("profit", new Document().append("$sum", "$profit"))),
												new Document().append("$addFields", new Document().append("exponent",
														new Document().append("$subtract",
																Arrays.asList(new Document().append("$toInt", "$_id"),
																		new Document().append("$toInt",
																				"$$nowYear"))))),
												new Document().append("$addFields", new Document().append(
														"presentRevenue",
														new Document().append("$cond", Arrays.asList(
																new Document().append("$gte",
																		Arrays.asList("$exponent", 0.0)),
																new Document().append("$divide",
																		Arrays.asList("$revenue", new Document().append(
																				"$pow",
																				Arrays.asList(new Document().append(
																						"$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																						new Document().append("$abs",
																								"$exponent"))))),
																new Document().append("$multiply", Arrays.asList(
																		"$revenue", new Document().append("$pow", Arrays
																				.asList(new Document().append("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																						new Document().append("$abs",
																								"$exponent"))))))))
														.append("presentCost", new Document().append("$cond", Arrays
																.asList(new Document().append("$gte",
																		Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$cost",
																				new Document().append("$pow", Arrays
																						.asList(new Document()
																								.append("$add", Arrays
																										.asList(1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply", Arrays
																				.asList("$cost", new Document().append(
																						"$pow",
																						Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))
														.append("presentProfit", new Document().append("$cond", Arrays
																.asList(new Document().append("$gte",
																		Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$profit",
																				new Document().append("$pow", Arrays
																						.asList(new Document()
																								.append("$add", Arrays
																										.asList(1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply",
																				Arrays.asList("$profit", new Document()
																						.append("$pow", Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))),
												new Document().append("$group", new Document().append("_id", null)
														.append("revenue", new Document().append("$sum", "$revenue"))
														.append("cost", new Document().append("$sum", "$cost"))
														.append("profit", new Document().append("$sum", "$profit"))
														.append("presentRevenue",
																new Document().append("$sum", "$presentRevenue"))
														.append("presentCost",
																new Document().append("$sum", "$presentCost"))
														.append("presentProfit", new Document().append("$sum",
																"$presentProfit"))
														.append("avgProfit",
																new Document().append("$avg", "$profit"))
														.append("presentAvgProfit",
																new Document().append("$avg", "$presentProfit"))),
												new Document().append("$addFields", new Document()
														.append("profitRate",
																new Document().append("$cond", Arrays.asList(
																		new Document().append("$eq", Arrays.asList(
																				new Document().append("$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																				0.0)),
																		0.0,
																		new Document().append("$divide",
																				Arrays.asList("$profit", "$revenue")))))
														.append("presentProfitRate", new Document()
																.append("$cond", Arrays.asList(
																		new Document().append("$eq", Arrays
																				.asList(new Document().append(
																						"$ifNull",
																						Arrays.asList(
																								"$presentRevenue",
																								0.0)),
																						0.0)),
																		0.0,
																		new Document().append("$divide",
																				Arrays.asList("$presentProfit",
																						"$presentRevenue"))))))))
										.append("as", "salesRealityTotalData")),
				new Document().append("$unwind", new Document().append("path", "$salesRealityTotalData").append(
						"preserveNullAndEmptyArrays", true)),
				new Document().append("$lookup", new Document().append("from", "cbsSubject").append("let",
						new Document().append("project_id", "$project_id").append("discountRate", "$discountRate")
								.append("nowYear", "$nowYear"))
						.append("pipeline", Arrays.asList(
								new Document().append("$lookup",
										new Document().append("from", "cbs").append("localField", "cbsItem_id")
												.append("foreignField", "_id").append("as", "cbs")),
								new Document().append("$unwind", "$cbs"), new Document().append("$graphLookup",
										new Document().append("from", "cbs").append("startWith", "$cbs._id").append(
												"connectFromField", "parent_id").append("connectToField", "_id")
												.append("as", "cbs")),
								new Document().append("$lookup",
										new Document().append("from", "work").append("localField", "cbs.scope_id")
												.append("foreignField", "_id").append("as", "work")),
								new Document().append("$unwind",
										new Document().append("path", "$work").append("preserveNullAndEmptyArrays",
												true)),
								new Document()
										.append("$lookup",
												new Document().append("from", "project")
														.append("localField", "cbs.scope_id").append("foreignField",
																"_id")
														.append("as", "project")),
								new Document().append("$unwind",
										new Document().append("path", "$project").append("preserveNullAndEmptyArrays",
												true)),
								new Document().append("$addFields", new Document()
										.append("project_id",
												new Document()
														.append("$ifNull", Arrays.asList(
																"$work.project_id", "$project._id")))),
								new Document().append("$lookup", new Document().append("from", "project").append(
										"localField", "project_id").append("foreignField", "_id").append("as",
												"project")),
								new Document().append("$unwind", new Document().append("path", "$project")),
								new Document().append("$project", new Document().append("id", true).append("budget",
										new Document().append("$ifNull", Arrays.asList("$budget", 0.0))).append("cost",
												new Document().append("$ifNull", Arrays.asList("$cost", 0.0)))
										.append("project_id", true)),
								new Document().append("$match",
										new Document().append("$expr", new Document().append("$and",
												Arrays.asList(new Document().append("$in",
														Arrays.asList("$project_id", "$$project_id")),
														new Document())))),
								new Document().append("$group",
										new Document()
												.append("_id",
														new Document().append("$substr",
																Arrays.asList("$id", 0.0, 4.0)))
												.append("budget", new Document().append("$sum", "$budget"))
												.append("cost", new Document().append("$sum", "$cost"))),
								new Document().append("$addFields",
										new Document().append("exponent", new Document()
												.append("$subtract", Arrays
														.asList(new Document()
																.append("$toInt", "$_id"),
																new Document().append("$toInt", "$$nowYear"))))),
								new Document().append("$addFields", new Document()
										.append("presentBudget",
												new Document().append("$cond", Arrays.asList(
														new Document().append("$gte", Arrays.asList("$exponent", 0.0)),
														new Document().append("$divide",
																Arrays.asList("$budget",
																		new Document().append("$pow", Arrays.asList(
																				new Document().append(
																						"$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document().append("$abs",
																						"$exponent"))))),
														new Document().append("$multiply",
																Arrays.asList("$budget",
																		new Document().append("$pow", Arrays.asList(
																				new Document().append("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document().append("$abs",
																						"$exponent"))))))))
										.append("presentCost",
												new Document().append("$cond", Arrays.asList(
														new Document().append("$gte", Arrays.asList("$exponent", 0.0)),
														new Document().append("$divide",
																Arrays.asList("$cost",
																		new Document().append("$pow", Arrays.asList(
																				new Document().append(
																						"$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document().append("$abs",
																						"$exponent"))))),
														new Document().append("$multiply",
																Arrays.asList("$cost",
																		new Document().append("$pow", Arrays.asList(
																				new Document().append("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document().append("$abs",
																						"$exponent"))))))))),
								new Document().append("$group",
										new Document().append("_id", null)
												.append("budget", new Document().append("$sum", "$budget"))
												.append("cost", new Document().append("$sum", "$cost"))
												.append("presentBudget",
														new Document().append("$sum", "$presentBudget"))
												.append("presentCost", new Document().append("$sum", "$presentCost")))))
						.append("as", "cbsTotalData")),
				new Document().append("$unwind", new Document().append("path", "$cbsTotalData").append(
						"preserveNullAndEmptyArrays", true)),
				new Document()
						.append("$lookup",
								new Document().append("from", "salesforecast")
										.append("let", new Document().append("project_id", "$project_id")
												.append("discountRate", "$discountRate").append("nowYear", "$nowYear"))
										.append("pipeline", Arrays.asList(
												new Document().append("$match", new Document().append("$expr",
														new Document().append("$and", Arrays.asList(new Document()
																.append("$in",
																		Arrays.asList("$project_id",
																				"$$project_id")))))),
												new Document().append("$group", new Document().append("_id", "$GJAHR")
														.append("revenue", new Document().append("$sum", "$VV010"))
														.append("cost", new Document().append("$sum",
																new Document().append("$add",
																		Arrays.asList("$VV030", "$VV040"))))
														.append("profit", new Document().append("$sum", new Document()
																.append("$subtract", Arrays.asList("$VV010",
																		new Document().append("$add",
																				Arrays.asList("$VV030", "$VV040"))))))),
												new Document().append("$addFields", new Document().append("exponent",
														new Document().append("$subtract", Arrays
																.asList(new Document().append("$toInt", "$_id"),
																		new Document().append("$toInt",
																				"$$nowYear"))))),
												new Document().append("$addFields", new Document()
														.append("presentRevenue", new Document().append("$cond",
																Arrays.asList(new Document()
																		.append("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$revenue",
																				new Document().append("$pow", Arrays
																						.asList(new Document().append(
																								"$add",
																								Arrays.asList(1.0,
																										"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply",
																				Arrays.asList("$revenue", new Document()
																						.append("$pow", Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))
														.append("presentCost",
																new Document().append("$cond", Arrays.asList(
																		new Document().append("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$cost",
																				new Document().append("$pow", Arrays
																						.asList(new Document().append(
																								"$add",
																								Arrays.asList(1.0,
																										"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply", Arrays
																				.asList("$cost", new Document().append(
																						"$pow",
																						Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))
														.append("presentProfit", new Document().append("$cond", Arrays
																.asList(new Document()
																		.append("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document().append("$divide", Arrays.asList(
																				"$profit",
																				new Document().append("$pow", Arrays
																						.asList(new Document().append(
																								"$add",
																								Arrays.asList(1.0,
																										"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))),
																		new Document().append("$multiply",
																				Arrays.asList("$profit", new Document()
																						.append("$pow", Arrays.asList(
																								new Document().append(
																										"$add",
																										Arrays.asList(
																												1.0,
																												"$$discountRate")),
																								new Document().append(
																										"$abs",
																										"$exponent"))))))))),
												new Document().append("$group", new Document().append("_id", null)
														.append("revenue", new Document().append("$sum", "$revenue"))
														.append("cost", new Document().append("$sum", "$cost"))
														.append("profit", new Document().append("$sum", "$profit"))
														.append("presentRevenue",
																new Document().append("$sum", "$presentRevenue"))
														.append("presentCost",
																new Document().append("$sum", "$presentCost"))
														.append("presentProfit",
																new Document().append("$sum", "$presentProfit"))
														.append("avgProfit", new Document()
																.append("$avg", "$profit"))
														.append("presentAvgProfit",
																new Document().append("$avg", "$presentProfit"))),
												new Document().append("$addFields", new Document()
														.append("profitRate",
																new Document().append("$cond", Arrays.asList(
																		new Document().append("$eq", Arrays.asList(
																				new Document().append("$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																				0.0)),
																		0.0,
																		new Document().append(
																				"$divide",
																				Arrays.asList("$profit", "$revenue")))))
														.append("presentProfitRate", new Document()
																.append("$cond", Arrays.asList(
																		new Document().append("$eq", Arrays
																				.asList(new Document().append(
																						"$ifNull",
																						Arrays.asList(
																								"$presentRevenue",
																								0.0)),
																						0.0)),
																		0.0,
																		new Document().append("$divide",
																				Arrays.asList("$presentProfit",
																						"$presentRevenue"))))))))
										.append("as", "salesForecastTotalData")),
				new Document().append("$unwind", new Document().append("path", "$salesForecastTotalData").append(
						"preserveNullAndEmptyArrays", true)),
				new Document().append("$lookup",
						new Document().append("from", "salesforecast")
								.append("let",
										new Document().append("project_id", "$project_id")
												.append("discountRate", "$discountRate").append("nowYear", "$nowYear"))
								.append("pipeline",
										Arrays.asList(
												new Document().append(
														"$match",
														new Document().append("$expr", new Document().append(
																"$and",
																Arrays.asList(new Document().append("$in",
																		Arrays.asList("$project_id",
																				"$$project_id")))))),
												new Document().append("$group", new Document().append("_id", null)
														.append("revenue", new Document().append("$sum", "$VV010"))
														.append("cost",
																new Document().append(
																		"$sum",
																		new Document().append(
																				"$add",
																				Arrays.asList("$VV030", "$VV040"))))
														.append("profit",
																new Document().append("$sum",
																		new Document().append("$subtract",
																				Arrays.asList("$VV010",
																						new Document().append("$add",
																								Arrays.asList("$VV030",
																										"$VV040"))))))),
												new Document().append("$addFields", new Document().append("profitRate",
														new Document().append("$cond", Arrays.asList(
																new Document().append("$eq",
																		Arrays.asList(new Document().append("$ifNull",
																				Arrays.asList("$revenue", 0.0)), 0.0)),
																0.0,
																new Document().append("$divide",
																		Arrays.asList("$profit", "$revenue")))))),
												new Document().append("$sort", new Document().append("_id", 1.0))))
								.append("as", "salesForecastMonthData")),
				new Document().append("$unwind", new Document().append("path", "$salesForecastMonthData").append(
						"preserveNullAndEmptyArrays", true)),
				new Document().append("$lookup", new Document()
						.append("from",
								"salesMonthData")
						.append("let",
								new Document().append("project_id", "$project_id").append(
										"discountRate", "$discountRate").append("nowYear", "$nowYear")
										.append("sDate", "$sDate"))
						.append("pipeline",
								Arrays.asList(
										new Document().append("$match", new Document().append("$expr",
												new Document().append("$and", Arrays.asList(new Document().append("$in",
														Arrays.asList("$project_id", "$$project_id")),
														new Document()
																.append("$eq",
																		Arrays.asList(
																				new Document().append("$concat",
																						Arrays.asList("$GJAHR",
																								"$PERDE")),
																				"$$sDate")))))),
										new Document().append("$group",
												new Document().append("_id", null)
														.append("revenue", new Document().append("$sum", "$revenue"))
														.append("cost",
																new Document().append("$sum", "$cost"))
														.append("profit", new Document().append("$sum", "$profit"))),
										new Document().append("$addFields", new Document().append("profitRate",
												new Document().append("$cond", Arrays.asList(
														new Document().append("$eq",
																Arrays.asList(new Document().append("$ifNull",
																		Arrays.asList("$revenue", 0.0)), 0.0)),
														0.0,
														new Document().append("$divide",
																Arrays.asList("$profit", "$revenue"))))))))
						.append("as", "salesRealityMonthData")),
				new Document().append("$unwind", new Document().append("path", "$salesRealityMonthData").append(
						"preserveNullAndEmptyArrays", true)),
				new Document().append("$lookup",
						new Document()
								.append("from", "cbsSubject").append(
										"let",
										new Document()
												.append("project_id", "$_id").append("discountRate", "$discountRate")
												.append("nowYear", "$nowYear"))
								.append("pipeline", Arrays.asList(new Document().append("$lookup",
										new Document().append("from", "cbs").append("localField", "cbsItem_id")
												.append("foreignField", "_id").append("as", "cbs")),
										new Document().append("$unwind", "$cbs"), new Document().append("$graphLookup",
												new Document().append("from", "cbs").append("startWith", "$cbs._id")
														.append("connectFromField", "parent_id")
														.append("connectToField", "_id").append("as", "cbs")),
										new Document().append("$lookup",
												new Document().append("from", "work")
														.append("localField", "cbs.scope_id")
														.append("foreignField", "_id").append("as", "work")),
										new Document().append("$unwind",
												new Document().append("path", "$work")
														.append("preserveNullAndEmptyArrays", true)),
										new Document().append("$lookup",
												new Document().append("from", "project")
														.append("localField", "cbs.scope_id")
														.append("foreignField", "_id").append("as", "project")),
										new Document().append("$unwind",
												new Document().append("path", "$project")
														.append("preserveNullAndEmptyArrays", true)),
										new Document().append("$addFields",
												new Document().append("project_id", new Document().append("$ifNull",
														Arrays.asList("$work.project_id", "$project._id")))),
										new Document().append("$lookup",
												new Document().append("from", "project")
														.append("localField", "project_id")
														.append("foreignField", "_id").append("as", "project")),
										new Document().append("$unwind", new Document().append("path", "$project")),
										new Document().append("$project", new Document().append("id", true)
												.append("budget",
														new Document().append("$ifNull", Arrays.asList("$budget", 0.0)))
												.append("cost",
														new Document().append("$ifNull", Arrays.asList("$cost", 0.0)))
												.append("project_id", true)),
										new Document().append("$match",
												new Document().append("$expr", new Document().append("$and",
														Arrays.asList(new Document().append("$eq",
																Arrays.asList("$project_id", "$$project_id")))))),
										new Document().append("$group",
												new Document().append("_id", null)
														.append("budget", new Document().append("$sum", "$budget"))
														.append("cost", new Document().append("$sum", "$cost"))),
										new Document().append("$sort", new Document().append("_id", 1.0))))
								.append("as", "cbsMonthData")),
				new Document().append("$unwind", new Document().append("path", "$cbsMonthData").append(
						"preserveNullAndEmptyArrays", true)),
				new Document().append("$project", new Document().append("eps_id", "$_id")
						.append("forecastYearAvgProfit",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastTotalData.avgProfit", 0.0)))
						.append("forecastYearPresentAvgProfit",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastTotalData.presentAvgProfit", 0.0)))
						.append("forecastPresentAvgProfit",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastTotalData.presentAvgProfit", 0.0)))
						.append("forecastRevenue",
								new Document().append("$ifNull", Arrays.asList("$salesForecastTotalData.revenue", 0.0)))
						.append("forecastCost",
								new Document().append("$ifNull", Arrays.asList("$salesForecastTotalData.cost", 0.0)))
						.append("forecastProfit",
								new Document().append("$ifNull", Arrays.asList("$salesForecastTotalData.profit", 0.0)))
						.append("forecastPresentRevenue",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastTotalData.presentRevenue", 0.0)))
						.append("forecastPresentCost",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastTotalData.presentCost", 0.0)))
						.append("forecastPresentProfit",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastTotalData.presentProfit", 0.0)))
						.append("forecastProfitRate",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastTotalData.profitRate", 0.0)))
						.append("forecastPresentProfitRate",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastTotalData.presentProfitRate", 0.0)))
						.append("forecastMonthRevenue",
								new Document().append("$ifNull", Arrays.asList("$salesForecastMonthData.revenue", 0.0)))
						.append("forecastMonthCost",
								new Document().append("$ifNull", Arrays.asList("$salesForecastMonthData.cost", 0.0)))
						.append("forecastMonthProfit",
								new Document().append("$ifNull", Arrays.asList("$salesForecastMonthData.profit", 0.0)))
						.append("forecastMonthProfitRate",
								new Document().append("$ifNull",
										Arrays.asList("$salesForecastMonthData.profitRate", 0.0)))
						.append("realityYearAvgProfit",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityTotalData.avgProfit", 0.0)))
						.append("realityYearPresentAvgProfit",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityTotalData.presentAvgProfit", 0.0)))
						.append("realityPresentAvgProfit",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityTotalData.presentAvgProfit", 0.0)))
						.append("realityRevenue",
								new Document().append("$ifNull", Arrays.asList("$salesRealityTotalData.revenue", 0.0)))
						.append("realityCost",
								new Document().append("$ifNull", Arrays.asList("$salesRealityTotalData.cost", 0.0)))
						.append("realityProfit",
								new Document().append("$ifNull", Arrays.asList("$salesRealityTotalData.profit", 0.0)))
						.append("realityPresentRevenue",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityTotalData.presentRevenue", 0.0)))
						.append("realityPresentCost",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityTotalData.presentCost", 0.0)))
						.append("realityPresentProfit",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityTotalData.presentProfit", 0.0)))
						.append("realityProfitRate",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityTotalData.profitRate", 0.0)))
						.append("realityPresentProfitRate",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityTotalData.presentProfitRate", 0.0)))
						.append("realityMonthRevenue",
								new Document().append("$ifNull", Arrays.asList("$salesRealityMonthData.revenue", 0.0)))
						.append("realityMonthCost",
								new Document().append("$ifNull", Arrays.asList("$salesRealityMonthData.cost", 0.0)))
						.append("realityMonthProfit",
								new Document().append("$ifNull", Arrays.asList("$salesRealityMonthData.profit", 0.0)))
						.append("realitytMonthProfitRate",
								new Document().append("$ifNull",
										Arrays.asList("$salesRealityMonthData.profitRate", 0.0)))
						.append("budget", new Document().append("$ifNull", Arrays.asList("$cbsTotalData.budget", 0.0)))
						.append("cost", new Document().append("$ifNull", Arrays.asList("$cbsTotalData.cost", 0.0)))
						.append("presentBudget",
								new Document().append("$ifNull", Arrays.asList("$cbsTotalData.presentBudget", 0.0)))
						.append("presentCost",
								new Document().append("$ifNull", Arrays.asList("$cbsTotalData.presentCost", 0.0)))
						.append("monthBudget",
								new Document().append("$ifNull", Arrays.asList("$cbsMonthData.budget", 0.0)))
						.append("monthCost", new Document().append("$ifNull", Arrays.asList("$cbsMonthData.cost", 0.0)))
						.append("salesForecastYearData", true).append("salesRealityYearData", true)
						.append("cbsYearData", true)),
				new Document()
						.append("$addFields",
								new Document()
										.append("forecastPP",
												new Document().append("$cond",
														Arrays.asList(
																new Document().append("$eq",
																		Arrays.asList("$forecastYearAvgProfit", 0.0)),
																0.0,
																new Document().append("$multiply", Arrays.asList(
																		new Document().append("$divide",
																				Arrays.asList("$budget",
																						"$forecastYearAvgProfit")),
																		12.0)))))
										.append("forecastPresentPP",
												new Document().append("$cond", Arrays.asList(
														new Document().append("$eq",
																Arrays.asList("$forecastYearPresentAvgProfit", 0.0)),
														0.0,
														new Document().append("$multiply", Arrays.asList(
																new Document().append("$divide",
																		Arrays.asList("$presentBudget",
																				"$forecastYearPresentAvgProfit")),
																12.0)))))
										.append("realityPP",
												new Document().append("$cond",
														Arrays.asList(
																new Document().append("$eq",
																		Arrays.asList("$realityYearAvgProfit", 0.0)),
																0.0,
																new Document().append("$multiply", Arrays.asList(
																		new Document().append("$divide",
																				Arrays.asList("$cost",
																						"$realityYearAvgProfit")),
																		12.0)))))
										.append("realityPresentPP",
												new Document().append("$cond", Arrays.asList(
														new Document().append("$eq",
																Arrays.asList("$realityYearPresentAvgProfit", 0.0)),
														0.0,
														new Document().append("$multiply", Arrays.asList(
																new Document().append("$divide",
																		Arrays.asList("$presentCost",
																				"$realityYearPresentAvgProfit")),
																12.0)))))
										.append("forecastROI",
												new Document().append("$cond", Arrays.asList(
														new Document().append("$eq", Arrays.asList("$budget", 0.0)),
														0.0,
														new Document().append("$divide",
																Arrays.asList("$forecastYearAvgProfit", "$budget")))))
										.append("forecastPresentROI", new Document().append("$cond", Arrays.asList(
												new Document().append("$eq", Arrays.asList("$presentBudget", 0.0)), 0.0,
												new Document().append("$divide",
														Arrays.asList("$forecastYearPresentAvgProfit",
																"$presentBudget")))))
										.append("realityROI",
												new Document().append("$cond", Arrays.asList(
														new Document().append("$eq", Arrays.asList("$cost", 0.0)), 0.0,
														new Document().append("$divide",
																Arrays.asList("$realityYearAvgProfit", "$cost")))))
										.append("realityPresentROI", new Document().append("$cond", Arrays.asList(
												new Document().append("$eq", Arrays.asList("$presentCost", 0.0)), 0.0,
												new Document().append("$divide",
														Arrays.asList("$realityYearPresentAvgProfit",
																"$presentCost")))))
										.append("forecastNPV",
												new Document().append("$add",
														Arrays.asList(
																new Document().append("$multiply",
																		Arrays.asList(-1.0, "$presentBudget")),
																"$forecastPresentProfit")))
										.append("realityNPV",
												new Document().append("$add",
														Arrays.asList(
																new Document().append("$multiply",
																		Arrays.asList(-1.0, "$presentCost")),
																"$realityPresentProfit")))
										.append("forecastNPVR",
												new Document().append("$cond", Arrays.asList(
														new Document()
																.append("$eq", Arrays.asList("$presentBudget", 0.0)),
														0.0,
														new Document().append("$divide", Arrays.asList(
																new Document().append("$add",
																		Arrays.asList(
																				new Document().append("$multiply",
																						Arrays.asList(-1.0,
																								"$presentBudget")),
																				"$forecastPresentProfit")),
																"$presentBudget")))))
										.append("realityNPVR", new Document().append("$cond", Arrays.asList(
												new Document().append("$eq", Arrays.asList("$presentCost", 0.0)), 0.0,
												new Document().append("$divide",
														Arrays.asList(
																new Document().append("$add",
																		Arrays.asList(
																				new Document().append("$multiply",
																						Arrays.asList(-1.0,
																								"$presentCost")),
																				"$realityPresentProfit")),
																"$presentCost")))))));
		List<Document> epsMonthDatas = new ArrayList<Document>();
		prjColl.aggregate(pipeline).map(doc -> {
			doc.append("eps_id", doc.get("_id"));
			doc.append("_id", new ObjectId());
			Map<String, Double> forecastOutFlowMap = new LinkedHashMap<String, Double>();
			Map<String, Double> realityOutFlowMap = new LinkedHashMap<String, Double>();
			Map<String, Double> forecastCashFlowMap = new LinkedHashMap<String, Double>();
			Map<String, Double> realityCashFlowMap = new LinkedHashMap<String, Double>();
			Check.isAssigned((List<Document>) doc.get("cbsYearData"), l -> l.forEach(d -> {
				String key = d.getString("_id");
				Double budget = Formatter.getDouble(d, "budget");
				Double cost = Formatter.getDouble(d, "cost");
				forecastOutFlowMap.put(key, -1 * budget);
				forecastCashFlowMap.put(key, 0d);
				realityOutFlowMap.put(key, -1 * cost);
				realityCashFlowMap.put(key, 0d);
			}));
			Check.isAssigned((List<Document>) doc.get("salesRealityYearData"), l -> l.forEach(d -> {
				String key = d.getString("_id");
				Double profit = Formatter.getDouble(d, "profit");
				Double value = realityCashFlowMap.get(key);
				if (value != null)
					realityCashFlowMap.put(key, value + profit);
				else
					realityCashFlowMap.put(key, profit);
			}));
			Check.isAssigned((List<Document>) doc.get("salesForecastYearData"), l -> l.forEach(d -> {
				String key = d.getString("_id");
				Double profit = Formatter.getDouble(d, "profit");
				Double value = forecastCashFlowMap.get(key);
				if (value != null)
					forecastCashFlowMap.put(key, value + profit);
				else
					forecastCashFlowMap.put(key, profit);
			}));

			doc.append("forecastIRR", getIrr(forecastOutFlowMap, forecastCashFlowMap));
			doc.append("realityIRR", getIrr(realityOutFlowMap, realityCashFlowMap));
			return doc.append("year", year).append("month", month);
		}).into(epsMonthDatas);

		// 清除之前写入的月销售数据
		eMDCol.deleteMany(new BasicDBObject("year", year).append("month", month));

		// 插入月销售数据
		if (epsMonthDatas.size() > 0)
			eMDCol.insertMany(epsMonthDatas);
	}

}
