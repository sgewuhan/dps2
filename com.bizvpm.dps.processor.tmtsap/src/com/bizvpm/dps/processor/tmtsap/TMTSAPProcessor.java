package com.bizvpm.dps.processor.tmtsap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bson.Document;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bizvpm.dps.processor.tmtsap.etl.ICostCollectorFactory;
import com.bizvpm.dps.processor.tmtsap.etl.RNDPeriodCostAdapter;
import com.bizvpm.dps.processor.tmtsap.etl.WorkorderPeriodCostAdapter;
import com.bizvpm.dps.processor.tmtsap.tools.ProjectToolkit;
import com.bizvpm.dps.runtime.IProcessContext;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.bizvpm.dps.runtime.ProcessResult;
import com.bizvpm.dps.runtime.ProcessTask;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

public class TMTSAPProcessor implements IProcessorRunable {

	public MongoCollection<Document> rndCol;
	public MongoCollection<Document> workOrderCol;
	public MongoCollection<Document> saleDataCol;
	public MongoCollection<Document> rndAllocationCol;
	public MongoCollection<Document> pjCol;
	public MongoCollection<Document> projectMonthCol;
	public MongoCollection<Document> colOrgMonthData;
	public MongoCollection<Document> colOrg;
	public MongoCollection<Document> colCalendarSetting;

	public TMTSAPProcessor() {
	}

	@Override
	public ProcessResult run(ProcessTask processTask, IProgressMonitor monitor, IProcessContext context)
			throws Exception {
		processTask.getInputStream("");

		ProcessResult result = new ProcessResult();
		result.put("result", "");
		return result;
	}

	public boolean doETL(int year, int month, int day, boolean isDoMonthData, String[] costElementArray,
			String[] workOrders, String[] costCodes, ICostCollectorFactory costCodeCostCollector,
			ICostCollectorFactory workOrdersCostCollector, ISaleDataCollector sdc) throws Exception {
		long start, end;

		clear(year, month);

		System.out.println("[成本数据]准备获取成本中心数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		RNDPeriodCostAdapter rndAdapter = new RNDPeriodCostAdapter(costCodeCostCollector);
		rndAdapter.runGetData(costCodes, costElementArray, year, month);
		end = System.currentTimeMillis();
		System.out.println("[成本数据]获得成本中心数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[成本数据]准备获取工作令号研发成本数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		WorkorderPeriodCostAdapter workorderadapter = new WorkorderPeriodCostAdapter(workOrdersCostCollector);
		workorderadapter.runGetData(workOrders, costElementArray, year, month);
		end = System.currentTimeMillis();
		System.out.println("[成本数据]获得工作令号研发成本完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[销售数据]准备获取销售数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		runGetData(year, month, sdc);
		end = System.currentTimeMillis();
		System.out.println("[销售数据]获得销售数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[销售数据]准备更新项目销售数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		ProjectToolkit.updateProjectSalesData();

		ProjectToolkit.updateProjectMonthSalesData(year, month, day);

		end = System.currentTimeMillis();
		System.out.println("[销售数据]更新项目销售数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[项目数据]准备更新项目ETL数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		List<Document> projectETLList = doProjectETL(year, month, day);
		end = System.currentTimeMillis();
		System.out.println("[项目数据]更新项目ETL数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		System.out.println("[组织指标数据]准备更新组织指标ETL数据:" + year + "-" + month);
		start = System.currentTimeMillis();
		List<Document> organizationETLList = doOrganizationETL(year, month, day);
		end = System.currentTimeMillis();
		System.out.println("[组织指标数据]更新组织指标ETL数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

		if (isDoMonthData) {
			System.out.println("[项目数据]准备更新项目月ETL数据:" + year + "-" + month);
			start = System.currentTimeMillis();
			doProjectMonthETL(year, month, day, projectETLList);
			end = System.currentTimeMillis();
			System.out.println("[项目数据]更新项目月ETL数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");

			System.out.println("[组织指标数据]准备更新组织月指标ETL数据:" + year + "-" + month);
			start = System.currentTimeMillis();
			doOrganizationMonthETL(year, month, day, organizationETLList);
			end = System.currentTimeMillis();
			System.out.println("[组织指标数据]更新组织月指标ETL数据完成:" + year + "-" + month + " " + (end - start) / 1000 + " S");
		}

		return true;
	}

	public List<Document> doProjectETL(int year, int month, int day) throws Exception {
		List<Document> projectETLList = new ArrayList<Document>();
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day);
		// 加了一天然后减去一秒就变成了传入这一天的23时59分59秒
		cal.add(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MILLISECOND, -1);

		// DBCursor cur = pjCol.find(getProjectETLQuery());
		// Project project = null;
		// while (cur.hasNext()) {
		// Document Document = (Document) cur.next();
		// ObjectId id = (ObjectId) Document.get(Project.F__ID);
		// if (id != null) {
		// // if (day == 1) {
		// try {
		// project = ModelService.createModelObject(Project.class, id);
		// ProjectMonthlyETL pres = project.getMonthlyETL();
		// Document etl = pres.doETL(cal);
		// projectETLList.add(etl);
		// if (Portal.getDefault().isDevelopMode()) {
		// // Commons.loginfo(project.getLabel() + " ETL finished."); //$NON-NLS-1$
		// }
		// } catch (Exception e) {
		// if (project != null) {
		// System.out.println(project.getLabel() + " ETL error. Message: " +
		// e.getMessage()); //$NON-NLS-1$
		// } else {
		// e.printStackTrace();
		// System.out.println("Project ETL error. Can not find project, id:" + id);
		// //$NON-NLS-1$
		// }
		// }
		// }
		// }
		// cur.close();
		return projectETLList;
	}

	public List<Document> doOrganizationETL(int year, int month, int day) {
		List<Document> organizationETLList = new ArrayList<Document>();
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day);
		cal.add(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MILLISECOND, -1);

		// DBCursor cur = colOrg.find();
		// while (cur.hasNext()) {
		// DBObject dbo = cur.next();
		// Organization organization = ModelService.createModelObject(dbo,
		// Organization.class);
		// OrganizationMonthETL monthlyETL = organization.getMonthlyETL();
		// monthlyETL.setCalendarCaculater(getCalendarCaculater());
		// try {
		// DBObject etl = monthlyETL.doETL(cal);
		// organizationETLList.add(etl);
		// } catch (Exception e) {
		// Commons.logerror(organization.getLabel()
		// + " ETL error. Message: " + e.getMessage()); //$NON-NLS-1$
		// }
		// }
		// cur.close();
		//
		// /**
		// *
		// * 进行数据装载，保存至Organization
		// *
		// */
		// DBCollection colOrganization = DBActivator.getCollection(
		// IModelConstants.DB, IModelConstants.C_ORGANIZATION_ETL_DATA);
		// colOrganization.remove(new BasicDBObject());
		// colOrganization.insert(organizationETLList);

		return organizationETLList;
	}

	public void clear(int year, int month) {
		rndClear(year, month);
		workOrderClear(year, month);
		saleDataClear(year, month);
	}

	public void initCollection() {
		// rndCol = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_RND_PEROIDCOST_COSTCENTER);
		// rndAllocationCol = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_RND_PEROIDCOST_ALLOCATION);
		// workOrderCol = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_WORKORDER_COST);
		// saleDataCol = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_SALESDATA);
		// pjCol = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_PROJECT);
		// projectMonthCol = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_PROJECT_MONTH_DATA);
		// colOrgMonthData = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_ORGANIZATION__MONTH_DATA);
		// colOrg = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_ORGANIZATION);
		// colCalendarSetting = DBActivator.getCollection(IModelConstants.DB,
		// IModelConstants.C_CALENDAR_SETTING);
	}

	public void runGetData(int year, int month, ISaleDataCollector sdc) throws Exception {
		sdc.runGetData(saleDataCol, year, month);
	}

	private void rndClear(int year, int month) {
		rndCol.deleteMany(new BasicDBObject("year", year).append("month", month));
		rndAllocationCol.deleteMany(new BasicDBObject("year", year).append("month", month));
	}

	private void workOrderClear(int year, int month) {
		workOrderCol.deleteMany(new BasicDBObject("year", year).append("month", month));
	}

	private void saleDataClear(int year, int month) {
		String gjahr = "" + year;
		String perde = String.format("%03d", month);
		saleDataCol.deleteMany(new BasicDBObject("GJAHR", gjahr).append("PERDE", perde));
	}

	public void doProjectMonthETL(int year, int month, int day, List<Document> projectETLList) throws Exception {
		projectMonthCol.deleteMany(new BasicDBObject("year", year).append("month", month));
		projectMonthCol.insertMany(projectETLList);
	}

	public void doOrganizationMonthETL(int year, int month, int day, List<Document> organizationETLList)
			throws Exception {
		colOrgMonthData.deleteMany(new BasicDBObject().append("year", year).append("month", month));
		colOrgMonthData.insertMany(organizationETLList);
	}

}
