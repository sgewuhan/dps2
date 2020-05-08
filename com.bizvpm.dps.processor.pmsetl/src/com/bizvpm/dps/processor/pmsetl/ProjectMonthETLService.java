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

public class ProjectMonthETLService extends AbstractMonthETLService {

	@Override
	public ProcessResult run(ProcessTask pT, IProgressMonitor monitor, IProcessContext context) throws Exception {
		MongoDatabase db = Domain.getDatabase((String) pT.get("domain"));
		String year = (String) pT.get("year");
		String month = (String) pT.get("month");

		MongoCollection<Document> prjColl = db.getCollection("project");
		MongoCollection<Document> pMDCol = db.getCollection("projectMonthData");
		Double discountRate = getDiscountRate(db);

		createProjectMonthData(prjColl, pMDCol, discountRate, year, month);
		ProcessResult result = new ProcessResult();
		result.put("result", "");
		return result;
	}

	/**
	 * 构建项目月绩效数据
	 * 
	 * @param prjColl
	 * @param pMDCol
	 * @param year
	 * @param month
	 */
	@SuppressWarnings("unchecked")
	private void createProjectMonthData(MongoCollection<Document> prjColl, MongoCollection<Document> pMDCol,
			Double discountRate, String year, String month) {
		// 获取项目月绩效数据
		List<Bson> pipeline = Arrays
				.asList(new Document("$addFields", new Document("discountRate", discountRate).append("nowYear", year)
						.append("sDate", year + month)),
						new Document("$lookup", new Document("from", "salesforecast")
								.append("let", new Document("project_id", "$_id").append("discountRate",
										"$discountRate").append("nowYear", "$nowYear")
										.append("sDate", "$sDate"))
								.append("pipeline",
										Arrays.asList(
												new Document("$match", new Document("$expr", new Document("$and", Arrays
														.asList(new Document("$eq", Arrays.asList("$project_id",
																"$$project_id")), new Document("$lte",
																		Arrays.asList(
																				new Document("$concat",
																						Arrays.asList("$GJAHR",
																								"$PERDE")),
																				"$$sDate")))))),
												new Document("$group", new Document("_id", "$GJAHR").append("revenue",
														new Document("$sum", "$VV010"))
														.append("cost",
																new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
														.append("profit", new Document("$sum", new Document("$subtract",
																Arrays.asList("$VV010",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))))),
												new Document("$addFields",
														new Document("exponent",
																new Document("$subtract",
																		Arrays.asList(new Document("$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
												new Document("$addFields", new Document("presentRevenue",
														new Document("$cond", Arrays.asList(
																new Document("$gte", Arrays.asList("$exponent", 0.0)),
																new Document("$divide", Arrays.asList("$revenue",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))),
																new Document("$multiply", Arrays.asList("$revenue",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))))))
																						.append("presentCost",
																								new Document("$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))
																						.append("presentProfit",
																								new Document("$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$profit",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$profit",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))),
												new Document("$addFields",
														new Document("profitRate", new Document("$cond", Arrays.asList(
																new Document("$eq",
																		Arrays.asList(new Document("$ifNull",
																				Arrays.asList("$revenue", 0.0)), 0.0)),
																0.0,
																new Document("$divide",
																		Arrays.asList("$profit", "$revenue"))))).append(
																				"presentProfitRate",
																				new Document("$cond", Arrays.asList(
																						new Document("$eq", Arrays
																								.asList(new Document(
																										"$ifNull",
																										Arrays.asList(
																												"$presentRevenue",
																												0.0)),
																										0.0)),
																						0.0,
																						new Document("$divide",
																								Arrays.asList(
																										"$presentProfit",
																										"$presentRevenue")))))),
												new Document("$sort", new Document("_id", 1.0))))
								.append("as", "salesForecastYearData")),
						new Document(
								"$lookup",
								new Document("from", "salesMonthData").append("let",
										new Document("project_id", "$_id").append("discountRate", "$discountRate")
												.append("nowYear", "$nowYear").append("sDate", "$sDate"))
										.append("pipeline", Arrays.asList(
												new Document("$match",
														new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$lte", Arrays.asList(
																				new Document("$concat",
																						Arrays.asList("$GJAHR",
																								"$PERDE")),
																				"$$sDate")))))),
												new Document("$group", new Document("_id", "$GJAHR").append("revenue",
														new Document("$sum", "$VV010")).append("cost",
																new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
														.append("profit",
																new Document("$sum",
																		new Document("$subtract", Arrays.asList(
																				"$VV010",
																				new Document("$add",
																						Arrays.asList("$VV030",
																								"$VV040"))))))),
												new Document("$addFields",
														new Document("exponent", new Document("$subtract",
																Arrays.asList(new Document("$toInt", "$_id"),
																		new Document("$toInt", "$$nowYear"))))),
												new Document("$addFields", new Document("presentRevenue",
														new Document("$cond", Arrays.asList(
																new Document("$gte", Arrays.asList("$exponent", 0.0)),
																new Document("$divide", Arrays.asList("$revenue",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))),
																new Document("$multiply", Arrays.asList("$revenue",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))))))
																						.append("presentCost",
																								new Document("$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))
																						.append("presentProfit",
																								new Document(
																										"$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$profit",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$profit",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))),
												new Document("$addFields",
														new Document("profitRate", new Document("$cond", Arrays.asList(
																new Document("$eq",
																		Arrays.asList(new Document(
																				"$ifNull",
																				Arrays.asList("$revenue", 0.0)), 0.0)),
																0.0,
																new Document("$divide",
																		Arrays.asList("$profit", "$revenue"))))).append(
																				"presentProfitRate",
																				new Document("$cond", Arrays.asList(
																						new Document("$eq", Arrays
																								.asList(new Document(
																										"$ifNull",
																										Arrays.asList(
																												"$presentRevenue",
																												0.0)),
																										0.0)),
																						0.0,
																						new Document("$divide",
																								Arrays.asList(
																										"$presentProfit",
																										"$presentRevenue")))))),
												new Document("$sort", new Document("_id", 1.0))))
										.append("as", "salesRealityYearData")),
						new Document("$lookup", new Document("from", "cbsSubject").append("let",
								new Document("project_id", "$_id").append("discountRate", "$discountRate")
										.append("nowYear", "$nowYear"))
								.append("pipeline",
										Arrays.asList(
												new Document("$lookup", new Document("from", "cbs")
														.append("localField", "cbsItem_id").append("foreignField",
																"_id")
														.append("as", "cbs")),
												new Document("$unwind", "$cbs"),
												new Document(
														"$graphLookup",
														new Document("from", "cbs").append("startWith", "$cbs._id")
																.append("connectFromField", "parent_id")
																.append("connectToField", "_id").append("as", "cbs")),
												new Document(
														"$lookup", new Document("from", "work")
																.append("localField", "cbs.scope_id")
																.append("foreignField", "_id").append("as", "work")),
												new Document("$unwind", new Document("path", "$work")
														.append("preserveNullAndEmptyArrays", true)),
												new Document("$lookup", new Document("from", "project")
														.append("localField", "cbs.scope_id")
														.append("foreignField", "_id").append("as", "project")),
												new Document("$unwind",
														new Document("path", "$project")
																.append("preserveNullAndEmptyArrays", true)),
												new Document("$addFields", new Document("project_id",
														new Document("$ifNull",
																Arrays.asList("$work.project_id", "$project._id")))),
												new Document("$lookup",
														new Document("from", "project")
																.append("localField", "project_id").append(
																		"foreignField", "_id")
																.append("as", "project")),
												new Document("$unwind", new Document("path", "$project")),
												new Document("$project", new Document("id", true)
														.append("budget", new Document(
																"$ifNull", Arrays.asList("$budget", 0.0)))
														.append("cost",
																new Document("$ifNull", Arrays.asList("$cost", 0.0)))
														.append("project_id", true)),
												new Document("$match", new Document("$expr",
														new Document("$and",
																Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document())))),
												new Document("$group",
														new Document("_id", new Document(
																"$substr", Arrays.asList("$id", 0.0, 4.0))).append(
																		"budget", new Document("$sum", "$budget"))
																		.append("cost", new Document("$sum", "$cost"))),
												new Document("$addFields",
														new Document("exponent",
																new Document(
																		"$subtract", Arrays.asList(new Document(
																				"$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
												new Document("$addFields", new Document("presentBudget",
														new Document("$cond", Arrays.asList(
																new Document("$gte", Arrays.asList("$exponent", 0.0)),
																new Document("$divide", Arrays.asList("$budget",
																		new Document("$pow", Arrays.asList(
																				new Document(
																						"$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))),
																new Document("$multiply", Arrays.asList("$budget",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))))))
																						.append("presentCost",
																								new Document("$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))),
												new Document("$sort", new Document("_id", 1.0))))
								.append("as", "cbsYearData")),
						new Document(
								"$lookup",
								new Document("from", "salesMonthData").append("let",
										new Document("project_id", "$_id").append("discountRate", "$discountRate")
												.append("nowYear", "$nowYear").append("sDate", "$sDate"))
										.append("pipeline", Arrays.asList(
												new Document("$match",
														new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$lte", Arrays.asList(
																				new Document("$concat",
																						Arrays.asList("$GJAHR",
																								"$PERDE")),
																				"$$sDate")))))),
												new Document("$group", new Document("_id", "$GJAHR").append("revenue",
														new Document("$sum", "$VV010")).append("cost",
																new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
														.append("profit",
																new Document("$sum",
																		new Document("$subtract", Arrays.asList(
																				"$VV010",
																				new Document("$add",
																						Arrays.asList("$VV030",
																								"$VV040"))))))),
												new Document("$addFields",
														new Document("exponent", new Document("$subtract",
																Arrays.asList(new Document("$toInt", "$_id"),
																		new Document("$toInt", "$$nowYear"))))),
												new Document("$addFields", new Document("presentRevenue",
														new Document("$cond", Arrays.asList(
																new Document("$gte", Arrays.asList("$exponent", 0.0)),
																new Document("$divide", Arrays.asList("$revenue",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))),
																new Document("$multiply", Arrays.asList("$revenue",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))))))
																						.append("presentCost",
																								new Document("$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))
																						.append("presentProfit",
																								new Document(
																										"$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$profit",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$profit",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))),
												new Document("$group", new Document("_id", null)
														.append("revenue", new Document("$sum", "$revenue"))
														.append("cost", new Document("$sum", "$cost"))
														.append("profit", new Document("$sum", "$profit"))
														.append("presentRevenue",
																new Document("$sum", "$presentRevenue"))
														.append("presentCost", new Document("$sum", "$presentCost"))
														.append("presentProfit", new Document("$sum", "$presentProfit"))
														.append("avgProfit", new Document("$avg", "$profit"))
														.append("presentAvgProfit",
																new Document("$avg", "$presentProfit"))),
												new Document("$addFields",
														new Document("profitRate", new Document("$cond", Arrays.asList(
																new Document("$eq",
																		Arrays.asList(new Document(
																				"$ifNull",
																				Arrays.asList("$revenue", 0.0)), 0.0)),
																0.0,
																new Document("$divide",
																		Arrays.asList("$profit", "$revenue"))))).append(
																				"presentProfitRate",
																				new Document("$cond", Arrays.asList(
																						new Document("$eq", Arrays
																								.asList(new Document(
																										"$ifNull",
																										Arrays.asList(
																												"$presentRevenue",
																												0.0)),
																										0.0)),
																						0.0,
																						new Document("$divide",
																								Arrays.asList(
																										"$presentProfit",
																										"$presentRevenue"))))))))
										.append("as", "salesRealityTotalData")),
						new Document("$unwind",
								new Document("path", "$salesRealityTotalData").append("preserveNullAndEmptyArrays",
										true)),
						new Document("$lookup", new Document("from", "cbsSubject")
								.append("let",
										new Document("project_id", "$_id").append("discountRate", "$discountRate")
												.append("nowYear", "$nowYear"))
								.append("pipeline",
										Arrays.asList(
												new Document("$lookup", new Document("from", "cbs")
														.append("localField", "cbsItem_id").append("foreignField",
																"_id")
														.append("as", "cbs")),
												new Document("$unwind", "$cbs"),
												new Document(
														"$graphLookup",
														new Document("from", "cbs").append("startWith", "$cbs._id")
																.append("connectFromField", "parent_id")
																.append("connectToField", "_id").append("as", "cbs")),
												new Document(
														"$lookup", new Document("from", "work")
																.append("localField", "cbs.scope_id").append(
																		"foreignField", "_id")
																.append("as", "work")),
												new Document("$unwind", new Document("path", "$work")
														.append("preserveNullAndEmptyArrays", true)),
												new Document("$lookup", new Document("from", "project")
														.append("localField", "cbs.scope_id").append("foreignField",
																"_id")
														.append("as", "project")),
												new Document("$unwind",
														new Document("path", "$project")
																.append("preserveNullAndEmptyArrays", true)),
												new Document("$addFields", new Document("project_id",
														new Document("$ifNull",
																Arrays.asList("$work.project_id", "$project._id")))),
												new Document("$lookup",
														new Document("from", "project").append("localField",
																"project_id").append("foreignField", "_id").append("as",
																		"project")),
												new Document("$unwind", new Document("path", "$project")),
												new Document("$project", new Document("id", true).append("budget",
														new Document("$ifNull", Arrays.asList("$budget", 0.0))).append(
																"cost",
																new Document("$ifNull", Arrays.asList("$cost", 0.0)))
														.append("project_id", true)),
												new Document("$match", new Document("$expr",
														new Document(
																"$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document())))),
												new Document("$group",
														new Document("_id", new Document(
																"$substr", Arrays.asList("$id", 0.0, 4.0)))
																		.append("budget", new Document(
																				"$sum", "$budget"))
																		.append("cost", new Document("$sum", "$cost"))),
												new Document("$addFields",
														new Document("exponent",
																new Document(
																		"$subtract", Arrays.asList(new Document(
																				"$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
												new Document("$addFields", new Document("presentBudget",
														new Document("$cond", Arrays.asList(
																new Document("$gte", Arrays.asList("$exponent", 0.0)),
																new Document("$divide", Arrays.asList("$budget",
																		new Document("$pow", Arrays.asList(
																				new Document(
																						"$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))),
																new Document("$multiply", Arrays.asList("$budget",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))))))
																						.append("presentCost",
																								new Document("$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))),
												new Document("$group", new Document("_id", null)
														.append("budget", new Document("$sum", "$budget"))
														.append("cost", new Document("$sum", "$cost"))
														.append("presentBudget", new Document("$sum", "$presentBudget"))
														.append("presentCost", new Document("$sum", "$presentCost")))))
								.append("as", "cbsTotalData")),
						new Document(
								"$unwind",
								new Document("path", "$cbsTotalData").append("preserveNullAndEmptyArrays", true)),
						new Document(
								"$lookup",
								new Document("from", "salesforecast").append("let",
										new Document("project_id", "$_id").append("discountRate", "$discountRate")
												.append("nowYear", "$nowYear").append("sDate", "$sDate"))
										.append("pipeline", Arrays.asList(
												new Document("$match",
														new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$lte", Arrays.asList(
																				new Document("$concat",
																						Arrays.asList("$GJAHR",
																								"$PERDE")),
																				"$$sDate")))))),
												new Document("$group", new Document("_id", "$GJAHR").append("revenue",
														new Document("$sum", "$VV010")).append("cost",
																new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
														.append("profit",
																new Document("$sum",
																		new Document("$subtract", Arrays.asList(
																				"$VV010",
																				new Document("$add",
																						Arrays.asList("$VV030",
																								"$VV040"))))))),
												new Document("$addFields",
														new Document("exponent", new Document("$subtract",
																Arrays.asList(new Document("$toInt", "$_id"),
																		new Document("$toInt", "$$nowYear"))))),
												new Document("$addFields", new Document("presentRevenue",
														new Document("$cond", Arrays.asList(
																new Document("$gte", Arrays.asList("$exponent", 0.0)),
																new Document("$divide", Arrays.asList("$revenue",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))),
																new Document("$multiply", Arrays.asList("$revenue",
																		new Document("$pow", Arrays.asList(
																				new Document("$add",
																						Arrays.asList(1.0,
																								"$$discountRate")),
																				new Document("$abs", "$exponent"))))))))
																						.append("presentCost",
																								new Document("$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$cost",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))
																						.append("presentProfit",
																								new Document(
																										"$cond",
																										Arrays.asList(
																												new Document(
																														"$gte",
																														Arrays.asList(
																																"$exponent",
																																0.0)),
																												new Document(
																														"$divide",
																														Arrays.asList(
																																"$profit",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))),
																												new Document(
																														"$multiply",
																														Arrays.asList(
																																"$profit",
																																new Document(
																																		"$pow",
																																		Arrays.asList(
																																				new Document(
																																						"$add",
																																						Arrays.asList(
																																								1.0,
																																								"$$discountRate")),
																																				new Document(
																																						"$abs",
																																						"$exponent"))))))))),
												new Document("$group", new Document("_id", null)
														.append("revenue", new Document("$sum", "$revenue"))
														.append("cost", new Document("$sum", "$cost"))
														.append("profit", new Document("$sum", "$profit"))
														.append("presentRevenue",
																new Document("$sum", "$presentRevenue"))
														.append("presentCost", new Document("$sum", "$presentCost"))
														.append("presentProfit", new Document("$sum", "$presentProfit"))
														.append("avgProfit", new Document("$avg", "$profit"))
														.append("presentAvgProfit",
																new Document("$avg", "$presentProfit"))),
												new Document("$addFields",
														new Document("profitRate", new Document("$cond", Arrays.asList(
																new Document("$eq",
																		Arrays.asList(new Document(
																				"$ifNull",
																				Arrays.asList("$revenue", 0.0)), 0.0)),
																0.0,
																new Document("$divide",
																		Arrays.asList("$profit", "$revenue"))))).append(
																				"presentProfitRate",
																				new Document("$cond", Arrays.asList(
																						new Document("$eq", Arrays
																								.asList(new Document(
																										"$ifNull",
																										Arrays.asList(
																												"$presentRevenue",
																												0.0)),
																										0.0)),
																						0.0,
																						new Document("$divide",
																								Arrays.asList(
																										"$presentProfit",
																										"$presentRevenue"))))))))
										.append("as", "salesForecastTotalData")),
						new Document("$unwind",
								new Document("path", "$salesForecastTotalData").append("preserveNullAndEmptyArrays",
										true)),
						new Document("$lookup", new Document("from", "salesforecast")
								.append("let",
										new Document("project_id", "$_id").append("discountRate", "$discountRate")
												.append("nowYear", "$nowYear").append("sDate", "$sDate"))
								.append("pipeline",
										Arrays.asList(
												new Document("$match",
														new Document("$expr", new Document("$and", Arrays.asList(
																new Document("$eq",
																		Arrays.asList("$project_id", "$$project_id")),
																new Document("$eq",
																		Arrays.asList(
																				new Document("$concat",
																						Arrays.asList("$GJAHR",
																								"$PERDE")),
																				"$$sDate")))))),
												new Document("$group", new Document("_id", null)
														.append("revenue", new Document("$sum", "$VV010"))
														.append("cost",
																new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
														.append("profit", new Document("$sum",
																new Document("$subtract", Arrays.asList("$VV010",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))))),
												new Document("$addFields",
														new Document("profitRate", new Document("$cond", Arrays.asList(
																new Document("$eq",
																		Arrays.asList(new Document("$ifNull",
																				Arrays.asList("$revenue", 0.0)), 0.0)),
																0.0,
																new Document("$divide",
																		Arrays.asList("$profit", "$revenue")))))),
												new Document("$sort", new Document("_id", 1.0))))
								.append("as", "salesForecastMonthData")),
						new Document("$unwind",
								new Document("path", "$salesForecastMonthData").append("preserveNullAndEmptyArrays",
										true)),
						new Document("$lookup", new Document("from", "salesMonthData")
								.append("let",
										new Document("project_id", "$_id").append("discountRate", "$discountRate")
												.append("nowYear", "$nowYear").append("sDate", "$sDate"))
								.append("pipeline",
										Arrays.asList(
												new Document("$match",
														new Document("$expr", new Document("$and", Arrays.asList(
																new Document("$eq",
																		Arrays.asList("$project_id", "$$project_id")),
																new Document(
																		"$eq",
																		Arrays.asList(
																				new Document("$concat",
																						Arrays.asList("$GJAHR",
																								"$PERDE")),
																				"$$sDate")))))),
												new Document("$group", new Document("_id", null)
														.append("revenue", new Document("$sum", "$VV010"))
														.append("cost",
																new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
														.append("profit", new Document("$sum",
																new Document("$subtract", Arrays.asList("$VV010",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))))),
												new Document("$addFields",
														new Document("profitRate", new Document("$cond", Arrays.asList(
																new Document("$eq",
																		Arrays.asList(new Document("$ifNull",
																				Arrays.asList("$revenue", 0.0)), 0.0)),
																0.0,
																new Document("$divide",
																		Arrays.asList("$profit", "$revenue"))))))))
								.append("as", "salesRealityMonthData")),
						new Document("$unwind", new Document("path", "$salesRealityMonthData").append(
								"preserveNullAndEmptyArrays", true)),
						new Document("$project", new Document("project_id", "$_id")
								.append("forecastYearAvgProfit",
										new Document("$ifNull",
												Arrays.asList("$salesForecastTotalData.avgProfit", 0.0)))
								.append("forecastYearPresentAvgProfit",
										new Document("$ifNull",
												Arrays.asList("$salesForecastTotalData.presentAvgProfit", 0.0)))
								.append("forecastPresentAvgProfit",
										new Document("$ifNull",
												Arrays.asList("$salesForecastTotalData.presentAvgProfit", 0.0)))
								.append("forecastRevenue",
										new Document("$ifNull", Arrays.asList("$salesForecastTotalData.revenue", 0.0)))
								.append("forecastCost",
										new Document("$ifNull", Arrays.asList("$salesForecastTotalData.cost", 0.0)))
								.append("forecastProfit",
										new Document("$ifNull", Arrays.asList("$salesForecastTotalData.profit", 0.0)))
								.append("forecastPresentRevenue",
										new Document("$ifNull",
												Arrays.asList("$salesForecastTotalData.presentRevenue", 0.0)))
								.append("forecastPresentCost",
										new Document("$ifNull",
												Arrays.asList("$salesForecastTotalData.presentCost", 0.0)))
								.append("forecastPresentProfit",
										new Document("$ifNull",
												Arrays.asList("$salesForecastTotalData.presentProfit", 0.0)))
								.append("forecastProfitRate",
										new Document("$ifNull",
												Arrays.asList("$salesForecastTotalData.profitRate", 0.0)))
								.append("forecastPresentProfitRate",
										new Document("$ifNull",
												Arrays.asList("$salesForecastTotalData.presentProfitRate", 0.0)))
								.append("forecastMonthRevenue",
										new Document("$ifNull", Arrays.asList("$salesForecastMonthData.revenue", 0.0)))
								.append("forecastMonthCost",
										new Document("$ifNull", Arrays.asList("$salesForecastMonthData.cost", 0.0)))
								.append("forecastMonthProfit",
										new Document("$ifNull", Arrays.asList("$salesForecastMonthData.profit", 0.0)))
								.append("forecastMonthProfitRate",
										new Document("$ifNull",
												Arrays.asList("$salesForecastMonthData.profitRate", 0.0)))
								.append("realityYearAvgProfit",
										new Document("$ifNull", Arrays.asList("$salesRealityTotalData.avgProfit", 0.0)))
								.append("realityYearPresentAvgProfit",
										new Document("$ifNull",
												Arrays.asList("$salesRealityTotalData.presentAvgProfit", 0.0)))
								.append("realityPresentAvgProfit",
										new Document("$ifNull",
												Arrays.asList("$salesRealityTotalData.presentAvgProfit", 0.0)))
								.append("realityRevenue",
										new Document("$ifNull", Arrays.asList("$salesRealityTotalData.revenue", 0.0)))
								.append("realityCost",
										new Document("$ifNull", Arrays.asList("$salesRealityTotalData.cost", 0.0)))
								.append("realityProfit",
										new Document("$ifNull", Arrays.asList("$salesRealityTotalData.profit", 0.0)))
								.append("realityPresentRevenue",
										new Document("$ifNull",
												Arrays.asList("$salesRealityTotalData.presentRevenue", 0.0)))
								.append("realityPresentCost",
										new Document("$ifNull",
												Arrays.asList("$salesRealityTotalData.presentCost", 0.0)))
								.append("realityPresentProfit",
										new Document("$ifNull",
												Arrays.asList("$salesRealityTotalData.presentProfit", 0.0)))
								.append("realityProfitRate",
										new Document("$ifNull",
												Arrays.asList("$salesRealityTotalData.profitRate", 0.0)))
								.append("realityPresentProfitRate",
										new Document("$ifNull",
												Arrays.asList("$salesRealityTotalData.presentProfitRate", 0.0)))
								.append("realityMonthRevenue",
										new Document("$ifNull", Arrays.asList("$salesRealityMonthData.revenue", 0.0)))
								.append("realityMonthCost",
										new Document("$ifNull", Arrays.asList("$salesRealityMonthData.cost", 0.0)))
								.append("realityMonthProfit",
										new Document("$ifNull", Arrays.asList("$salesRealityMonthData.profit", 0.0)))
								.append("realitytMonthProfitRate",
										new Document("$ifNull",
												Arrays.asList("$salesRealityMonthData.profitRate", 0.0)))
								.append("budget", new Document("$ifNull", Arrays.asList("$cbsTotalData.budget", 0.0)))
								.append("cost", new Document("$ifNull", Arrays.asList("$cbsTotalData.cost", 0.0)))
								.append("presentBudget",
										new Document("$ifNull", Arrays.asList("$cbsTotalData.presentBudget", 0.0)))
								.append("presentCost",
										new Document("$ifNull", Arrays.asList("$cbsTotalData.presentCost", 0.0)))
								.append("salesForecastYearData", true).append("salesRealityYearData", true)
								.append("cbsYearData", true)),
						new Document("$addFields", new Document("forecastPP", new Document("$cond",
								Arrays.asList(new Document("$eq", Arrays.asList("$forecastYearAvgProfit", 0.0)), 0.0,
										new Document("$multiply", Arrays.asList(
												new Document("$divide",
														Arrays.asList("$budget", "$forecastYearAvgProfit")),
												12.0))))).append(
														"forecastPresentPP",
														new Document("$cond", Arrays.asList(new Document(
																"$eq",
																Arrays.asList("$forecastYearPresentAvgProfit", 0.0)),
																0.0,
																new Document("$multiply", Arrays.asList(new Document(
																		"$divide",
																		Arrays.asList("$presentBudget",
																				"$forecastYearPresentAvgProfit")),
																		12.0)))))
														.append("realityPP", new Document("$cond", Arrays.asList(
																new Document("$eq",
																		Arrays.asList("$realityYearAvgProfit", 0.0)),
																0.0,
																new Document("$multiply", Arrays.asList(
																		new Document("$divide",
																				Arrays.asList("$cost",
																						"$realityYearAvgProfit")),
																		12.0)))))
														.append("realityPresentPP", new Document("$cond", Arrays.asList(
																new Document("$eq",
																		Arrays.asList(
																				"$realityYearPresentAvgProfit", 0.0)),
																0.0,
																new Document("$multiply", Arrays.asList(new Document(
																		"$divide",
																		Arrays.asList("$presentCost",
																				"$realityYearPresentAvgProfit")),
																		12.0)))))
														.append("forecastROI",
																new Document("$cond", Arrays.asList(
																		new Document(
																				"$eq", Arrays.asList("$budget", 0.0)),
																		0.0,
																		new Document("$divide",
																				Arrays.asList("$forecastYearAvgProfit",
																						"$budget")))))
														.append("forecastPresentROI",
																new Document("$cond",
																		Arrays.asList(
																				new Document("$eq",
																						Arrays.asList(
																								"$presentBudget", 0.0)),
																				0.0,
																				new Document("$divide", Arrays.asList(
																						"$forecastYearPresentAvgProfit",
																						"$presentBudget")))))
														.append("realityROI",
																new Document("$cond", Arrays.asList(
																		new Document(
																				"$eq", Arrays.asList("$cost", 0.0)),
																		0.0,
																		new Document("$divide",
																				Arrays.asList("$realityYearAvgProfit",
																						"$cost")))))
														.append("realityPresentROI",
																new Document("$cond",
																		Arrays.asList(
																				new Document("$eq",
																						Arrays.asList(
																								"$presentCost", 0.0)),
																				0.0,
																				new Document("$divide", Arrays.asList(
																						"$realityYearPresentAvgProfit",
																						"$presentCost")))))
														.append("forecastNPV", new Document("$add", Arrays.asList(
																new Document("$multiply",
																		Arrays.asList(-1.0, "$presentBudget")),
																"$forecastPresentProfit")))
														.append("realityNPV",
																new Document(
																		"$add",
																		Arrays.asList(
																				new Document("$multiply", Arrays
																						.asList(-1.0, "$presentCost")),
																				"$realityPresentProfit")))
														.append("forecastNPVR", new Document("$cond", Arrays.asList(
																new Document(
																		"$eq", Arrays.asList("$presentBudget", 0.0)),
																0.0,
																new Document("$divide", Arrays.asList(
																		new Document("$add", Arrays.asList(
																				new Document("$multiply",
																						Arrays.asList(-1.0,
																								"$presentBudget")),
																				"$forecastPresentProfit")),
																		"$presentBudget")))))
														.append("realityNPVR", new Document("$cond", Arrays.asList(
																new Document("$eq", Arrays.asList("$presentCost", 0.0)),
																0.0,
																new Document("$divide", Arrays.asList(
																		new Document("$add", Arrays.asList(
																				new Document("$multiply",
																						Arrays.asList(-1.0,
																								"$presentCost")),
																				"$realityPresentProfit")),
																		"$presentCost")))))));
		List<Document> projectMonthDatas = new ArrayList<Document>();
		prjColl.aggregate(pipeline).map(doc -> {
			doc.append("project_id", doc.get("_id"));
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
		}).into(projectMonthDatas);

		// 清除之前写入的月项目绩效数据
		pMDCol.deleteMany(new BasicDBObject("year", year).append("month", month));

		// 插入月项目绩效数据
		if (projectMonthDatas.size() > 0)
			pMDCol.insertMany(projectMonthDatas);
	}
}
