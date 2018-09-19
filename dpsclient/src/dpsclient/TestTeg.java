package dpsclient;

import java.util.ArrayList;
import java.util.Map;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.Task;
import com.google.gson.GsonBuilder;

public class TestTeg {

	public static void main(String[] args) throws Exception {
//		DPS dps = new DPS("http://127.0.0.1:8199/dps/server?wsdl");

		DPS dps = new DPS("http://10.99.9.54:82/dps/server?wsdl");
		
		Task task = new Task();
		
		task.setPriority(Task.PRIORITY_1);
		
		GsonBuilder gb= new GsonBuilder();
		String json = "";

//		System.out.println("验证通过编码获取零部件基本信息----------->>>>>>>>>>>>>>>>>");

		
		
		
		ArrayList<String> ouidList = new ArrayList<String>();
		ouidList.add("1563314934");
		ouidList.add("1511824093");
		ouidList.add("1563371121");
		task.setValue("ids", ouidList);

		System.out.println("验证获取零部件基本信息----------->>>>>>>>>>>>>>>>>");
		task.setName("验证获取零部件基本信息");
		Map<String, Object> result1 = dps.runTask(task, "com.bizvpm.dps.processor.plm.wcs:partquery");
		json = gb.create().toJson(result1);
		System.out.println(json);
		
		
		System.out.println("验证获取软属性----------->>>>>>>>>>>>>>>>>");
		task.setName("验证获取软属性");
		ArrayList<String> attrList = new ArrayList<String>();
		attrList.add("com.plm.hyth.UnitPartsType");	//零部整件类别
		attrList.add("com.plm.hyth.IsImpPart");	//是否关重件
		attrList.add("com.plm.hyth.ERPPartNumber");	//ERP编码
		attrList.add("aaa");	//无用
		attrList.add("com.plm.hyth.documentType");	//文档小类
		attrList.add("com.plm.hyth.Security");	//保密级别
		Task task2 = new Task();
		task2.setName("Windchill 查询软属性");
		task2.setPriority(Task.PRIORITY_1);
		task2.setValue("ouidList", ouidList);
		task2.setValue("attrList", attrList);
		Map<String, Object> result6 = dps.runTask(task2, "com.bizvpm.dps.processor.plm.wcs:softattrquery");
		json = gb.create().toJson(result6);
		System.out.println(json);
		
		
		System.out.println("验证获取BOM基本信息----------->>>>>>>>>>>>>>>>>");
		task.setName("验证获取BOM基本信息");
		Map<String, Object> result2 = dps.runTask(task, "com.bizvpm.dps.processor.plm.wcs:bomquery");
		json = gb.create().toJson(result2);
		System.out.println(json);
		
		
		System.out.println("验证获取零部件结构数量----------->>>>>>>>>>>>>>>>>");
		task.setName("验证获取零部件结构数量");
		Map<String, Object> result3 = dps.runTask(task, "com.bizvpm.dps.processor.plm.wcs:bomcountquery");
		json = gb.create().toJson(result3);
		System.out.println(json);
		
		
		System.out.println("验证获取零部件说明文档----------->>>>>>>>>>>>>>>>>");
		task.setName("验证获取零部件说明文档");
		Map<String, Object> result4 = dps.runTask(task, "com.bizvpm.dps.processor.plm.wcs:docDescribeQuery");
		json = gb.create().toJson(result4);
		System.out.println(json);
		
		
		System.out.println("验证获取零部件参考文档----------->>>>>>>>>>>>>>>>>");
		task.setName("验证获取零部件参考文档");
		Map<String, Object> result5 = dps.runTask(task, "com.bizvpm.dps.processor.plm.wcs:docReferenceQuery");
		json = gb.create().toJson(result5);
		System.out.println(json);
		
		System.out.println("验证获取EPM----------->>>>>>>>>>>>>>>>>");
		task.setName("验证获取EPM");
		Map<String, Object> result7 = dps.runTask(task, "com.bizvpm.dps.processor.plm.wcs:epmquery");
		json = gb.create().toJson(result7);
		System.out.println(json);
//		
//
//		json = gb.create().toJson(result1);
//		System.out.println(json);
		
		
		
		
	}

}
