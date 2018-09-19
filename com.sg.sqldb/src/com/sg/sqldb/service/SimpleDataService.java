package com.sg.sqldb.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.sg.sqldb.utility.SQLDelegator;
import com.sg.sqldb.utility.SQLResult;
import com.sg.sqldb.utility.SQLRow;
import com.sg.sqldb.utility.SQLUtil;
import com.sg.sqldb.utility.Util;

public class SimpleDataService {

	/**
	 * 
	 * @param data
	 *            传入可持久化的Map, key 为 字段名，value 为可持久化值例如 字符串类型 应当在前后加上 ’
	 * @param tableName
	 *            表名
	 * @param returnField
	 *            返回的字段名用,分割
	 * @param dataSource 
	 * @return
	 * @throws Exception
	 */
	public static SQLResult insert(final Map<String, Object> data,
			final String tableName, String returnField, String dataSource) throws Exception {

		SQLDelegator sqldelegator = new SQLDelegator() {

			@Override
			protected SQLResult run(Statement stat) throws Exception {
				ResultSet rs = stat.executeQuery(SQLUtil
						.SQL_GET_NEXTVAL_SQL("SEQ_OUID"));
				rs.next();
				Object ouid = rs.getObject(1);

				String s1 = "INSERT INTO " + tableName
						+ " (SF$OUID,MD$STATUS,MD$CDATE,MD$MDATE,";
				String s2 = ") VALUES (" + ouid + ",'CRT','"
						+ Util.SDF_YYYYMMDDHHMMSS.format(new Date()) + "','"
						+ Util.SDF_YYYYMMDDHHMMSS.format(new Date()) + "',";

				Iterator<String> iter = data.keySet().iterator();
				while (iter.hasNext()) {
					String pname = iter.next();
					s1 += pname;
					s2 += data.get(pname);
					if (iter.hasNext()) {
						s1 += ",";
						s2 += ",";
					}
				}
				String sql1 = s1 + s2 + ")";
				stat.executeUpdate(sql1);

				SQLResult result = new SQLResult();
				result.setColumns(new String[] { "SF$OUID" });
				SQLRow row = new SQLRow(result.getColumns());
				row.setValue(0, ouid);
				result.add(row);
				return result;
			}
		};
		SQLResult sRes = sqldelegator.execute(dataSource);
		if (returnField == null) {
			return sRes;
		} else {
			Object ouid = sRes.getData().get(0).getValue(0);
			return SQLUtil.SQL_QUERY(dataSource, "SELECT " + returnField
					+ " FROM " + tableName + " WHERE SF$OUID = " + ouid);
		}
	}

	public static SQLResult update(final Map<String, Object> data,
			final String tableName, final String key, final Object value,
			final String returnField, String dataSource) throws Exception {
		
		String sql = "UPDATE " + tableName + " SET ";

		Iterator<String> iter = data.keySet().iterator();
		while (iter.hasNext()) {
			String pname = iter.next();
			sql += pname + " = " + data.get(pname);
			if (iter.hasNext()) {
				sql += ",";
			}
		}

		sql += " WHERE " + key + " = " + value;
		SQLUtil.SQL_UPDATE(dataSource, sql);
		
		return SQLUtil.SQL_QUERY(dataSource, "SELECT "
							+ returnField + " FROM " + tableName + " WHERE "
							+ key + " = " + value);
	}

	public static SQLResult select(String tableName, String returnFieldList, String dataSource)
			throws Exception {
		String sql = "";
		if (returnFieldList == null)
			sql = "SELECT * FROM " + tableName;
		else
			sql = "SELECT " + returnFieldList + " FROM " + tableName;
		return SQLUtil.SQL_QUERY(dataSource, sql);
	}

	public static String getStringOuidByNumber(String tableName, String number, String dataSource) {
		BigDecimal ouid = (BigDecimal) getDecimalOuidByNumber(tableName,number,dataSource);
		return tableName.toLowerCase()+"@"+Long.toHexString(ouid.longValue());
	}

	public static Object getDecimalOuidByNumber(String tableName, String number, String dataSource) {
		try {
			SQLResult result = SQLUtil.SQL_QUERY(dataSource,
					"SELECT SF$OUID FROM " + tableName + " WHERE MD$NUMBER = '"
							+ number + "'");
			if (!result.isEmpty()) {
				return result.getData().get(0).getValue(0);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static void delete(String tableName, String key, Object value, String dataSource) throws Exception {
		SQLUtil.SQL_UPDATE(dataSource, "DELETE FROM "+ tableName + " WHERE "+ key + " = "+value);
	}


}
