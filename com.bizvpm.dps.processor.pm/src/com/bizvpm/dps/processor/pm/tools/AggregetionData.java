package com.bizvpm.dps.processor.pm.tools;

import java.util.ArrayList;
import java.util.List;

import com.bizvpm.dps.processor.pm.ws.ProjectGroupCost;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoNamespace;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class AggregetionData {

	private static final String RNDCOSTALLOCATION = "rndcostallocation";
	private static final String COST_GROUP = "cost_group_";
	private static final String COST_RESULT = "cost_result_";
	private static final String COST_PROJECTCATEGORY = "cost_projectcategory_";

	public static List<ProjectGroupCost> getResult(MongoDatabase db, String time, String year, String month) {
		ArrayList<String> codeList = new ArrayList<String>();
		MongoCollection<BasicDBObject> cost_projectcategory = db.getCollection(COST_PROJECTCATEGORY + time,
				BasicDBObject.class);
		MongoCollection<BasicDBObject> cost_group = db.getCollection(COST_GROUP + time, BasicDBObject.class);
		MongoCollection<BasicDBObject> cost_result = db.getCollection(COST_RESULT + time, BasicDBObject.class);
		MongoCollection<BasicDBObject> rndcostallocation = db.getCollection(RNDCOSTALLOCATION, BasicDBObject.class);
		AggregateIterable<BasicDBObject> aggregateCostData = aggregateCostData(year, month, rndcostallocation,
				cost_projectcategory, cost_group, codeList, db);
		aggregateCostData.iterator();
		costResult(db, cost_group, cost_result);
		List<ProjectGroupCost> costColumnToRow = costColumnToRow(db, cost_result, year, month, codeList);
		// 删除临时集合
		cost_projectcategory.drop();
		cost_group.drop();
		cost_result.drop();
		return costColumnToRow;
	}

	/**
	 * 根据项目大类和成本中心做聚合聚合，导出到cost_group表
	 * 
	 * @param _year
	 * @param _month
	 * @param rndcostallocation
	 * @param cost_projectcategory
	 * @param cost_result2
	 * @param cost_group2
	 * @param cost_group
	 * @return
	 */
	private static AggregateIterable<BasicDBObject> aggregateCostData(String _year, String _month,
			MongoCollection<BasicDBObject> rndcostallocation, MongoCollection<BasicDBObject> cost_projectcategory,
			MongoCollection<BasicDBObject> cost_group, List<String> codeList, MongoDatabase db) {
		List<BasicDBObject> pipeLine = new ArrayList<BasicDBObject>();
		BasicDBObject group = new BasicDBObject();
		BasicDBObject append = new BasicDBObject().append("_id", new BasicDBObject()
				.append("projectcategory", "$projectcategory.projectcategory").append("costcenter", "$costcenter"));
		lookupAndOut(_year, _month, append, rndcostallocation, cost_projectcategory, codeList, db);

		group.put("$group", append);

		BasicDBObject out = new BasicDBObject();
		MongoNamespace namespace = cost_group.getNamespace();
		out.put("$out", namespace.getCollectionName());

		pipeLine.add(group);
		pipeLine.add(out);

		AggregateIterable<BasicDBObject> aggregate = cost_projectcategory.aggregate(pipeLine);
		return aggregate;
	}

	/**
	 * 合并rndcostallocation、workorderdetail表
	 * 
	 * @param _year
	 * @param _month
	 * @param append2
	 * @param rndcostallocation
	 * @param cost_projectcategory2
	 */
	private static void lookupAndOut(String _year, String _month, BasicDBObject append2,
			MongoCollection<BasicDBObject> rndcostallocation, MongoCollection<BasicDBObject> cost_projectcategory,
			List<String> codeList, MongoDatabase db) {
		List<BasicDBObject> pipeLine = new ArrayList<BasicDBObject>();
		BasicDBObject match = new BasicDBObject();
		match.put("$match",
				new BasicDBObject().append("year", Integer.parseInt(_year)).append("month", Integer.parseInt(_month)));

		BasicDBObject lookup = new BasicDBObject();
		lookup.put("$lookup", new BasicDBObject().append("from", "workorderdetail").append("localField", "workorder")
				.append("foreignField", "workorder").append("as", "projectcategory"));

		BasicDBObject unwind = new BasicDBObject();
		unwind.put("$unwind", "$projectcategory");

		BasicDBObject project = new BasicDBObject();
		BasicDBObject append = new BasicDBObject().append("_id", 0).append("projectcategory.projectcategory", 1)
				.append("costcenter", 1).append("projectcategory.projectcategory", 1);
		MongoCollection<BasicDBObject> collection = db.getCollection("costaccount", BasicDBObject.class);
		DistinctIterable<String> codes = collection.distinct("accountnumber", String.class);
		MongoCursor<String> iterator = codes.iterator();
		while (iterator.hasNext()) {
			String code = iterator.next();
			append.put(code, 1);
			append2.put(code, new BasicDBObject().append("$sum", "$" + code));
			codeList.add(code);
		}
		project.put("$project", append);

		BasicDBObject out = new BasicDBObject();
		out.put("$out", cost_projectcategory.getNamespace().getCollectionName());

		pipeLine.add(match);
		pipeLine.add(lookup);
		pipeLine.add(unwind);
		pipeLine.add(project);
		pipeLine.add(out);
		rndcostallocation.aggregate(pipeLine).iterator();
	}

	/**
	 * 最终取值表
	 * 
	 * @param cost_result
	 * @param cost_result2
	 */
	private static void costResult(MongoDatabase db, MongoCollection<BasicDBObject> cost_group,
			MongoCollection<BasicDBObject> cost_result) {
		List<BasicDBObject> pipeLine = new ArrayList<BasicDBObject>();
		BasicDBObject match = new BasicDBObject();
		match.put("$match", new BasicDBObject().append("_id.projectcategory", new BasicDBObject().append("$ne", null)));
		BasicDBObject lookup = new BasicDBObject();
		lookup.put("$lookup",
				new BasicDBObject().append("from", "projectcategory").append("localField", "_id.projectcategory")
						.append("foreignField", "_id").append("as", "projectcategory"));
		BasicDBObject unwind = new BasicDBObject();
		unwind.put("$unwind", "$projectcategory");

		BasicDBObject out = new BasicDBObject();
		out.put("$out", cost_result.getNamespace().getCollectionName());

		pipeLine.add(match);
		pipeLine.add(lookup);
		pipeLine.add(unwind);
		pipeLine.add(out);
		cost_group.aggregate(pipeLine).iterator();
	}

	/**
	 * 科目转为行，成为结果数据
	 * 
	 * @param cost_result2
	 * 
	 * @param month
	 * @param year
	 * 
	 * @return List
	 */
	private static List<ProjectGroupCost> costColumnToRow(MongoDatabase db, MongoCollection<BasicDBObject> cost_result,
			String year, String month, List<String> codeList) {
		// List<Map<String, Object>> result = new ArrayList<Map<String,
		// Object>>();
		List<ProjectGroupCost> results = new ArrayList<ProjectGroupCost>();
		FindIterable<BasicDBObject> find = cost_result.find();
		MongoCursor<BasicDBObject> iterator = find.iterator();
		while (iterator.hasNext()) {
			BasicDBObject next = iterator.next();
			for (String code : codeList) {
				// Map<String, Object> element = new HashMap<String, Object>();
				ProjectGroupCost projectGroupCost = new ProjectGroupCost();
				Object object = next.get(code);
				if (object != null) {
					BasicDBObject _idDbo = (BasicDBObject) next.get("_id");
					BasicDBObject projectcategoryDbo = (BasicDBObject) next.get("projectcategory");

					Object costcenter = _idDbo.get("costcenter");
					Object prjCategoryCode = projectcategoryDbo.get("code");
					Object complanyCode = projectcategoryDbo.get("entry");
					Object buzScopeCode = projectcategoryDbo.get("management_scope");

					projectGroupCost.year = year;
					projectGroupCost.month = month;
					projectGroupCost.complanyCode = (String) complanyCode;
					projectGroupCost.costCenterCode = (String) costcenter;
					projectGroupCost.buzScopeCode = (String) buzScopeCode;
					projectGroupCost.pjGroupCode = (String) prjCategoryCode;
					projectGroupCost.accountCode = code;
					if (object instanceof Integer) {
						projectGroupCost.amount = ((Integer) object).doubleValue();
					} else {
						projectGroupCost.amount = (double) object;
					}
				}
				results.add(projectGroupCost);
			}
		}
		return results;
	}
}
