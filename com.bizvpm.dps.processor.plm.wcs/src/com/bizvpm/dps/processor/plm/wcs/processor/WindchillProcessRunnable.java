package com.bizvpm.dps.processor.plm.wcs.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import com.bizvpm.dps.processor.plm.wcs.Activator;
import com.bizvpm.dps.processor.plm.wcs.PreferenceConstacts;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.sg.sqldb.utility.IRowCallBack;
import com.sg.sqldb.utility.SQLRow;
import com.sg.sqldb.utility.SQLUtil;

public abstract class WindchillProcessRunnable implements IProcessorRunable {

	protected String dataSource;

	public WindchillProcessRunnable() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		dataSource = store.getString(PreferenceConstacts.DS);
	}

	protected String getInClause(List<String> list) {
		String returnString = "";
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				returnString += ",";
			}
			returnString += list.get(i);
		}
		return "in (" + returnString + ")";
	}

	protected String getStrInClause(List<String> list) {
		String returnString = "";
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				returnString += ",";
			}
			returnString += "'" + list.get(i) + "'";
		}
		return "in (" + returnString + ")";
	}

	protected Object generateSqlReqult(String sql) throws Exception {

		final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		IRowCallBack rcb = new IRowCallBack() {

			@Override
			public void input(SQLRow row) {
				result.add(toMap(row));
			}
		};

		SQLUtil.SQL_QUERY(dataSource, sql, rcb);

		return result;
	}

	protected Map<String, Object> toMap(SQLRow row) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		String[] cols = row.getColumns();
		for (int i = 0; i < cols.length; i++) {
			Object value = row.getValue(cols[i]);
			result.put(cols[i], getValue(value));
		}
		return result;
	}

	private Object getValue(Object value) {
		if(value instanceof BigDecimal){
			return value.toString();
		}else if(value instanceof Number){
			return value.toString();
		}else{
			return value;
		}
//		return null;
	}

}
