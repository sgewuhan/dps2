package dpsclient;

import java.util.ArrayList;
import java.util.Map;

import com.bizvpm.dps.client.DPS;
import com.bizvpm.dps.client.Task;
import com.google.gson.GsonBuilder;

public class Test4 {

	public static void main(String[] args) throws Exception {
		DPS dps = new DPS("http://127.0.0.1:8199/dps/server?wsdl");

//		DPS dps = new DPS("http://10.99.9.54:82/dps/server?wsdl");
		
		Task task = new Task();
		
		task.setPriority(Task.PRIORITY_1);
		
		GsonBuilder gb= new GsonBuilder();
		String json = "";

//		System.out.println("验证通过编码获取零部件基本信息----------->>>>>>>>>>>>>>>>>");

		ArrayList<String> ouidList = new ArrayList<String>();
		ouidList.add("21066940");
		ouidList.add("1580600111");
		ouidList.add("4172116");
		task.setValue("ids", ouidList);

		System.out.println("验证获取零部件基本信息----------->>>>>>>>>>>>>>>>>");
		task.setName("验证获取零部件基本信息");
		Map<String, Object> result1 = dps.runTask(task, "com.bizvpm.dps.processor.plm.wcs:partquery");
		json = gb.create().toJson(result1);
		System.out.println(json);
		
		
		System.out.println("验证获取软属性----------->>>>>>>>>>>>>>>>>");
		ArrayList<String> attrList = new ArrayList<String>();
		attrList.add("CSR_ZHONGLIANG");	//零部整件类别
		attrList.add("CSR_CAILIAO");	//是否关重件
		attrList.add("CSR_XINGHAOGUIGE");	//ERP编码
		attrList.add("CSR_GUANXILAIYUAN");	//无用
		attrList.add("CSR_ZONGZHONG");	//文档小类
		attrList.add("CSR_YOUXIANJI");	//保密级别
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
		
		System.out.println("验证获取全局替代件----------->>>>>>>>>>>>>>>>>");
		task.setName("验证全局替代件");
		Map<String, Object> result8 = dps.runTask(task, "com.bizvpm.dps.processor.plm.wcs:alternatesquery");
		json = gb.create().toJson(result8);
		System.out.println(json);
		
		
		System.out.println("验证获取特定替代件----------->>>>>>>>>>>>>>>>>");
		ArrayList<String> linkList = new ArrayList<String>();
		linkList.add("26278248");	//
		linkList.add("26296033");	//

		Task task9 = new Task();
		task9.setName("Windchill 特定替代件");
		task9.setPriority(Task.PRIORITY_1);
		task9.setValue("linkIds", linkList);
		
		Map<String, Object> result9 = dps.runTask(task9, "com.bizvpm.dps.processor.plm.wcs:substitutequery");
		json = gb.create().toJson(result9);
		System.out.println(json);
//		
//
//		json = gb.create().toJson(result1);
//		System.out.println(json);
		
		
		
		
	}

}
