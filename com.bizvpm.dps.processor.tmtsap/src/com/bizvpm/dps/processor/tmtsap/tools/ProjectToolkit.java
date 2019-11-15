package com.bizvpm.dps.processor.tmtsap.tools;

import java.util.Calendar;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

public class ProjectToolkit {
	private static String map = "function Map() {"
			+ "emit(this.MATNR,{VV030: this.VV030, sales_income: this.VV010, VV040:this.VV040});" + "}";
	private static String reduce = "function Reduce(key, values)" + "{"
			+ "var reduced = {VV030:0, sales_income:0,VV040:0};" + "values.forEach(function(val) " + "{"
			+ "reduced.VV030 += val.VV030;" + "reduced.sales_income += val.sales_income;"
			+ "reduced.VV040 += val.VV040;" + "});" + "return reduced;" + "}";
	private static String finalize = "function Finalize(key, reduced)" + "{"
			+ "reduced.sales_cost = reduced.VV030 + reduced.VV040;" + "return reduced;" + "}";

	public static void updateProjectSalesData() {
		MongoCollection<Document> colPd = null;
		// DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_SALESDATA);

		// MapReduceCommand mapReduceCommand = new MapReduceCommand(colPd, map, reduce,
		// "productsalesdata",
		// MapReduceCommand.OutputType.REPLACE, null);
		// mapReduceCommand.setFinalize(finalize);
		// colPd.mapReduce(mapReduceCommand);
	}

	public static void updateProjectMonthSalesData(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		// cal.set(year, month, day, 0, 0, 0);
		// cal.set(Calendar.MILLISECOND, 0);
		// cal.add(Calendar.MILLISECOND, -1);
		cal.set(year, month - 1, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MILLISECOND, -1);
		Document query = new Document().append("GJAHR", "" + cal.get(Calendar.YEAR)).append("PERDE",
				String.format("%03d", cal.get(Calendar.MONTH) + 1));
		MongoCollection<Document> colPd = null;
		// DBCollection colPd = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_SALESDATA);
		// MapReduceCommand mapReduceCommand = new MapReduceCommand(colPd, map,
		// reduce, IModelConstants.C_PRODUCT_MONTH_SALESDATA,
		// MapReduceCommand.OutputType.REPLACE, query);
		// mapReduceCommand.setFinalize(finalize);
		// colPd.mapReduce(mapReduceCommand);
	}
}
