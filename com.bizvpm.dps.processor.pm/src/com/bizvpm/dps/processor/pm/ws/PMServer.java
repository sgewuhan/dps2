package com.bizvpm.dps.processor.pm.ws;

import java.util.Calendar;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.bizvpm.dps.processor.pm.Activator;
import com.bizvpm.dps.processor.pm.tools.AggregetionData;
import com.mongodb.client.MongoDatabase;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class PMServer {

	@WebMethod
	public ProjectGroupCost[] getCostSummaryByProjectGroup(
			@WebParam(name = "year", partName = "year") String year,
			@WebParam(name = "month", partName = "month") String month) {
		MongoDatabase db = Activator.db();
		// 使用时间戳解决多用户线程安全问题
		Calendar instance = Calendar.getInstance();
		String timeInMillis = String.valueOf(instance.getTimeInMillis());
		List<ProjectGroupCost> results = AggregetionData.getResult(db, timeInMillis,year, month);
//		ProjectGroupCost 
//		row = new ProjectGroupCost();
//		row.year  =year;
//		row.month = month;
//		row.complanyCode = "4010";
//		row.costCenterCode="E001010000";
//		row.buzScopeCode="4000";
//		row.accountCode="8083010000";
//		row.pjGroupCode="100000";
//		row.ammount=45678d;
//		results.add(row);
//
//		row = new ProjectGroupCost();
//		row.year  =year;
//		row.month = month;
//		row.complanyCode="4010";
//		row.costCenterCode="E001010000";
//		row.buzScopeCode="4050";
//		row.accountCode="8083010000";
//		row.pjGroupCode="100001";
//		row.ammount=12345d;
//		results.add(row);
//
//		row = new ProjectGroupCost();
//		row.year  =year;
//		row.month = month;
//		row.complanyCode="4010";
//		row.costCenterCode="E001010000";
//		row.buzScopeCode="4050";
//		row.accountCode="8083010000";
//		row.pjGroupCode="100000";
//		row.ammount=99999d;
//		results.add(row);

		return results.toArray(new ProjectGroupCost[]{});
	}

}
