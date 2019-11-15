package com.bizvpm.dps.processor.tmtsap;

import java.util.Map;
import java.util.Set;

import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Client;
import com.sap.mw.jco.JCO.Function;
import com.sap.mw.jco.JCO.ParameterList;
import com.sap.mw.jco.JCO.Structure;
import com.sap.mw.jco.JCO.Table;

public class JCO_BAPI_INTERNALORDER_CREATE {

	private static final String REPOSITORY_NAME = "MYRepository"; //$NON-NLS-1$

	private static final String FUNCTION_NAME = "ZXFUN_INTERNALORDER_CREATE"; //$NON-NLS-1$

	private static final String FUNCTION_NAME_1 = "ZXFUN_FSMS_GZLH_XG";

	private static final String FUNCTION_NAME_2 = "ZXFUN_PROJECTGROUP_CREATE";

	public void createWorkOrder(Map<String, Object> parameter) throws Exception {
		Client client = SapJCOToolkit.getSAPClient();
		IRepository repository = JCO.createRepository(REPOSITORY_NAME, client);

		IFunctionTemplate ftemplate = repository.getFunctionTemplate(FUNCTION_NAME);

		if (ftemplate == null) {
			throw new IllegalArgumentException("Can not get function template, Name:" + FUNCTION_NAME); //$NON-NLS-1$
		}

		Function function = ftemplate.getFunction();
		Structure inputStr = function.getImportParameterList().getStructure("INPUT_DATA");
		Set<String> keySet = parameter.keySet();
		for (String key : keySet) {
			inputStr.setValue(parameter.get(key), key);
		}
		client.execute(function);

		// order ID
		// String outputOrderID =
		// function.getExportParameterList().getString("ORDERID");
		// System.out.println("outputOrderID:" + outputOrderID);

		// message
		// String message =
		// function.getExportParameterList().getString("MESSAGE");
		// System.out.println("message:" + message);
	}

	public void updateWorkOrder(String[] workOrders, String userId) {
		Client sapClient = SapJCOToolkit.getSAPClient();
		IRepository repository = JCO.createRepository(REPOSITORY_NAME, sapClient);

		IFunctionTemplate ftemplate = repository.getFunctionTemplate(FUNCTION_NAME_1);

		if (ftemplate == null) {
			throw new IllegalArgumentException("Can not get function template, Name:" + FUNCTION_NAME_1); //$NON-NLS-1$
		}
		Function function = ftemplate.getFunction();
		ParameterList input_table = function.getTableParameterList();
		Table tableIn = input_table.getTable("T_GZLH");
		for (int i = 0; workOrders != null && i < workOrders.length; i++) {
			tableIn.appendRow();
			tableIn.setValue(workOrders[i], "AUFNR");// 工作令号 //$NON-NLS-1$
			tableIn.setValue(userId, "USER2");
		}
		function.setTableParameterList(input_table);
		sapClient.execute(function);
		SapJCOToolkit.releaseClient(sapClient);

		Table table_out = function.getTableParameterList().getTable("T_GZLH");
		ParameterList exportParameterList = function.getExportParameterList();
		Object result = exportParameterList.getValue("RESULT");
		if ("S".equals(result)) {
			// TODO 记录错误提示
			System.out.println("项目负责人同步到SAP全部成功!");
		} else if ("W".equals(result)) {
			int numRows = table_out.getNumRows();
			if (numRows > 0) {
				for (int i = 0; i < numRows; i++) {
					table_out.setRow(i);
					JCO.Field field = table_out.getField("MSGTY");
					Object value = field.getValue();
					if (!"S".equals(value)) {
						JCO.Field field2 = table_out.getField("AUFNR");
						// TODO 记录错误提示
						System.out.println("项目负责人同步到SAP失败[" + field2.getValue() + "]!");
					}
				}
			}
		} else {
			throw new IllegalArgumentException("项目负责人同步到SAP全部失败！");
		}
	}

	public void createProjectGWorkOrder(Map<String, Object> parameter) throws Exception {
		Client client = SapJCOToolkit.getSAPClient();
		IRepository repository = JCO.createRepository(REPOSITORY_NAME, client);

		IFunctionTemplate ftemplate = repository.getFunctionTemplate(FUNCTION_NAME_2);

		if (ftemplate == null) {
			throw new IllegalArgumentException("Can not get function template, Name:" + FUNCTION_NAME_2); //$NON-NLS-1$
		}

		Function function = ftemplate.getFunction();
		Structure inputStr = function.getImportParameterList().getStructure("INPUTDATA");
		Set<String> keySet = parameter.keySet();
		for (String key : keySet) {
			inputStr.setValue(parameter.get(key), key);
		}
		client.execute(function);
	}
}
