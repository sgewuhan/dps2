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

public class OrganizationMonthETLService extends AbstractMonthETLService {

	@Override
	public ProcessResult run(ProcessTask pT, IProgressMonitor monitor, IProcessContext context) throws Exception {
		MongoDatabase db = Domain.getDatabase((String) pT.get("domain"));
		String year = (String) pT.get("year");
		String month = (String) pT.get("month");

		MongoCollection<Document> prjColl = db.getCollection("organization");
		MongoCollection<Document> oMDCol = db.getCollection("organizationMonthData");
		Double discountRate = getDiscountRate(db);

		createOrganizationMonthData(prjColl, oMDCol, discountRate, year, month);
		ProcessResult result = new ProcessResult();
		result.put("result", "");
		return result;
	}

	/**
	 * ������֯�¼�Ч����
	 * 
	 * @param prjColl
	 * @param oMDCol
	 * @param discountRate
	 * @param year
	 * @param month
	 */
	@SuppressWarnings("unchecked")
	private void createOrganizationMonthData(MongoCollection<Document> prjColl, MongoCollection<Document> oMDCol,
			Double discountRate, String year, String month) {
		// ��ȡ��֯�¼�Ч����
		List<Bson> pipeline = Arrays.asList(new Document("$graphLookup",
				new Document("from", "organization").append("startWith", "$_id").append("connectFromField", "_id")
						.append("connectToField", "parent_id").append("as", "children").append("depthField", "level")),
				new Document("$addFields",
						new Document("children",
								new Document("$concatArrays", Arrays.asList("$children._id", Arrays.asList("$_id"))))),
				new Document("$lookup",
						new Document("from", "project").append("let", new Document("impUnit_id", "$children"))
								.append("pipeline", Arrays.asList(
										new Document("$match",
												new Document("$expr", new Document("$and",
														Arrays.asList(new Document("$in",
																Arrays.asList("$impUnit_id", "$$impUnit_id")))))),
										new Document("$addFields", new Document(
												"discountRate", discountRate).append("nowYear", year).append("sDate",
														year + month)),
										new Document("$lookup", new Document("from", "salesforecast")
												.append("let", new Document("project_id", "$_id")
														.append("discountRate", "$discountRate")
														.append("nowYear", "$nowYear").append("sDate", "$sDate"))
												.append("pipeline", Arrays.asList(
														new Document("$match", new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$lte", Arrays.asList(
																				new Document("$concat",
																						Arrays.asList("$GJAHR",
																								"$PERDE")),
																				"$$sDate")))))),
														new Document("$group", new Document("_id", "$GJAHR")
																.append("revenue", new Document("$sum", "$VV010"))
																.append("cost", new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
																.append("profit", new Document("$sum", new Document(
																		"$subtract",
																		Arrays.asList("$VV010",
																				new Document("$add",
																						Arrays.asList("$VV030",
																								"$VV040"))))))),
														new Document("$addFields",
																new Document("exponent", new Document("$subtract",
																		Arrays.asList(new Document("$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
														new Document("$addFields", new Document("presentRevenue",
																new Document("$cond", Arrays.asList(
																		new Document("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document("$divide", Arrays.asList(
																				"$revenue",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document(
																								"$abs",
																								"$exponent"))))),
																		new Document("$multiply", Arrays.asList(
																				"$revenue",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document("$abs",
																								"$exponent"))))))))
																										.append("presentCost",
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
														new Document("$addFields", new Document("profitRate",
																new Document("$cond", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList(new Document(
																						"$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																						0.0)),
																		0.0,
																		new Document("$divide", Arrays.asList("$profit",
																				"$revenue"))))).append(
																						"presentProfitRate",
																						new Document("$cond", Arrays
																								.asList(new Document(
																										"$eq",
																										Arrays.asList(
																												new Document(
																														"$ifNull",
																														Arrays.asList(
																																"$presentRevenue",
																																0.0)),
																												0.0)),
																										0.0,
																										new Document(
																												"$divide",
																												Arrays.asList(
																														"$presentProfit",
																														"$presentRevenue")))))),
														new Document("$sort", new Document("_id", 1.0))))
												.append("as", "salesForecastYearData")),
										new Document("$lookup", new Document("from", "salesMonthData")
												.append("let", new Document("project_id", "$_id")
														.append("discountRate", "$discountRate")
														.append("nowYear", "$nowYear").append("sDate", "$sDate"))
												.append("pipeline", Arrays.asList(
														new Document("$match", new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$lte",
																				Arrays.asList(
																						new Document("$concat",
																								Arrays.asList("$GJAHR",
																										"$PERDE")),
																						"$$sDate")))))),
														new Document("$group", new Document("_id", "$GJAHR")
																.append("revenue", new Document("$sum", "$VV010"))
																.append("cost", new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
																.append("profit", new Document("$sum", new Document(
																		"$subtract",
																		Arrays.asList("$VV010",
																				new Document("$add",
																						Arrays.asList("$VV030",
																								"$VV040"))))))),
														new Document("$addFields",
																new Document("exponent", new Document("$subtract",
																		Arrays.asList(new Document("$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
														new Document("$addFields", new Document("presentRevenue",
																new Document("$cond", Arrays.asList(
																		new Document("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document("$divide", Arrays.asList(
																				"$revenue",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document(
																								"$abs",
																								"$exponent"))))),
																		new Document("$multiply", Arrays.asList(
																				"$revenue",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document("$abs",
																								"$exponent"))))))))
																										.append("presentCost",
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
														new Document("$addFields", new Document("profitRate",
																new Document("$cond", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList(new Document(
																						"$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																						0.0)),
																		0.0,
																		new Document("$divide", Arrays.asList("$profit",
																				"$revenue"))))).append(
																						"presentProfitRate",
																						new Document("$cond", Arrays
																								.asList(new Document(
																										"$eq",
																										Arrays.asList(
																												new Document(
																														"$ifNull",
																														Arrays.asList(
																																"$presentRevenue",
																																0.0)),
																												0.0)),
																										0.0,
																										new Document(
																												"$divide",
																												Arrays.asList(
																														"$presentProfit",
																														"$presentRevenue")))))),
														new Document("$sort", new Document("_id", 1.0))))
												.append("as", "salesRealityYearData")),
										new Document("$lookup", new Document("from", "cbsSubject")
												.append("let", new Document("project_id", "$_id")
														.append("discountRate", "$discountRate")
														.append("nowYear", "$nowYear"))
												.append("pipeline", Arrays.asList(new Document("$lookup",
														new Document("from", "cbs").append("localField",
																"cbsItem_id").append("foreignField", "_id")
																.append("as", "cbs")),
														new Document("$unwind", "$cbs"), new Document("$graphLookup",
																new Document("from", "cbs")
																		.append("startWith", "$cbs._id").append(
																				"connectFromField", "parent_id")
																		.append("connectToField", "_id")
																		.append("as", "cbs")),
														new Document("$lookup", new Document("from", "work").append(
																"localField", "cbs.scope_id")
																.append("foreignField", "_id").append("as", "work")),
														new Document("$unwind", new Document("path", "$work").append(
																"preserveNullAndEmptyArrays", true)),
														new Document("$lookup", new Document("from", "project").append(
																"localField", "cbs.scope_id")
																.append("foreignField", "_id").append("as", "project")),
														new Document("$unwind",
																new Document(
																		"path", "$project").append(
																				"preserveNullAndEmptyArrays", true)),
														new Document("$addFields", new Document("project_id",
																new Document("$ifNull",
																		Arrays.asList("$work.project_id",
																				"$project._id")))),
														new Document("$lookup", new Document("from", "project").append(
																"localField", "project_id").append(
																		"foreignField", "_id")
																.append("as", "project")),
														new Document("$unwind", new Document("path", "$project")),
														new Document("$project", new Document("id", true)
																.append("budget",
																		new Document("$ifNull",
																				Arrays.asList("$budget", 0.0)))
																.append("cost",
																		new Document("$ifNull",
																				Arrays.asList("$cost", 0.0)))
																.append("project_id", true)),
														new Document("$match", new Document("$expr", new Document(
																"$and",
																Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document())))),
														new Document("$group", new Document("_id", new Document(
																"$substr", Arrays.asList("$id", 0.0, 4.0)))
																		.append("budget", new Document("$sum",
																				"$budget"))
																		.append("cost", new Document("$sum", "$cost"))),
														new Document("$addFields",
																new Document("exponent", new Document("$subtract",
																		Arrays.asList(new Document("$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
														new Document("$addFields", new Document("presentBudget",
																new Document("$cond", Arrays.asList(
																		new Document("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document("$divide", Arrays.asList("$budget",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document("$abs",
																								"$exponent"))))),
																		new Document("$multiply", Arrays.asList(
																				"$budget",
																				new Document("$pow", Arrays.asList(
																						new Document("$add", Arrays
																								.asList(1.0,
																										"$$discountRate")),
																						new Document("$abs",
																								"$exponent"))))))))
																										.append("presentCost",
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
										new Document("$lookup", new Document("from", "salesMonthData")
												.append("let", new Document("project_id", "$_id")
														.append("discountRate", "$discountRate")
														.append("nowYear", "$nowYear").append("sDate", "$sDate"))
												.append("pipeline", Arrays.asList(
														new Document("$match", new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$lte",
																				Arrays.asList(
																						new Document("$concat",
																								Arrays.asList("$GJAHR",
																										"$PERDE")),
																						"$$sDate")))))),
														new Document("$group", new Document("_id", "$GJAHR")
																.append("revenue", new Document("$sum", "$VV010"))
																.append("cost", new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
																.append("profit", new Document("$sum", new Document(
																		"$subtract",
																		Arrays.asList("$VV010",
																				new Document("$add",
																						Arrays.asList("$VV030",
																								"$VV040"))))))),
														new Document("$addFields",
																new Document("exponent", new Document("$subtract",
																		Arrays.asList(new Document("$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
														new Document("$addFields", new Document("presentRevenue",
																new Document("$cond", Arrays.asList(
																		new Document("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document("$divide", Arrays.asList(
																				"$revenue",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document(
																								"$abs",
																								"$exponent"))))),
																		new Document("$multiply", Arrays.asList(
																				"$revenue",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document("$abs",
																								"$exponent"))))))))
																										.append("presentCost",
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
																.append("presentCost",
																		new Document("$sum", "$presentCost"))
																.append("presentProfit",
																		new Document("$sum", "$presentProfit"))
																.append("avgProfit",
																		new Document("$avg", "$profit"))
																.append("presentAvgProfit",
																		new Document("$avg", "$presentProfit"))),
														new Document("$addFields", new Document("profitRate",
																new Document("$cond", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList(new Document(
																						"$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																						0.0)),
																		0.0,
																		new Document("$divide", Arrays.asList("$profit",
																				"$revenue"))))).append(
																						"presentProfitRate",
																						new Document("$cond", Arrays
																								.asList(new Document(
																										"$eq",
																										Arrays.asList(
																												new Document(
																														"$ifNull",
																														Arrays.asList(
																																"$presentRevenue",
																																0.0)),
																												0.0)),
																										0.0,
																										new Document(
																												"$divide",
																												Arrays.asList(
																														"$presentProfit",
																														"$presentRevenue"))))))))
												.append("as", "salesRealityTotalData")),
										new Document("$unwind",
												new Document("path", "$salesRealityTotalData")
														.append("preserveNullAndEmptyArrays", true)),
										new Document("$addFields",
												new Document("realityYearAvgProfit", "$salesRealityTotalData.avgProfit")
														.append("realityYearPresentAvgProfit",
																"$salesRealityTotalData.presentAvgProfit")
														.append("realityPresentAvgProfit",
																"$salesRealityTotalData.presentAvgProfit")
														.append("realityRevenue", "$salesRealityTotalData.revenue")
														.append("realityCost", "$salesRealityTotalData.cost")
														.append("realityProfit", "$salesRealityTotalData.profit")
														.append("realityPresentRevenue",
																"$salesRealityTotalData.presentRevenue")
														.append("realityPresentCost",
																"$salesRealityTotalData.presentCost")
														.append("realityPresentProfit",
																"$salesRealityTotalData.presentProfit")
														.append("realityProfitRate",
																"$salesRealityTotalData.profitRate")
														.append("realityPresentProfitRate",
																"$salesRealityTotalData.presentProfitRate")),
										new Document("$project", new Document("salesRealityTotalData", false)),
										new Document("$lookup", new Document("from", "cbsSubject")
												.append("let", new Document("project_id", "$_id").append("discountRate",
														"$discountRate").append("nowYear",
																"$nowYear"))
												.append("pipeline", Arrays.asList(new Document("$lookup",
														new Document("from", "cbs").append("localField", "cbsItem_id")
																.append("foreignField", "_id").append("as", "cbs")),
														new Document("$unwind", "$cbs"), new Document("$graphLookup",
																new Document("from", "cbs")
																		.append("startWith", "$cbs._id").append(
																				"connectFromField", "parent_id")
																		.append("connectToField", "_id")
																		.append("as", "cbs")),
														new Document("$lookup", new Document("from", "work").append(
																"localField", "cbs.scope_id")
																.append("foreignField", "_id").append("as", "work")),
														new Document("$unwind", new Document("path", "$work").append(
																"preserveNullAndEmptyArrays", true)),
														new Document("$lookup", new Document("from", "project").append(
																"localField", "cbs.scope_id")
																.append("foreignField", "_id").append("as", "project")),
														new Document("$unwind",
																new Document(
																		"path", "$project").append(
																				"preserveNullAndEmptyArrays", true)),
														new Document("$addFields", new Document("project_id",
																new Document("$ifNull",
																		Arrays.asList("$work.project_id",
																				"$project._id")))),
														new Document("$lookup", new Document("from", "project").append(
																"localField", "project_id").append(
																		"foreignField", "_id")
																.append("as", "project")),
														new Document("$unwind", new Document("path", "$project")),
														new Document("$project", new Document("id", true)
																.append("budget",
																		new Document("$ifNull",
																				Arrays.asList("$budget", 0.0)))
																.append("cost",
																		new Document("$ifNull",
																				Arrays.asList("$cost", 0.0)))
																.append("project_id", true)),
														new Document("$match", new Document("$expr", new Document(
																"$and",
																Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document())))),
														new Document("$group", new Document("_id", new Document(
																"$substr", Arrays.asList("$id", 0.0, 4.0)))
																		.append("budget",
																				new Document("$sum", "$budget"))
																		.append("cost", new Document("$sum", "$cost"))),
														new Document("$addFields",
																new Document("exponent", new Document("$subtract",
																		Arrays.asList(new Document("$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
														new Document("$addFields", new Document("presentBudget",
																new Document("$cond", Arrays.asList(
																		new Document("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document("$divide", Arrays.asList("$budget",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document("$abs",
																								"$exponent"))))),
																		new Document("$multiply", Arrays.asList(
																				"$budget",
																				new Document("$pow", Arrays.asList(
																						new Document("$add", Arrays
																								.asList(1.0,
																										"$$discountRate")),
																						new Document("$abs",
																								"$exponent"))))))))
																										.append("presentCost",
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
																.append("presentBudget",
																		new Document("$sum", "$presentBudget"))
																.append("presentCost",
																		new Document("$sum", "$presentCost")))))
												.append("as", "cbsTotalData")),
										new Document("$unwind",
												new Document("path", "$cbsTotalData")
														.append("preserveNullAndEmptyArrays", true)),
										new Document("$addFields",
												new Document("budget", "$cbsTotalData.budget")
														.append("cost", "$cbsTotalData.cost")
														.append("presentBudget", "$cbsTotalData.presentBudget")
														.append("presentCost", "$cbsTotalData.presentCost")),
										new Document("$project", new Document("cbsTotalData", false)),
										new Document("$lookup", new Document("from", "salesforecast")
												.append("let", new Document("project_id", "$_id")
														.append("discountRate", "$discountRate")
														.append("nowYear", "$nowYear").append("sDate", "$sDate"))
												.append("pipeline", Arrays.asList(
														new Document("$match", new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$lte",
																				Arrays.asList(
																						new Document("$concat",
																								Arrays.asList("$GJAHR",
																										"$PERDE")),
																						"$$sDate")))))),
														new Document("$group", new Document("_id", "$GJAHR")
																.append("revenue", new Document("$sum", "$VV010"))
																.append("cost", new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
																.append("profit", new Document("$sum", new Document(
																		"$subtract",
																		Arrays.asList("$VV010",
																				new Document("$add",
																						Arrays.asList("$VV030",
																								"$VV040"))))))),
														new Document("$addFields",
																new Document("exponent", new Document("$subtract",
																		Arrays.asList(new Document("$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
														new Document("$addFields", new Document("presentRevenue",
																new Document("$cond", Arrays.asList(
																		new Document("$gte",
																				Arrays.asList("$exponent", 0.0)),
																		new Document("$divide", Arrays.asList(
																				"$revenue",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document(
																								"$abs",
																								"$exponent"))))),
																		new Document("$multiply", Arrays.asList(
																				"$revenue",
																				new Document("$pow", Arrays.asList(
																						new Document("$add",
																								Arrays.asList(
																										1.0,
																										"$$discountRate")),
																						new Document("$abs",
																								"$exponent"))))))))
																										.append("presentCost",
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
																.append("presentCost",
																		new Document("$sum", "$presentCost"))
																.append("presentProfit",
																		new Document("$sum", "$presentProfit"))
																.append("avgProfit",
																		new Document("$avg", "$profit"))
																.append("presentAvgProfit",
																		new Document("$avg", "$presentProfit"))),
														new Document("$addFields", new Document("profitRate",
																new Document("$cond", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList(new Document(
																						"$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																						0.0)),
																		0.0,
																		new Document("$divide", Arrays.asList("$profit",
																				"$revenue"))))).append(
																						"presentProfitRate",
																						new Document("$cond", Arrays
																								.asList(new Document(
																										"$eq",
																										Arrays.asList(
																												new Document(
																														"$ifNull",
																														Arrays.asList(
																																"$presentRevenue",
																																0.0)),
																												0.0)),
																										0.0,
																										new Document(
																												"$divide",
																												Arrays.asList(
																														"$presentProfit",
																														"$presentRevenue"))))))))
												.append("as", "salesForecastTotalData")),
										new Document("$unwind",
												new Document("path", "$salesForecastTotalData")
														.append("preserveNullAndEmptyArrays", true)),
										new Document("$lookup", new Document("from", "salesforecast")
												.append("let", new Document("project_id", "$_id")
														.append("discountRate", "$discountRate")
														.append("nowYear", "$nowYear").append("sDate", "$sDate"))
												.append("pipeline", Arrays.asList(
														new Document("$match", new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$eq",
																				Arrays.asList(
																						new Document("$concat",
																								Arrays.asList("$GJAHR",
																										"$PERDE")),
																						"$$sDate")))))),
														new Document("$group", new Document("_id", "$GJAHR")
																.append("revenue", new Document("$sum", "$VV010"))
																.append("cost", new Document("$sum",
																		new Document("$add",
																				Arrays.asList("$VV030", "$VV040"))))
																.append("profit", new Document("$sum", new Document(
																		"$subtract",
																		Arrays.asList("$VV010",
																				new Document("$add",
																						Arrays.asList("$VV030",
																								"$VV040"))))))),
														new Document("$addFields",
																new Document("exponent", new Document("$subtract",
																		Arrays.asList(new Document("$toInt", "$_id"),
																				new Document("$toInt", "$$nowYear"))))),
														new Document("$addFields", new Document("profitRate",
																new Document("$cond", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList(new Document("$ifNull",
																						Arrays.asList("$revenue", 0.0)),
																						0.0)),
																		0.0,
																		new Document("$divide",
																				Arrays.asList("$profit",
																						"$revenue")))))),
														new Document("$sort", new Document("_id", 1.0))))
												.append("as", "salesForecastMonthData")),
										new Document("$unwind",
												new Document("path", "$salesForecastMonthData")
														.append("preserveNullAndEmptyArrays", true)),
										new Document("$project", new Document("project_id", "$_id")
												.append("forecastYearAvgProfit",
														new Document("$ifNull",
																Arrays.asList("$salesForecastTotalData.avgProfit",
																		0.0)))
												.append("forecastYearPresentAvgProfit",
														new Document("$ifNull", Arrays.asList(
																"$salesForecastTotalData.presentAvgProfit", 0.0)))
												.append("forecastPresentAvgProfit",
														new Document("$ifNull", Arrays.asList(
																"$salesForecastTotalData.presentAvgProfit", 0.0)))
												.append("forecastRevenue",
														new Document("$ifNull",
																Arrays.asList("$salesForecastTotalData.revenue", 0.0)))
												.append("forecastCost",
														new Document("$ifNull",
																Arrays.asList("$salesForecastTotalData.cost", 0.0)))
												.append("forecastProfit",
														new Document("$ifNull",
																Arrays.asList("$salesForecastTotalData.profit", 0.0)))
												.append("forecastPresentRevenue",
														new Document("$ifNull",
																Arrays.asList("$salesForecastTotalData.presentRevenue",
																		0.0)))
												.append("forecastPresentCost",
														new Document("$ifNull",
																Arrays.asList("$salesForecastTotalData.presentCost",
																		0.0)))
												.append("forecastPresentProfit",
														new Document("$ifNull",
																Arrays.asList("$salesForecastTotalData.presentProfit",
																		0.0)))
												.append("forecastProfitRate",
														new Document("$ifNull",
																Arrays.asList("$salesForecastTotalData.profitRate",
																		0.0)))
												.append("forecastPresentProfitRate",
														new Document("$ifNull", Arrays.asList(
																"$salesForecastTotalData.presentProfitRate", 0.0)))
												.append("realityYearAvgProfit",
														new Document("$ifNull",
																Arrays.asList("$realityYearAvgProfit", 0.0)))
												.append("realityYearPresentAvgProfit",
														new Document("$ifNull",
																Arrays.asList("$realityYearPresentAvgProfit", 0.0)))
												.append("realityPresentAvgProfit",
														new Document("$ifNull",
																Arrays.asList("$realityPresentAvgProfit", 0.0)))
												.append("realityRevenue",
														new Document("$ifNull", Arrays.asList("$realityRevenue", 0.0)))
												.append("realityCost",
														new Document("$ifNull", Arrays.asList("$realityCost", 0.0)))
												.append("realityProfit",
														new Document("$ifNull", Arrays.asList("$realityProfit", 0.0)))
												.append("realityPresentRevenue",
														new Document("$ifNull",
																Arrays.asList("$realityPresentRevenue", 0.0)))
												.append("realityPresentCost",
														new Document("$ifNull",
																Arrays.asList("$realityPresentCost", 0.0)))
												.append("realityPresentProfit",
														new Document("$ifNull",
																Arrays.asList("$realityPresentProfit", 0.0)))
												.append("realityProfitRate",
														new Document("$ifNull",
																Arrays.asList("$realityProfitRate", 0.0)))
												.append("realityPresentProfitRate",
														new Document("$ifNull",
																Arrays.asList("$realityPresentProfitRate", 0.0)))
												.append("budget",
														new Document("$ifNull", Arrays.asList("$budget", 0.0)))
												.append("cost", new Document("$ifNull", Arrays.asList("$cost", 0.0)))
												.append("presentBudget",
														new Document("$ifNull", Arrays.asList("$presentBudget", 0.0)))
												.append("presentCost",
														new Document("$ifNull", Arrays.asList("$presentCost", 0.0)))
												.append("salesForecastYearData",
														true)
												.append("salesRealityYearData", true).append("cbsYearData", true)),
										new Document("$group", new Document("_id", null)
												.append("forecastYearAvgProfit",
														new Document("$avg",
																new Document("$ifNull",
																		Arrays.asList("$forecastYearAvgProfit", 0.0))))
												.append("forecastYearPresentAvgProfit", new Document("$avg",
														new Document("$ifNull",
																Arrays.asList("$forecastYearPresentAvgProfit", 0.0))))
												.append("forecastPresentAvgProfit", new Document("$avg",
														new Document("$ifNull",
																Arrays.asList("$forecastPresentAvgProfit", 0.0))))
												.append("forecastRevenue",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$forecastRevenue", 0.0))))
												.append("forecastCost",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$forecastCost", 0.0))))
												.append("forecastProfit",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$forecastProfit", 0.0))))
												.append("forecastPresentRevenue",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$forecastPresentRevenue", 0.0))))
												.append("forecastPresentCost",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$forecastPresentCost", 0.0))))
												.append("forecastPresentProfit",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$forecastPresentProfit", 0.0))))
												.append("forecastProfitRate",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$forecastProfitRate", 0.0))))
												.append("forecastPresentProfitRate", new Document("$sum",
														new Document("$ifNull",
																Arrays.asList("$forecastPresentProfitRate", 0.0))))
												.append("realityYearAvgProfit",
														new Document("$avg",
																new Document("$ifNull",
																		Arrays.asList("$realityYearAvgProfit", 0.0))))
												.append("realityYearPresentAvgProfit", new Document("$avg",
														new Document("$ifNull",
																Arrays.asList("$realityYearPresentAvgProfit", 0.0))))
												.append("realityPresentAvgProfit", new Document("$avg",
														new Document("$ifNull",
																Arrays.asList("$realityPresentAvgProfit", 0.0))))
												.append("realityRevenue",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$realityRevenue", 0.0))))
												.append("realityCost",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$realityCost", 0.0))))
												.append("realityProfit",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$realityProfit", 0.0))))
												.append("realityPresentRevenue",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$realityPresentRevenue", 0.0))))
												.append("realityPresentCost",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$realityPresentCost", 0.0))))
												.append("realityPresentProfit",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$realityPresentProfit", 0.0))))
												.append("realityProfitRate",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$realityProfitRate", 0.0))))
												.append("realityPresentProfitRate", new Document("$sum",
														new Document("$ifNull",
																Arrays.asList("$realityPresentProfitRate", 0.0))))
												.append("budget",
														new Document("$sum",
																new Document("$ifNull", Arrays.asList("$budget", 0.0))))
												.append("cost",
														new Document("$sum",
																new Document("$ifNull", Arrays.asList("$cost", 0.0))))
												.append("presentBudget",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$presentBudget", 0.0))))
												.append("presentCost",
														new Document("$sum",
																new Document("$ifNull",
																		Arrays.asList("$presentCost", 0.0))))
												.append("salesForecastYearData",
														new Document("$push", "$salesForecastYearData"))
												.append("salesRealityYearData", new Document("$push",
														"$salesRealityYearData"))
												.append("cbsYearData", new Document("$push", "$cbsYearData")))))
								.append("as", "pj")),
				new Document("$unwind", "$pj"),
				new Document("$addFields",
						new Document("pj.impUnit_id", "$_id")
								.append("pj.forecastPP",
										new Document("$cond",
												Arrays.asList(
														new Document(
																"$eq", Arrays.asList("$pj.forecastYearAvgProfit", 0.0)),
														0.0,
														new Document("$multiply",
																Arrays.asList(
																		new Document("$divide",
																				Arrays.asList("$pj.budget",
																						"$pj.forecastYearAvgProfit")),
																		12.0)))))
								.append("pj.forecastPresentPP",
										new Document("$cond",
												Arrays.asList(
														new Document("$eq",
																Arrays.asList("$pj.forecastYearPresentAvgProfit", 0.0)),
														0.0,
														new Document("$multiply", Arrays.asList(
																new Document("$divide",
																		Arrays.asList("$pj.presentBudget",
																				"$pj.forecastYearPresentAvgProfit")),
																12.0)))))
								.append("pj.realityPP",
										new Document("$cond",
												Arrays.asList(
														new Document(
																"$eq", Arrays.asList("$pj.realityYearAvgProfit", 0.0)),
														0.0,
														new Document("$multiply",
																Arrays.asList(
																		new Document("$divide",
																				Arrays.asList("$pj.cost",
																						"$pj.realityYearAvgProfit")),
																		12.0)))))
								.append("pj.realityPresentPP",
										new Document("$cond",
												Arrays.asList(
														new Document("$eq",
																Arrays.asList("$pj.realityYearPresentAvgProfit", 0.0)),
														0.0,
														new Document("$multiply", Arrays.asList(
																new Document("$divide",
																		Arrays.asList("$pj.presentCost",
																				"$pj.realityYearPresentAvgProfit")),
																12.0)))))
								.append("pj.forecastROI", new Document("$cond",
										Arrays.asList(new Document("$eq", Arrays.asList("$pj.budget", 0.0)), 0.0,
												new Document("$divide",
														Arrays.asList("$pj.forecastYearAvgProfit", "$pj.budget")))))
								.append("pj.forecastPresentROI", new Document("$cond",
										Arrays.asList(new Document("$eq", Arrays.asList("$pj.presentBudget", 0.0)), 0.0,
												new Document("$divide",
														Arrays.asList("$pj.forecastYearPresentAvgProfit",
																"$pj.presentBudget")))))
								.append("pj.realityROI", new Document("$cond",
										Arrays.asList(new Document("$eq", Arrays.asList("$pj.cost", 0.0)), 0.0,
												new Document("$divide",
														Arrays.asList("$pj.realityYearAvgProfit", "$pj.cost")))))
								.append("pj.realityPresentROI", new Document("$cond",
										Arrays.asList(new Document("$eq", Arrays.asList("$pj.presentCost", 0.0)), 0.0,
												new Document("$divide",
														Arrays.asList("$pj.realityYearPresentAvgProfit",
																"$pj.presentCost")))))
								.append("pj.forecastNPV",
										new Document("$add",
												Arrays.asList(
														new Document("$multiply",
																Arrays.asList(-1.0, "$pj.presentBudget")),
														"$pj.forecastPresentProfit")))
								.append("pj.realityNPV", new Document("$add",
										Arrays.asList(new Document("$multiply", Arrays.asList(-1.0, "$pj.presentCost")),
												"$pj.realityPresentProfit")))
								.append("pj.forecastNPVR", new Document("$cond",
										Arrays.asList(new Document("$eq", Arrays.asList("$pj.presentBudget", 0.0)), 0.0,
												new Document("$divide",
														Arrays.asList(
																new Document("$add",
																		Arrays.asList(
																				new Document("$multiply",
																						Arrays.asList(-1.0,
																								"$pj.presentBudget")),
																				"$pj.forecastPresentProfit")),
																"$pj.presentBudget")))))
								.append("pj.realityNPVR", new Document("$cond",
										Arrays.asList(new Document("$eq", Arrays.asList("$pj.presentCost", 0.0)), 0.0,
												new Document("$divide",
														Arrays.asList(
																new Document("$add",
																		Arrays.asList(
																				new Document("$multiply",
																						Arrays.asList(-1.0,
																								"$pj.presentCost")),
																				"$pj.realityPresentProfit")),
																"$pj.presentCost")))))),
				new Document("$replaceRoot", new Document("newRoot", "$pj")));

		List<Document> organizationMonthDatas = new ArrayList<Document>();
		prjColl.aggregate(pipeline).map(doc -> {
			doc.append("impUnit_id", doc.get("_id"));
			doc.append("_id", new ObjectId());
			Map<String, Double> forecastOutFlowMap = new LinkedHashMap<String, Double>();
			Map<String, Double> realityOutFlowMap = new LinkedHashMap<String, Double>();
			Map<String, Double> forecastCashFlowMap = new LinkedHashMap<String, Double>();
			Map<String, Double> realityCashFlowMap = new LinkedHashMap<String, Double>();

			Check.isAssigned((List<Object>) doc.get("cbsYearData"), cbsYearDatas -> cbsYearDatas
					.forEach(cbsYearData -> Check.isAssigned((List<Document>) cbsYearData, ls -> ls.forEach(d -> {
						String key = d.getString("_id");
						Double budget = Formatter.getDouble(d, "budget");
						Double cost = Formatter.getDouble(d, "cost");
						Double value = forecastOutFlowMap.get(key);
						if (value != null)
							forecastOutFlowMap.put(key, value - 1 * budget);
						else
							forecastOutFlowMap.put(key, -1 * budget);

						value = forecastCashFlowMap.get(key);
						if (value == null)
							forecastCashFlowMap.put(key, 0d);

						value = realityOutFlowMap.get(key);
						if (value != null)
							realityOutFlowMap.put(key, value - 1 * cost);
						else
							realityOutFlowMap.put(key, -1 * cost);

						value = realityCashFlowMap.get(key);
						if (value == null)
							realityCashFlowMap.put(key, 0d);
					}))));

			Check.isAssigned((List<Object>) doc.get("salesRealityYearData"),
					salesRealityYearDatas -> salesRealityYearDatas.forEach(salesRealityYearData -> Check
							.isAssigned((List<Document>) salesRealityYearData, l -> l.forEach(d -> {
								String key = d.getString("_id");
								Double profit = Formatter.getDouble(d, "profit");
								Double value = realityCashFlowMap.get(key);
								if (value != null)
									realityCashFlowMap.put(key, value + profit);
								else
									realityCashFlowMap.put(key, profit);
							}))));

			Check.isAssigned((List<Object>) doc.get("salesForecastYearData"),
					salesForecastYearDatas -> salesForecastYearDatas.forEach(salesForecastYearData -> Check
							.isAssigned((List<Document>) salesForecastYearData, l -> l.forEach(d -> {
								String key = d.getString("_id");
								Double profit = Formatter.getDouble(d, "profit");
								Double value = forecastCashFlowMap.get(key);
								if (value != null)
									forecastCashFlowMap.put(key, value + profit);
								else
									forecastCashFlowMap.put(key, profit);
							}))));

			doc.append("forecastIRR", getIrr(forecastOutFlowMap, forecastCashFlowMap));
			doc.append("realityIRR", getIrr(realityOutFlowMap, realityCashFlowMap));
			return doc.append("year", year).append("month", month);
		}).into(organizationMonthDatas);

		// ���֮ǰд�������������
		oMDCol.deleteMany(new BasicDBObject("year", year).append("month", month));

		// ��������������
		if (organizationMonthDatas.size() > 0)
			oMDCol.insertMany(organizationMonthDatas);
	}

}
